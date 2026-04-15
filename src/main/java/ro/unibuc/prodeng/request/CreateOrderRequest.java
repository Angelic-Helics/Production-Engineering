package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = "Item name is required")
        String itemName,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Customer email is required")
        String customerEmail,

        String specialInstructions
) {}
