package kafka;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;


public class CoffeeStockProducer {
  public static void main(String[] args) throws InterruptedException, IOException {

    /* Kafka configuration */
    String BootstrapServer = "localhost:9092";
    String topic = "coffee_stock";

    /* Setting producer properties */
    Properties prop = new Properties();
    prop.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BootstrapServer);
    prop.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    prop.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    /* Creating producer */
    KafkaProducer<String, String> producer = new KafkaProducer<>(prop); 

    /* Settings for connection with Alpha Vantage API, where the real stock SBUX is located */
    String function = "TIME_SERIES_INTRADAY";
    String apiKey = "0WCXGJ9X5SRNOH5M";
    String symbol = "SBUX";
    String interval = "1min";
    String url = String.format("https://www.alphavantage.co/query?function=%s&interval=%s&symbol=%s&apikey=%s", function, interval, symbol, apiKey);

    /* Request to API */
    HttpClient client = HttpClientBuilder.create().build();
    int i = 0;
    while (true) {
      i += 1;
      HttpGet request = new HttpGet(url);
      URL apiUrl = new URL(url);
      HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
      conn.setRequestMethod("GET");

      /* If successfully responded, send the raw response to kafka in byte[] */
      try {
        HttpResponse response = client.execute(request);
        String responseObject = EntityUtils.toString(response.getEntity());
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, "cafe", responseObject);
        producer.send(record);
        System.out.println("Publishing API Raw on " + topic);

      } catch (Exception ex) {
        ex.printStackTrace();
      }

      /* Sleep for 10 seconds, before getting new API call */
      Thread.sleep(10000); 
    }
  }
}