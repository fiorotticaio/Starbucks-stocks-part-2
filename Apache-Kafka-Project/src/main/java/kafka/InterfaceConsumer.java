package kafka;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;


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
        

        /* Creating kStream to recive the data from source topic */
        KStream<String, String> sourceStream = builder.stream(sourceTopic,
            Consumed.with(Serdes.String(), Serdes.String()));

        // TODO: trocar pela operação statefull count e janelas temporais
        AtomicInteger countCoffeeSales = new AtomicInteger(0); // Initializing the counter
            
        /* Creating Ktream to recive the web price event */
        KStream<String, String> webPriceValuesStream = sourceStream
            .mapValues((key, value) -> {
                System.out.println("Received message - key: " + key + " value: " + value);
                Double coffeeValue = Double.parseDouble(value); 
                
                System.out.println("COUNT: " + countCoffeeSales.incrementAndGet()); // Incrementing the counter
        
                /* If sales surpass some threshold, the overall price of coffee rises */
                if (countCoffeeSales.get() >= 5) { 
                    coffeeValue *= 2;    
                    System.out.println("Aumentou o preco do cafeh: " + coffeeValue);
                    countCoffeeSales.set(0); // Resetting the counter
                } 
                return coffeeValue.toString();
            });

        KStream<String, String> webPriceKeyValuesStream = webPriceValuesStream
            .selectKey((key, value) -> {
                String newKey = "cafe";
                return newKey;
            });

        /* Sending the web price to the destination topic */
        webPriceKeyValuesStream.to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));


        /* Creating kafka stream */
        KafkaStreams streams = new KafkaStreams(builder.build(), kStreamProps);
        streams.start();
    } 
}
