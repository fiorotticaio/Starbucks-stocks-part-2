package kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;

import kafka.classes.Purchases;


public class InterfaceConsumer {
    public static void main(String[] args) {
        
        // Config variables and topics
        String BootstrapServer = "localhost:9092";
        String sourceTopic = "coffee_sales";
        String destinationTopic = "web_coffee_price";
        String purchaseTopic = "purchase_history";
        int threshold = 5;
        Map<String, Purchases> purchaseHistory = new HashMap<>();

        // Kafka stream configs and builder initialization
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "coffee-price-counter");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        StreamsBuilder builder = new StreamsBuilder();

        // Getting the topic where the interface purchases are sended
        KStream<String, String> textLines = builder.stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.String()));

        textLines.foreach((key, value) ->
            System.out.println("[INPUT] KEY: " + key + " VALUE: " + value)
        );
        // Sanitizing the incoming data
        // KStream<String, String> inputStream = textLines
        //     .filter((key, value) -> value.contains(","))
        //     .selectKey((key, value) -> value.split(",")[0].toLowerCase().trim())
        //     .mapValues(value -> value.split(",")[1].trim())
        //     .filter((key, value) -> key.equals("price"))
        //     .peek((key, value) -> System.out.println("[INPUT] KEY: " + key + " VALUE: " + value));
        
        
        // Counting amount of purchases
        KTable<String, Long> grouped = textLines
            .groupByKey()
            .count();

        // Saving purchase history
        KStream<String, Purchases> purchaseStream = grouped
            .toStream()
            .mapValues((key, value) -> {
                Purchases p = new Purchases(key, value, java.time.Instant.now());
                purchaseHistory.put(key, p);
                return p;
            });
        
        KStream<String, String> purchaseStringStream = purchaseStream
            .mapValues((key, value) -> value.toString());
        
        purchaseStringStream
            .to(purchaseTopic, Produced.with(Serdes.String(), Serdes.String()));


        // Mapping amount of purchases accoding to threshold and converting count to String
        KStream<String, String> groupedStream = grouped
            .toStream()
            .peek((key, value) -> displayPurchaseHistory(purchaseHistory))
            .mapValues(value->{
                if (value%threshold==0) return value.toString();
                else return "null";
            });
        // Joining streams of ammounts and streams with prices 
        KStream<String, String> joinedStream = groupedStream.leftJoin(
            textLines,
            (count, price) -> {
                if (count.equals("null")) return "null";
                else {
                    if (price==null) return price;
                    Double newValue = Double.parseDouble(price)*1.1;
                    return newValue.toString();
                }
            },
            JoinWindows.of(Duration.ofSeconds(1)),
            StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String())
            );
        // Output of prices to the destination topic (which will be merged in the futures)
        joinedStream
            .filter((key, value) -> { if (value!=null) return !value.contains("null"); else return false;})
            .selectKey((key, value) -> "price")     // para fazer o join, o kafka precisa que as chaves sejam iguais. A key do APIstream é "price". Isso significa que comprar um café grande resulta num aumento geral dos preços, assim como antes.
            .peek((key, value) -> System.out.println("[OUTPUT] KEY:" + key +" VALUE: "+ value))
            .to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));

        // Final configuration            
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.setUncaughtExceptionHandler(ex -> {
            System.out.println("Kafka-Streams 1 uncaught exception occurred. Stream will be replaced with new thread"+ ex);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
        });
        
        
        streams.start();
    }
    
    private static void displayPurchaseHistory(Map<String, Purchases> purchaseHistory) {
        System.out.println("---- Histórico de Compras ----");
        for (Map.Entry<String, Purchases> entry : purchaseHistory.entrySet()) {
            Purchases purchase = entry.getValue();
            System.out.println(entry.getKey() + ": " + purchase.getQuantity() + " cafés comprados em " + purchase.getTimestamp());
        }
        System.out.println("-----------------------------");
    }
}