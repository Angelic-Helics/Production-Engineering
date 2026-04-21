package ro.unibuc.prodeng.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateMenuItemRequest(
        @NotBlank(message = "Menu item name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than 0")
        BigDecimal price,

        @Valid
        @NotEmpty(message = "Recipe must contain at least one ingredient")
        List<IngredientRequirementRequest> recipe
) {}
