package ro.unibuc.prodeng.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RestockInventoryItemRequest(
        @NotNull(message = "Restock quantity is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Restock quantity must be greater than 0")
        BigDecimal quantity
) {}
