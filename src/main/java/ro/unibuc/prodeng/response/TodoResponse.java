package ro.unibuc.prodeng.response;

public record TodoResponse(
    String id,
    String description,
    boolean done,
    String assigneeName,
    String assigneeEmail
) {}
