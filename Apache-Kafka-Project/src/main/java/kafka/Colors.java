package kafka;

import java.util.Properties;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;


public class Colors {
    public static void main(String[] args) {
        String BootstrapServer = "localhost:9092";
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "coffee-price-counter");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("input", Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> inputStream = textLines
            .filter((key, value) -> value.contains(","))
            .selectKey((key, value) -> value.split(",")[0].toLowerCase().trim())
            .mapValues(value -> value.split(",")[1].trim())
            .filter((key, value) -> key.equals("price"))
            .peek((key, value) -> System.out.println("INPUT KEY: " + key + " VALUE: " + value));

        KTable<String, Long> grouped = inputStream
            .groupByKey()
            .count();

        List<Long> pricesCount = new ArrayList<>();
        int threshold = 5;
        
        KStream<String, Long> groupedStream = grouped
            .toStream()
            .peek((key, value) -> System.out.println("GROUPED KEY: " + key + " VALUE: " + value))
            .flatMapValues(value->{
                if (value > threshold) {
                    pricesCount.add(value);
                }
                return pricesCount;
            });

        // System.out.println(pricesCount);

        groupedStream.to("output", Produced.with(Serdes.String(), Serdes.Long()));
        
        KStream<String, Long> saidas = builder.stream("output", Consumed.with(Serdes.String(), Serdes.Long()));
        saidas.peek((key, value) -> System.out.println("OUTPUT KEY:" + key +" VALUE: "+ value));


        KafkaStreams streams = new KafkaStreams(builder.build(), config);


        streams.setUncaughtExceptionHandler(ex -> {
            System.out.println("Kafka-Streams uncaught exception occurred. Stream will be replaced with new thread"+ ex);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
        });

        // only do this in dev - not in prod
        streams.cleanUp();
        streams.start();
        System.out.println("passei");
        // print the topology
        //streams.localThreadsMetadata().forEach(data -> System.out.println(data));
        streams.metadataForLocalThreads().forEach (data -> System.out.println(data));

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        System.out.println("oi");
    }
}