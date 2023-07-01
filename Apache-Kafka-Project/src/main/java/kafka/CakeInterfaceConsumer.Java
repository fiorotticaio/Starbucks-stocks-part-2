package kafka;

import java.util.Arrays;
import java.util.Properties;
import java.time.Duration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;


public class InterfaceConsumer {
    public static void main(String[] args) {
        
        // Config variables and topics
        String BootstrapServer = "localhost:9092";
        String sourceTopic = "cake_sales";
        String destinationTopic = "web_cake_price";
        int threshold = 5;

        // Kafka stream configs and builder initialization
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "cake-price-counter");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        StreamsBuilder builder = new StreamsBuilder();

        // Getting the topic where the interface purchases are sended
        KStream<String, String> textLines = builder.stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.String()));

        // Sanitizing the incoming data
        KStream<String, String> inputStream = textLines
            .filter((key, value) -> value.contains(","))
            .selectKey((key, value) -> value.split(",")[0].toLowerCase().trim())
            .mapValues(value -> value.split(",")[1].trim())
            .filter((key, value) -> key.equals("price"))
            .peek((key, value) -> System.out.println("[INPUT] KEY: " + key + " VALUE: " + value));

        // Counting amount of purchases
        KTable<String, Long> grouped = inputStream
            .groupByKey()
            .count();
    
        // Mapping amount of purchases accoding to threshold and converting count to String
        KStream<String, String> groupedStream = grouped
            .toStream()
            .peek((key, value) -> System.out.println("[COUNT] KEY: " + key + " VALUE: " + value))
            .mapValues(value->{
                if (value%threshold==0) return value.toString();
                else return "null";
            })

        // Joining streams of ammounts and streams with prices 
        KStream<String, String> joinedStream = groupedStream.leftJoin(
            inputStream,
            (count, price) -> {
                if (count.equals("null")) return "null";
                else return price;
            },
            JoinWindows.of(Duration.ofSeconds(1)),
            StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String())
        );

        // Output of prices to the destination topic (which will be merged in the futures)
        joinedStream
            .filter((key, value) -> { if (value!=null) return !value.contains("null"); else return false;})
            .peek((key, value) -> System.out.println("[OUTPUT] KEY:" + key +" VALUE: "+ value))
            .to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));

        // TODO: Decay stream
        // KStream<Windowed<String>,String> decayingStream = inputStream
        //     .groupByKey()
        //     .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
        //     .count()
        //     .filter((windowedKey, count) -> count == 0)
        //     .mapValues((windowedKey, count) -> "0.9")
        //     .toStream();
        
        // KStream<String, String> transformedStream = decayingStream
        //     .map((windowedKey, value) -> new KeyValue<>(windowedKey.key(), value));
        
        // KStream<String, String> resultStream = joinedStream.leftJoin(
        //     transformedStream,
        //     (price, decay) -> {if (decay!=null) return Long.parseLong(decay) * Long.parseLong(price);},
        //     JoinWindows.of(Duration.ofSeconds(1)),
        //     StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String()));
        
        // resultStream.to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));
        
        
        // Final configuration            
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.setUncaughtExceptionHandler(ex -> {
            System.out.println("Kafka-Streams uncaught exception occurred. Stream will be replaced with new thread"+ ex);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
        });

        streams.start();
    }
}