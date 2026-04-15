package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record EditTodoRequest(
    @NotBlank(message = "Description is required")
    String description
) {}
