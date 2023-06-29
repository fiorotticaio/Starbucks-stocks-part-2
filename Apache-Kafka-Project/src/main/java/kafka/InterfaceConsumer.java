package kafka;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;


public class InterfaceConsumer {
    public static void main(String[] args) {
        
        /* Kafka configuration */
        String BootstrapServer = "localhost:9092"; 
        String sourceTopic = "coffee_sales"; 
        String destinationTopic = "web_coffee_price";

        /* Creating kafka stream props */
        Properties kStreamProps = new Properties();
        kStreamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "coffee-price-interface-processor");
        kStreamProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);


        /* Creating stream builder */
        StreamsBuilder builder = new StreamsBuilder();


        // TODO: trocar pela operação statefull count e janelas temporais
        // AtomicInteger countCoffeeSales = new AtomicInteger(0); // Initializing the counter

        /* Creating kStream to recive the data from source topic */
        // KStream<String, String> sourceStream = builder.stream(sourceTopic,
        //     Consumed.with(Serdes.String(), Serdes.String()));


        //FIXME: isso nao ta funcionando ............
        KTable<String, String> coffeeSales = builder.table(sourceTopic);

        // Group by key and count occurrences
        KTable<String, Long> countTable = coffeeSales
            .groupBy((key, value) -> new KeyValue<>(value, value))
            .count();

        // Process the count values
        countTable
            .toStream()
            .peek((key, value) -> System.out.println("Key:" + key +" Valor:"+ value))
            .to(destinationTopic, Produced.with(Serdes.String(),Serdes.Long()));
        
            
        /* Creating Ktream to recive the web price event */
        // KStream<String, String> webPriceValuesStream = sourceStream
        //     .mapValues((key, value) -> {
        //         System.out.println("Received message - key: " + key + " value: " + value);
        //         // Double coffeeValue = Double.parseDouble(value); 
                
        //         // System.out.println("COUNT: " + countCoffeeSales.incrementAndGet()); // Incrementing the counter
        
        //         // /* If sales surpass some threshold, the overall price of coffee rises */
        //         // if (countCoffeeSales.get() >= 5) { 
        //         //     coffeeValue *= 1.5;    
        //         //     System.out.println("Aumentou o preco do cafeh: " + coffeeValue);
        //         //     countCoffeeSales.set(0); // Resetting the counter
        //         //     return coffeeValue.toString();  
        //         // } else return "0";

        //         return "0";

        //     });

        // KStream<String, String> webPriceKeyValuesStream = webPriceValuesStream
        //     .selectKey((key, value) -> {
        //         String newKey = "cafe";
        //         return newKey;
        //     });

        // /* Sending the web price to the destination topic */
        // webPriceKeyValuesStream.to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));


        /* Creating kafka stream */
        KafkaStreams streams = new KafkaStreams(builder.build(), kStreamProps);
        streams.start();
    } 
}
