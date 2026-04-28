package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.IngredientRequirement;
import ro.unibuc.prodeng.model.InventoryItemEntity;
import ro.unibuc.prodeng.model.MenuItemEntity;
import ro.unibuc.prodeng.model.SupplierEntity;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.MenuItemRepository;
import ro.unibuc.prodeng.request.CreateInventoryItemRequest;
import ro.unibuc.prodeng.request.RestockInventoryItemRequest;
import ro.unibuc.prodeng.response.InventoryItemResponse;

@ExtendWith(SpringExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void testGetInventoryItems_lowStockFilter_returnsOnlyLowStockItems() {
        SupplierEntity supplier = new SupplierEntity("sup-1", "Bianca", "Fresh Farm Supply",
                "bianca@freshfarm.ro", "+40700111222");
        InventoryItemEntity lowStock = new InventoryItemEntity("1", "Mozzarella", "kg",
                new BigDecimal("1.50"), new BigDecimal("2.00"), "sup-1");
        InventoryItemEntity healthyStock = new InventoryItemEntity("2", "Tomatoes", "kg",
                new BigDecimal("5.00"), new BigDecimal("2.00"), "sup-1");

        when(inventoryItemRepository.findAll()).thenReturn(List.of(lowStock, healthyStock));
        when(supplierService.getSupplierEntityById("sup-1")).thenReturn(supplier);

        List<InventoryItemResponse> result = inventoryService.getInventoryItems(true, null);

        assertEquals(1, result.size());
        assertEquals("Mozzarella", result.get(0).name());
        assertTrue(result.get(0).lowStock());
    }

    @Test
    void testGetInventoryItems_supplierFilter_returnsSupplierItems() {
        SupplierEntity supplier = new SupplierEntity("sup-1", "Bianca", "Fresh Farm Supply",
                "bianca@freshfarm.ro", "+40700111222");
        InventoryItemEntity item = new InventoryItemEntity("1", "Mozzarella", "kg",
                new BigDecimal("4.50"), new BigDecimal("2.00"), "sup-1");

        when(inventoryItemRepository.findBySupplierId("sup-1")).thenReturn(List.of(item));
        when(supplierService.getSupplierEntityById("sup-1")).thenReturn(supplier);

        List<InventoryItemResponse> result = inventoryService.getInventoryItems(null, "sup-1");

        assertEquals(1, result.size());
        assertEquals("Fresh Farm Supply", result.get(0).supplierName());
    }

    @Test
    void testCreateInventoryItem_validRequest_createsMappedItem() {
        SupplierEntity supplier = new SupplierEntity("sup-1", "Bianca", "Fresh Farm Supply",
                "bianca@freshfarm.ro", "+40700111222");
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                "Mozzarella",
                "KG",
                new BigDecimal("4.50"),
                new BigDecimal("2.00"),
                "sup-1"
        );

        when(supplierService.getSupplierEntityById("sup-1")).thenReturn(supplier);
        when(inventoryItemRepository.save(any(InventoryItemEntity.class))).thenAnswer(invocation -> {
            InventoryItemEntity entity = invocation.getArgument(0);
            return new InventoryItemEntity("generated-id", entity.name(), entity.unit(),
                    entity.quantityInStock(), entity.reorderThreshold(), entity.supplierId());
        });

        InventoryItemResponse result = inventoryService.createInventoryItem(request);

        assertEquals("generated-id", result.id());
        assertEquals("kg", result.unit());
        assertFalse(result.lowStock());
    }

    @Test
    void testUpdateInventoryItem_existingItem_updatesFieldsAndKeepsQuantity() {
        InventoryItemEntity existing = new InventoryItemEntity("1", "Mozzarella", "kg",
                new BigDecimal("4.50"), new BigDecimal("2.00"), "sup-1");
        SupplierEntity supplier = new SupplierEntity("sup-2", "Alex", "Market Greens",
                "alex@greens.ro", "+40700333444");
        when(inventoryItemRepository.findById("1")).thenReturn(Optional.of(existing));
        when(supplierService.getSupplierEntityById("sup-2")).thenReturn(supplier);
        when(inventoryItemRepository.save(any(InventoryItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItemResponse result = inventoryService.updateInventoryItem(
                "1",
                new ro.unibuc.prodeng.request.UpdateInventoryItemRequest(
                        "Aged Mozzarella",
                        " KG ",
                        new BigDecimal("1.50"),
                        "sup-2"
                )
        );

        assertEquals("Aged Mozzarella", result.name());
        assertEquals("kg", result.unit());
        assertEquals(new BigDecimal("4.50"), result.quantityInStock());
        assertEquals("sup-2", result.supplierId());
    }

    @Test
    void testRestockInventoryItem_existingItem_addsQuantity() {
        InventoryItemEntity existing = new InventoryItemEntity("1", "Mozzarella", "kg",
                new BigDecimal("4.50"), new BigDecimal("2.00"), "sup-1");
        SupplierEntity supplier = new SupplierEntity("sup-1", "Bianca", "Fresh Farm Supply",
                "bianca@freshfarm.ro", "+40700111222");

        when(inventoryItemRepository.findById("1")).thenReturn(Optional.of(existing));
        when(inventoryItemRepository.save(any(InventoryItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(supplierService.getSupplierEntityById("sup-1")).thenReturn(supplier);

        InventoryItemResponse result = inventoryService.restockInventoryItem(
                "1",
                new RestockInventoryItemRequest(new BigDecimal("3.00"))
        );

        assertEquals(new BigDecimal("7.50"), result.quantityInStock());
    }

    @Test
    void testRestockInventoryItem_missingItem_throwsEntityNotFoundException() {
        when(inventoryItemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> inventoryService.restockInventoryItem(
                "missing",
                new RestockInventoryItemRequest(new BigDecimal("3.00"))
        ));
    }

    @Test
    void testDeleteInventoryItem_usedByMenu_throwsIllegalArgumentException() {
        when(inventoryItemRepository.existsById("1")).thenReturn(true);
        when(menuItemRepository.findAll()).thenReturn(List.of(
                new MenuItemEntity("menu-1", "Pizza", "Classic pizza", new BigDecimal("39.99"),
                        List.of(new IngredientRequirement("1", new BigDecimal("0.30"))))
        ));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> inventoryService.deleteInventoryItem("1"));

        assertTrue(exception.getMessage().contains("used by at least one menu item"));
        verify(inventoryItemRepository, never()).deleteById("1");
    }

    @Test
    void testGetInventoryItemById_missingItem_throwsEntityNotFoundException() {
        when(inventoryItemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> inventoryService.getInventoryItemById("missing"));
    }

    @Test
    void testDeleteInventoryItem_existingUnusedItem_deletesItem() {
        when(inventoryItemRepository.existsById("1")).thenReturn(true);
        when(menuItemRepository.findAll()).thenReturn(List.of());

        inventoryService.deleteInventoryItem("1");

        verify(inventoryItemRepository).deleteById("1");
    }
}
