package kafka;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;


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

    apiCoffeePriceStream
      .peek((key, value) -> System.out.println("[API] KEY:" + key +" VALUE: "+ value));

    webCoffeePriceStream
      // .mapValues(value -> value.split(",")[1].trim())
      .peek((key, value) -> System.out.println("[WEB] KEY:" + key +" VALUE: "+ value));



    /* left join of the two streams */
    KStream<String, String> joinedStream = apiCoffeePriceStream.leftJoin(webCoffeePriceStream,
      (apiPrice, webPrice) -> {
        if (webPrice != null) {
          if (Double.parseDouble(webPrice) != 0) {
            /* If there is a matching value in the right stream return the average of the two values */
            Double rtn = mergePrices(Double.parseDouble(apiPrice), Double.parseDouble(webPrice));
            System.out.printf("Real coffee price [%s||%s]: %.2f\n", webPrice, apiPrice, rtn);
            return rtn.toString();
          } else {
            /* If there is no matching value in the right stream, return the value in the left stream */
            return apiPrice;
          }
        } else {
          System.out.printf("Real coffee price [00||%s]: %.2f\n", apiPrice, Double.parseDouble(apiPrice));
          return apiPrice;
        }
      },

      JoinWindows.of(Duration.ofSeconds(10)),
      StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String())
    );

    /* Sending result stream to destination topic */
    joinedStream
    .peek((key, value) -> System.out.println("[MERGED] KEY:" + key +" VALUE: "+ value))
    .to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));

      
    /* Creating kafka stream */
    KafkaStreams streams = new KafkaStreams(builder.build(), kStreamProps);
    streams.start();
  }

  
  private static double mergePrices(double webPrice, double apiPrice) {
    /* Checking if one of the prices are not set, if so, set the other */
    if (webPrice == 0 && apiPrice != 0)      return apiPrice;
    else if (webPrice != 0 && apiPrice == 0) return webPrice;

    /* 
      If price from web is too low (because of inflation factor implemented 
      on interface consumer), then just use the api price.
    */
    if (webPrice < apiPrice) return apiPrice;
    
    /* The merging strategy is a simple arithmetic average */
    return (webPrice + apiPrice) / 2;
  }
}
