package ro.unibuc.prodeng.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.InventoryItemEntity;
import ro.unibuc.prodeng.model.MenuItemEntity;
import ro.unibuc.prodeng.model.SupplierEntity;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.MenuItemRepository;
import ro.unibuc.prodeng.request.CreateInventoryItemRequest;
import ro.unibuc.prodeng.request.RestockInventoryItemRequest;
import ro.unibuc.prodeng.request.UpdateInventoryItemRequest;
import ro.unibuc.prodeng.response.InventoryItemResponse;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private SupplierService supplierService;

    public List<InventoryItemResponse> getInventoryItems(Boolean lowStock, String supplierId) {
        List<InventoryItemEntity> items = supplierId == null || supplierId.isBlank()
                ? inventoryItemRepository.findAll()
                : inventoryItemRepository.findBySupplierId(supplierId);

        return items.stream()
                .filter(item -> lowStock == null || isLowStock(item) == lowStock)
                .map(this::toResponse)
                .toList();
    }

    public InventoryItemResponse getInventoryItemById(String id) {
        return toResponse(getInventoryItemEntityById(id));
    }

    public InventoryItemEntity getInventoryItemEntityById(String id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    public InventoryItemResponse createInventoryItem(CreateInventoryItemRequest request) {
        SupplierEntity supplier = supplierService.getSupplierEntityById(request.supplierId());

        InventoryItemEntity item = new InventoryItemEntity(
                null,
                request.name(),
                normalizeUnit(request.unit()),
                request.quantityInStock(),
                request.reorderThreshold(),
                supplier.id()
        );

        return toResponse(inventoryItemRepository.save(item), supplier);
    }

    public InventoryItemResponse updateInventoryItem(String id, UpdateInventoryItemRequest request) {
        InventoryItemEntity existing = getInventoryItemEntityById(id);
        SupplierEntity supplier = supplierService.getSupplierEntityById(request.supplierId());

        InventoryItemEntity updated = new InventoryItemEntity(
                existing.id(),
                request.name(),
                normalizeUnit(request.unit()),
                existing.quantityInStock(),
                request.reorderThreshold(),
                supplier.id()
        );

        return toResponse(inventoryItemRepository.save(updated), supplier);
    }

    public InventoryItemResponse restockInventoryItem(String id, RestockInventoryItemRequest request) {
        InventoryItemEntity existing = getInventoryItemEntityById(id);
        InventoryItemEntity updated = new InventoryItemEntity(
                existing.id(),
                existing.name(),
                existing.unit(),
                existing.quantityInStock().add(request.quantity()),
                existing.reorderThreshold(),
                existing.supplierId()
        );

        return toResponse(inventoryItemRepository.save(updated));
    }

    public void deleteInventoryItem(String id) {
        if (!inventoryItemRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }

        boolean usedInMenu = menuItemRepository.findAll().stream()
                .map(MenuItemEntity::recipe)
                .flatMap(List::stream)
                .anyMatch(ingredient -> ingredient.inventoryItemId().equals(id));

        if (usedInMenu) {
            throw new IllegalArgumentException("Inventory item is used by at least one menu item: " + id);
        }

        inventoryItemRepository.deleteById(id);
    }

    private String normalizeUnit(String unit) {
        return unit.trim().toLowerCase();
    }

    private boolean isLowStock(InventoryItemEntity item) {
        return item.quantityInStock().compareTo(item.reorderThreshold()) <= 0;
    }

    private InventoryItemResponse toResponse(InventoryItemEntity item) {
        SupplierEntity supplier = supplierService.getSupplierEntityById(item.supplierId());
        return toResponse(item, supplier);
    }

    private InventoryItemResponse toResponse(InventoryItemEntity item, SupplierEntity supplier) {
        return new InventoryItemResponse(
                item.id(),
                item.name(),
                item.unit(),
                item.quantityInStock(),
                item.reorderThreshold(),
                isLowStock(item),
                supplier.id(),
                supplier.companyName()
        );
    }
}
