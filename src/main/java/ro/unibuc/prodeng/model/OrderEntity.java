package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public record OrderEntity(
        @Id
        String id,
        String itemName,
        int quantity,
        String specialInstructions,
        OrderStatus status,
        String customerId
) {}
