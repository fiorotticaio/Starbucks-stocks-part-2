package kafka;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;


public class teste2 {
    public static void main(String[] args) {
        String BootstrapServer = "localhost:9092";
        //String Topic = "color-pref";

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "favourite-colour-java");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in prod
       // config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

        StreamsBuilder builder = new StreamsBuilder();

        // Step 1: We create the topic of users keys to colours
        KStream<String, String> textLines = builder.stream("favorite-color-input");

        KStream<String, String> usersAndColours = textLines
                // 1 - we ensure that a comma is here as we will split on it
                .filter((key, value) -> value.contains(","))
                // 2 - we select a key that will be the user id (lowercase for safety)
                .selectKey((key, value) -> value.split(",")[0].toLowerCase().trim())
                // 3 - we get the colour from the value (lowercase for safety)
                .mapValues(value -> value.split(",")[1].toLowerCase().trim())
                // 4 - we filter undesired colours (could be a data sanitization step
                .filter((user, colour) -> Arrays.asList("green", "blue", "red").contains(colour));

        // 5 - records with keys (names) and values (colors) are inserted back into Kafka
        usersAndColours.to("user-keys-and-colors"); //(patricia, red) (patricia, green)(joao,re d)(pedro,green)

        // From now on we want to keep a history of votes for every person
        // For that, we are going to use an aggregation

        // 1 - capture de records from kafka
        KStream<String, String> userColor = builder.stream("user-keys-and-colors");

        // 2 - Define an aggregation object, which inserts a color into a history array list
        Aggregator<String, String, ArrayList<String>> agg =
                (id, color, hist) -> {
                    hist.add(color);
                    return hist;
                };

        // 3 - Now the topology...
        KTable<String,ArrayList<String>> userColorHist = userColor
                .groupByKey()
                .aggregate(()->new ArrayList<String>(),
                agg,
                Materialized.<String, ArrayList<String>, KeyValueStore<Bytes, byte[]>>as("store_history_color")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(new Serdes.ListSerde(ArrayList.class, Serdes.String())));
        // 4 - Now we get a peek into the topology
        KStream<String, ArrayList<String>> teste = userColorHist.toStream()
                .peek((k,v) -> {
                    System.out.println("user:" + k + " , cores:");
                    for (String s: v) {
                        System.out.println(s);
                    }
                });

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
