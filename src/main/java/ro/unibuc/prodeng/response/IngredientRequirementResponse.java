package ro.unibuc.prodeng.response;

import java.math.BigDecimal;

public record IngredientRequirementResponse(
        String inventoryItemId,
        String inventoryItemName,
        BigDecimal quantityRequired
) {}
