package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
public record CustomerEntity(
        @Id
        String id,
        String name,
        String email,
        String phoneNumber
) {}
