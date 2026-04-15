package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AssignTodoRequest(
    @Email(message = "Invalid email format")
    @NotBlank(message = "New assignee email is required")
    String newAssigneeEmail
) {}

