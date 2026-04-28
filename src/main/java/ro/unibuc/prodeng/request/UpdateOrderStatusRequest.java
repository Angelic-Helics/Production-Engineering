package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotNull;
import ro.unibuc.prodeng.model.OrderStatus;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}
