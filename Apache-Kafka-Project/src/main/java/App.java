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
      NewTopic coffeePrice = new NewTopic("coffee_price", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(coffeePrice)).all().get();

      // Create the 'api_coffee_price' topic
      NewTopic ApiCoffeePrice = new NewTopic("api_coffee_price", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(ApiCoffeePrice)).all().get();

      // Create the 'web_coffee_price' topic
      NewTopic WebCoffeePrice = new NewTopic("web_coffee_price", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(WebCoffeePrice)).all().get();

      // Create the 'coffee_sales' topic
      NewTopic coffeeSales = new NewTopic("coffee_sales", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(coffeeSales)).all().get();

      // Create the 'purchase_history' topic
      NewTopic purchaseHistory = new NewTopic("purchase_history", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(purchaseHistory)).all().get();

      // Create the 'purchases_history_web' topic
      NewTopic purchasesHistoryWeb = new NewTopic("purchases_history_web", 1, (short) 1);
      adminClient.createTopics(Collections.singleton(purchasesHistoryWeb)).all().get();

      adminClient.close();
  }
}