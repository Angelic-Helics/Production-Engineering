package ro.unibuc.prodeng.response;

import java.math.BigDecimal;
import java.util.List;

public record MenuItemResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        boolean available,
        List<IngredientRequirementResponse> recipe
) {}
