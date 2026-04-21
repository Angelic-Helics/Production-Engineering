package ro.unibuc.prodeng.response;

import java.math.BigDecimal;

public record InventoryItemResponse(
        String id,
        String name,
        String unit,
        BigDecimal quantityInStock,
        BigDecimal reorderThreshold,
        boolean lowStock,
        String supplierId,
        String supplierName
) {}
