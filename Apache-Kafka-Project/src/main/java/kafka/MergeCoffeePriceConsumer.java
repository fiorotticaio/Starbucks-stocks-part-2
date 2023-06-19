package kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class MergeCoffeePriceConsumer {
  public static void main(String[] args) throws InterruptedException {
    /* Kafka configuration */
    String BootstrapServer = "localhost:9092"; 
    String destinationTopic = "coffee_price";
    String sourceTopic1 = "api_coffee_price";
    String sourceTopic2 = "web_coffee_price"; 


    /* Creating kafka stream props */
    Properties kStreamProps = new Properties();
    kStreamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "merge-coffee-price-processor");
    kStreamProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);


    /* Creating table builder */
    StreamsBuilder builder = new StreamsBuilder();


    /* Creating kStream to receive the data from api coffee price topic */
    KStream<String, String> apiCoffeePriceStream = builder.stream(sourceTopic1,
      Consumed.with(Serdes.String(), Serdes.String()));

    /* Creating kStream to receive the data from web coffee price topic */
    KStream<String, String> webCoffeePriceStream = builder.stream(sourceTopic2,
      Consumed.with(Serdes.String(), Serdes.String()));

    // /* Convert KStreams to KTables */
    // KTable<String, String> apiCoffeePriceTable = apiCoffeePriceStream
    //   .groupByKey()
    //   .reduce((oldValue, newValue) -> newValue, Materialized.as("api-coffee-price-table"));

    // KTable<String, String> webCoffeePriceTable = webCoffeePriceStream
    //   .groupByKey()
    //   .reduce((oldValue, newValue) -> newValue, Materialized.as("web-coffee-price-table"));


    /* Fazendo o left join das duas streams */
    KStream<String, String> joinedStream = apiCoffeePriceStream.leftJoin(
      webCoffeePriceStream,
      (apiPrice, webPrice) -> {
        if (Double.parseDouble(webPrice) != null) {
          // Se houver um valor correspondente na stream direita
          // retorne a média dos dois valores
          Double rtn = (Double.parseDouble(apiPrice) + Double.parseDouble(webPrice)) / 2.0;
          return rtn.toString();
        } else {
          // Se não houver valor correspondente na stream direita
          // mantenha o valor da stream esquerda
          return apiPrice;
        }
      },
      JoinWindows.of(3000),
      Joined.with(Serdes.String(), Serdes.String(), Serdes.String())
    );

    /* Enviando o resultado para o tópico de destino */
    joinedStream.to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));

      
    /* Creating kafka stream */
    KafkaStreams streams = new KafkaStreams(builder.build(), kStreamProps);
    streams.start();











    // /* Setting consumer 1 properties */
    // Properties propConsumer = new Properties();
    // propConsumer.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
    // propConsumer.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    // propConsumer.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    // propConsumer.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    
    // /* Consumer group settings (this is not necessary. Learning purposes)*/
    // propConsumer.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "price_group");
    // propConsumer.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    // propConsumer.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    // propConsumer.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1000");
  
    // /* Creating two consumers, one for each partition */  
    // KafkaConsumer<String, String> consumerApiCoffeePriceTopic = new KafkaConsumer<>(propConsumer);
    // KafkaConsumer<String, String> consumerWebCoffeePriceTopic = new KafkaConsumer<>(propConsumer);

    // /* New producer that send a record in the last partition of the "coffee_price" topic */
    // Properties propProducer = new Properties();
    // propProducer.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
    // propProducer.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // propProducer.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // /* Creating producer to send the merged value to partition */
    // KafkaProducer<String, String> producer = new KafkaProducer<>(propProducer);

    // /* Assining the topic on the api consumer */
    // consumerApiCoffeePriceTopic.subscribe(Collections.singletonList("api_coffee_price"));    
    
    // /* Assining the topic on the web consumer */
    // consumerWebCoffeePriceTopic.subscribe(Collections.singletonList("web_coffee_price"));

    // String apiCoffePriceStr = "0.0";
    // String webCoffePriceStr = "0.0";
    // int count = 0;
    
    // while (true) {
    //   /* Polling from api topic on topic, each second */
    //   ConsumerRecords<String, String> records2 = consumerApiCoffeePriceTopic.poll(Duration.ofMillis(2000));
    //   for (ConsumerRecord<String, String> record : records2) {
    //     /* Storing the value for api topic on string */
    //     apiCoffePriceStr = record.value();
    //     System.out.println("apiCoffePriceStr: " + apiCoffePriceStr);
    //   }

    //   // /* Polling from web topic on topic, each second */
    //   ConsumerRecords<String, String> records1 = consumerWebCoffeePriceTopic.poll(Duration.ofMillis(2000));
    //   for (ConsumerRecord<String, String> record : records1) {
    //     /* Storing the value for web topic on string, if it is greater them 0 */
    //     if (Double.parseDouble(record.value()) != 0) webCoffePriceStr = record.value();
    //     System.out.println("webCoffePriceStr: " + webCoffePriceStr);
    //   }

    //   /* Merging both values */
    //   double realCoffeePrice = mergePrices(Double.parseDouble(webCoffePriceStr), Double.parseDouble(apiCoffePriceStr));
    //   System.out.printf("Real coffee price [%s/%s]: %.2f\n", webCoffePriceStr, apiCoffePriceStr, realCoffeePrice);

    //   sendRealCoffeePriceToTopic(count++, topic, real_coffee_price, BootstrapServer, realCoffeePrice, producer);
    // }
  }

  private static void sendRealCoffeePriceToTopic(int id, String topic, int partition, 
    String BootstrapServer, double realCoffeePrice, KafkaProducer<String, String> producer) {

    /* Create a record to partition of the topic */
    String realCoffeePriceStr = Double.toString(realCoffeePrice);
    ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, partition, "id_"+id, realCoffeePriceStr);
    producer.send(record);
  }

  private static double mergePrices(double webPrice, double apiPrice) {
    /* Checking if one of the prices are not set, if so, set the other */
    if (webPrice==0 && apiPrice!=0) return apiPrice;
    else if (webPrice!=0 && apiPrice==0) return webPrice;

    /* 
      If price from web is too low (because of inflation factor implemented 
      on interface consumer), then just use the api price.
    */
    if (webPrice < apiPrice) return apiPrice;
    
    /* The merging strategy is a simple arithmetic average */
    return (webPrice + apiPrice) / 2;
  }
}
