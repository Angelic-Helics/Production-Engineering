package ro.unibuc.prodeng.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventory_items")
public record InventoryItemEntity(
        @Id
        String id,
        String name,
        String unit,
        BigDecimal quantityInStock,
        BigDecimal reorderThreshold,
        String supplierId
) {}
