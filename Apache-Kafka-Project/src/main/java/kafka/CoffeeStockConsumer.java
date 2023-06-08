package kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

public class CoffeeStockConsumer {
  public static void main(String[] args) throws InterruptedException {
    
    /* Kafka configuration */
    String BootstrapServer = "localhost:9092"; 
    String sourceTopic = "coffee_stock"; 
    String destinationTopic = "api_coffee_price";
    int inflationFactor = 5;


    /* Creating kafka stream props */
    Properties kStreamProps = new Properties();
    kStreamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "coffee-stock-processor");
    kStreamProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);


    /* Creating stream builder */
    StreamsBuilder builder = new StreamsBuilder();
    

    /* Creating kStream to recive the data from source topic */
    KStream<String, String> sourceStream = builder.stream(sourceTopic,
      Consumed.with(Serdes.String(), Serdes.String()));


    /* Creating kStream to recive the close value */
    KStream<String, String> closedValuesStream = sourceStream
      .mapValues(value -> {
        String[] lines = value.split("\n", -1); // Split the message in a lot of lines
        StringBuilder sb = new StringBuilder();
        
        for (String line : lines) {
          if (line.contains("\"4. close\": ")) {
            /* Get close value in the correct line */
            int startIndex = line.indexOf(":") + 2;
            int endIndex = line.lastIndexOf("\"");
            String closeValue = line.substring(startIndex, endIndex);
            sb.append(closeValue);
          }
        }

        return sb.toString();
      });


    /* Creating stream to recive the api coffee price */
    KStream<String, String> apiCoffeePriceStream = closedValuesStream
      .flatMapValues(value -> {
        String[] numbers = value.split("\""); // Split the message in a lot of values
        Double sumCloseValue = 0.0;
        int countStockSales = 0;

        /* Initialization of arbitrary value for coffee */
        Double globalCloseAverage = 106.0, coffeePrice = 4.0;

        List<String> coffePrices = new ArrayList<>();

        for (String number : numbers) {
          try {
            Double isolatedValue = Double.parseDouble(number);
            sumCloseValue += isolatedValue;
            countStockSales++;
          } catch (NumberFormatException e) {
            // Ignore values that can not be converted
          }
        }

        /* Calculating the average for these sales*/
        Double closeAverageOnRequest = sumCloseValue / countStockSales;

        /* Inserting this influence on global close average */
        globalCloseAverage = (globalCloseAverage + closeAverageOnRequest) / 2;

        /* Here, for illustration purposes, the 'close' value receives a random change */
        int increaseOrDecrease = 0;
        if (new Random().nextDouble()>.5) increaseOrDecrease=-1; else increaseOrDecrease=1;
        Double variation = (new Random().nextDouble()*inflationFactor)*increaseOrDecrease;
        Double randomClose = globalCloseAverage + variation;

        /* Updating the historical average or setting new one if it is the first iteration */
        Double lastAverage = globalCloseAverage;
        globalCloseAverage= (globalCloseAverage + randomClose) / 2;
        Double closeAverageVariation = ((globalCloseAverage - lastAverage) / globalCloseAverage) * 100;
    
        /* Updating coffee value */
        coffeePrice = changeCoffeeValue(coffeePrice, closeAverageVariation); 

        System.out.println("Api Coffe Price: " + coffeePrice);

        coffePrices.add(coffeePrice.toString()); // Adding the coffee price to the list

        return coffePrices;
      });

    /* Send coffee price to a new topic, the api_coffee_price */
    apiCoffeePriceStream.to(destinationTopic, Produced.with(Serdes.String(), Serdes.String()));
    //TODO: se descobrirem como mandar pra partição específica como era antes seria ideal


    /* Creating kafka stream */
    KafkaStreams streams = new KafkaStreams(builder.build(), kStreamProps);
    streams.start(); 
  }


  private static double changeCoffeeValue(double coffeeValue, double closeAverageVariation) {
    /* If the variation is greater than 1.5%, the coffee value increases */
    if (closeAverageVariation >= 2 || closeAverageVariation <= 2) { 
      /* The increase value is 'boosted' for generating more visual effect */
      coffeeValue += closeAverageVariation / 10; 
    }
    return coffeeValue;
  }
}
