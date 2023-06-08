package kafka;

import java.util.Arrays;
import java.util.Properties;
import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;


public class InterfaceConsumer {
    public static void main(String[] args) {
        
        /* Kafka configuration */
        String BootstrapServer = "localhost:9092"; 
        String sourceTopic = "coffee_sales"; 
        String destinationTopic = "coffee_price";
        int web_coffee_price = 1;

        /* Creating consumer to receive the raw coffee buy event on interface */
        /* Consumer settings */
        Properties propConsumer = new Properties();
        propConsumer.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
        propConsumer.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        propConsumer.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        propConsumer.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        /* Consumer group settings */
        propConsumer.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "coffee-sales-consumer-group");
        propConsumer.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        propConsumer.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        propConsumer.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1000");

        /* Creating consumer to receive the records from UI calls */
        Consumer<String, String> consumer = new KafkaConsumer<>(propConsumer); 
        consumer.subscribe(Arrays.asList(sourceTopic)); 

        /* Creating producer to send coffee value got from interface to the topic */
        /* Setting new producer properties */
        Properties propProducer = new Properties();
        propProducer.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
        propProducer.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propProducer.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        /* Instantiating new producer */
        KafkaProducer<String, String> producer = new KafkaProducer<>(propProducer);

        /* Initialization of the parameter that controls the amount of sales registered */
        int countCoffeeSales = 0;
        Double coffeeValue=0.0;
        int inflationFactor=0;

        /* Loop to consume messages */
        while (true) {
            /* Polling from web partition, each second */
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000)); 
            
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("Received message: " + record.value());
                coffeeValue = Double.parseDouble(record.value()); 
                countCoffeeSales++;
                inflationFactor=0;

                System.out.println("COUNT: " + countCoffeeSales);
                
            }

            /* After 10 seconds of idle on interface, prices starts to decay */
            if (inflationFactor++ >= 10) {
                coffeeValue*=0.9;
                if (coffeeValue<0.0) coffeeValue=0.0;
                sendSalesToTopic(destinationTopic, web_coffee_price, BootstrapServer, coffeeValue, producer);
            }

            /* If sales surpass some threshold, the overall price of coffee rises */
            if (countCoffeeSales >= 5) { 
                Double newCoffeeValue = coffeeValue*1.5;    
                sendSalesToTopic(destinationTopic, web_coffee_price, BootstrapServer, newCoffeeValue, producer);
                System.out.println("Aumentou o preço do café");
                countCoffeeSales = 0; 
            }
        }
    }


    private static void sendSalesToTopic(String topic, int partition, String BootstrapServer, 
        Double newCoffeeValue, KafkaProducer<String, String> producer){

        /* Creating a record on the partition, with the new coffee value */
        String coffeeValueStr = Double.toString(newCoffeeValue);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, partition, "UI_change", coffeeValueStr);
        producer.send(record);
    }
}

