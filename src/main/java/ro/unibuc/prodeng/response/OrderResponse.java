package ro.unibuc.prodeng.response;

import ro.unibuc.prodeng.model.OrderStatus;

public record OrderResponse(
        String id,
        String itemName,
        int quantity,
        String specialInstructions,
        OrderStatus status,
        String customerName,
        String customerEmail
) {}
