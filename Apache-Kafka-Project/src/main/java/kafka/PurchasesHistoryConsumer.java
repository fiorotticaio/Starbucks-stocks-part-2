package kafka;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

public class PurchasesHistoryConsumer {
    public static void main(String[] args) {
        String BootstrapServer = "localhost:9092";
        String destinationTopic = "purchases_history_web";
        String sourceTopic = "purchase_history";
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "purchases-history-processor");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> purchasesHistoryStream = builder.stream(sourceTopic,
            Consumed.with(Serdes.String(), Serdes.String()));

        purchasesHistoryStream
        .mapValues(value -> { 
            return value.split(",")[1]+" "+value.split(",")[2];
        })
        .peek((key, value) -> System.out.println("[PURCHASE HISTORY] KEY:" + key +" VALUE: "+ value))
        .to(destinationTopic);


        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), config);
        kafkaStreams.start();
    }
}
