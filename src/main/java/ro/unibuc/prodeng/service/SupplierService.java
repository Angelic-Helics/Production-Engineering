package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.SupplierEntity;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.SupplierRepository;
import ro.unibuc.prodeng.request.CreateSupplierRequest;
import ro.unibuc.prodeng.request.UpdateSupplierRequest;
import ro.unibuc.prodeng.response.SupplierResponse;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SupplierResponse getSupplierById(String id) {
        return toResponse(getSupplierEntityById(id));
    }

    public SupplierEntity getSupplierEntityById(String id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        if (supplierRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Supplier email already exists: " + request.email());
        }

        SupplierEntity supplier = new SupplierEntity(
                null,
                request.name(),
                request.companyName(),
                request.email(),
                request.phoneNumber()
        );

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse updateSupplier(String id, UpdateSupplierRequest request) {
        SupplierEntity existing = getSupplierEntityById(id);
        supplierRepository.findByEmail(request.email())
                .filter(supplier -> !supplier.id().equals(id))
                .ifPresent(supplier -> {
                    throw new IllegalArgumentException("Supplier email already exists: " + request.email());
                });

        SupplierEntity updated = new SupplierEntity(
                existing.id(),
                request.name(),
                request.companyName(),
                request.email(),
                request.phoneNumber()
        );

        return toResponse(supplierRepository.save(updated));
    }

    public void deleteSupplier(String id) {
        if (!supplierRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }

        if (!inventoryItemRepository.findBySupplierId(id).isEmpty()) {
            throw new IllegalArgumentException("Supplier is still assigned to inventory items: " + id);
        }

        supplierRepository.deleteById(id);
    }

    private SupplierResponse toResponse(SupplierEntity supplier) {
        return new SupplierResponse(
                supplier.id(),
                supplier.name(),
                supplier.companyName(),
                supplier.email(),
                supplier.phoneNumber()
        );
    }
}
