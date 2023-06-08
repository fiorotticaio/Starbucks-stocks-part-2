import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

public class App {
  public static void main(String[] args) throws ExecutionException, InterruptedException {
      // Set up the AdminClient configuration
      Properties props = new Properties();
      props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      AdminClient adminClient = AdminClient.create(props);

      // Create the 'coffee_stock' topic
      NewTopic coffeeStock = new NewTopic("coffee_stock", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(coffeeStock)).all().get();

      // Create the 'coffee_price' topic
      NewTopic coffeePrice = new NewTopic("coffee_price", 3, (short) 1);
      adminClient.createTopics(Collections.singleton(coffeePrice)).all().get();

      // Create the 'coffee_sales' topic
      NewTopic coffeeSales = new NewTopic("coffee_sales", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(coffeeSales)).all().get();

      adminClient.close();
  }
}