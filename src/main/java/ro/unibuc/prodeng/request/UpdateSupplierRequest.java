package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateSupplierRequest(
        @NotBlank(message = "Contact name is required")
        String name,

        @NotBlank(message = "Company name is required")
        String companyName,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9+()\\-\\s]{7,20}$",
                message = "Phone number must contain only digits and phone characters")
        String phoneNumber
) {}
