package ro.unibuc.prodeng.model;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "menu_items")
public record MenuItemEntity(
        @Id
        String id,
        String name,
        String description,
        BigDecimal price,
        List<IngredientRequirement> recipe
) {}
