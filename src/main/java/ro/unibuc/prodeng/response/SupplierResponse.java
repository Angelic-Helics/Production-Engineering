package ro.unibuc.prodeng.response;

public record SupplierResponse(
        String id,
        String name,
        String companyName,
        String email,
        String phoneNumber
) {}
