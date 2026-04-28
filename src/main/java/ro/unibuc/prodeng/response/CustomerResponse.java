package ro.unibuc.prodeng.response;

public record CustomerResponse(
        String id,
        String name,
        String email,
        String phoneNumber
) {}
