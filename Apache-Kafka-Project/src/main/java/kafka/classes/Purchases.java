package kafka.classes;

import java.time.Instant;

public class Purchases {
    private String product;
    private double quantity;
    private Instant timestamp;
    
    public Purchases(String product, double quantity, Instant timestamp) {
        this.product = product;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public double getQuantity() {
        return quantity;
    }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    @Override
    public String toString() {
        return product + ", " + quantity + ", " + timestamp;
    }
    
    
}
