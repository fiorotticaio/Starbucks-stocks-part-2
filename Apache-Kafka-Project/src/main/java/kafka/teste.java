package kafka;

import java.util.Properties;
import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;


public class teste {
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
            config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

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
                    .filter((user, value) -> Arrays.asList("green", "blue", "red").contains(value));

            usersAndColours.to("user-keys-and-colors"); //(patricia, red) (patricia, green)(joao,re d)(pedro,green)

            Serde<String> stringSerde = Serdes.String();
            Serde<Long> longSerde = Serdes.Long();

            // step 2 - we read that topic as a KTable so that updates are read correctly
            KTable<String, String> usersAndColoursTable = builder.table("user-keys-and-colors");

            // step 3 - we count the occurences of colours
            KTable<String, Long> favouriteColours = usersAndColoursTable
                    // 5 - we group by colour within the KTable
                    .groupBy((user, value) -> new KeyValue<>(value, value))
                    .count();

            // 6 - we output the results to a Kafka Topic - don't forget the serializers
            favouriteColours.toStream()
                    .peek((key, value) -> System.out.println("Depois de virar ktable Key:valor " + key +":"+ value))
                    .to("favorite-color-output", Produced.with(Serdes.String(),Serdes.Long()));

            // saidas para teste
            KStream<String, Long> saidas = builder.stream("favorite-color-output",
                                            Consumed.with(Serdes.String(), Serdes.Long()));
            saidas.peek((key, value) -> System.out.println("SAIDA Key:" + key +":"+ value));


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