package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCustomerRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9+()\\-\\s]{7,20}$",
                message = "Phone number must contain only digits and phone characters")
        String phoneNumber
) {}
