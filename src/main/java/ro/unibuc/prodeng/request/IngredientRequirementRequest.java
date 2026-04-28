package ro.unibuc.prodeng.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IngredientRequirementRequest(
        @NotBlank(message = "Inventory item id is required")
        String inventoryItemId,

        @NotNull(message = "Ingredient quantity is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Ingredient quantity must be greater than 0")
        BigDecimal quantityRequired
) {}
