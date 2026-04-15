package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "todos")
public record TodoEntity(
    @Id String id,
    String description,
    boolean done,
    String assignedUserId
) {}
