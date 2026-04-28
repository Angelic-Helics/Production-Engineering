package ro.unibuc.prodeng.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateInventoryItemRequest(
        @NotBlank(message = "Inventory item name is required")
        String name,

        @NotBlank(message = "Unit is required")
        String unit,

        @NotNull(message = "Reorder threshold is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Reorder threshold must be at least 0")
        BigDecimal reorderThreshold,

        @NotBlank(message = "Supplier id is required")
        String supplierId
) {}
