package ro.unibuc.prodeng.model;

import java.math.BigDecimal;

public record IngredientRequirement(
        String inventoryItemId,
        BigDecimal quantityRequired
) {}
