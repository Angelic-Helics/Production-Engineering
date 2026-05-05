package ro.unibuc.prodeng.metrics;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import ro.unibuc.prodeng.model.InventoryItemEntity;
import ro.unibuc.prodeng.model.MenuItemEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.MenuItemRepository;
import ro.unibuc.prodeng.repository.SupplierRepository;

@Component
public class KitchenFlowMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter customersCreated;
    private final Counter customersUpdated;
    private final Counter customersDeleted;
    private final Counter ordersCreated;
    private final Counter ordersDeleted;
    private final Counter inventoryCreated;
    private final Counter inventoryUpdated;
    private final Counter inventoryRestocked;
    private final Counter inventoryRestockedQuantity;
    private final Counter inventoryDeleted;
    private final Counter menuCreated;
    private final Counter menuUpdated;
    private final Counter menuDeleted;
    private final Counter suppliersCreated;
    private final Counter suppliersUpdated;
    private final Counter suppliersDeleted;
    private final InventoryItemRepository inventoryItemRepository;
    private final MenuItemRepository menuItemRepository;

    public KitchenFlowMetrics(
            MeterRegistry meterRegistry,
            InventoryItemRepository inventoryItemRepository,
            MenuItemRepository menuItemRepository,
            SupplierRepository supplierRepository) {
        this.meterRegistry = meterRegistry;
        this.inventoryItemRepository = inventoryItemRepository;
        this.menuItemRepository = menuItemRepository;
        customersCreated = Counter.builder("kitchenflow.customers.created")
                .description("Number of customers created successfully")
                .register(meterRegistry);
        customersUpdated = Counter.builder("kitchenflow.customers.updated")
                .description("Number of customers updated successfully")
                .register(meterRegistry);
        customersDeleted = Counter.builder("kitchenflow.customers.deleted")
                .description("Number of customers deleted successfully")
                .register(meterRegistry);
        ordersCreated = Counter.builder("kitchenflow.orders.created")
                .description("Number of orders created successfully")
                .register(meterRegistry);
        ordersDeleted = Counter.builder("kitchenflow.orders.deleted")
                .description("Number of orders deleted successfully")
                .register(meterRegistry);
        inventoryCreated = Counter.builder("kitchenflow.inventory.created")
                .description("Number of inventory items created successfully")
                .register(meterRegistry);
        inventoryUpdated = Counter.builder("kitchenflow.inventory.updated")
                .description("Number of inventory items updated successfully")
                .register(meterRegistry);
        inventoryRestocked = Counter.builder("kitchenflow.inventory.restocked")
                .description("Number of successful inventory restock operations")
                .register(meterRegistry);
        inventoryRestockedQuantity = Counter.builder("kitchenflow.inventory.restock.quantity")
                .description("Total inventory quantity added through restock operations")
                .register(meterRegistry);
        inventoryDeleted = Counter.builder("kitchenflow.inventory.deleted")
                .description("Number of inventory items deleted successfully")
                .register(meterRegistry);
        menuCreated = Counter.builder("kitchenflow.menu.created")
                .description("Number of menu items created successfully")
                .register(meterRegistry);
        menuUpdated = Counter.builder("kitchenflow.menu.updated")
                .description("Number of menu items updated successfully")
                .register(meterRegistry);
        menuDeleted = Counter.builder("kitchenflow.menu.deleted")
                .description("Number of menu items deleted successfully")
                .register(meterRegistry);
        suppliersCreated = Counter.builder("kitchenflow.suppliers.created")
                .description("Number of suppliers created successfully")
                .register(meterRegistry);
        suppliersUpdated = Counter.builder("kitchenflow.suppliers.updated")
                .description("Number of suppliers updated successfully")
                .register(meterRegistry);
        suppliersDeleted = Counter.builder("kitchenflow.suppliers.deleted")
                .description("Number of suppliers deleted successfully")
                .register(meterRegistry);

        meterRegistry.gauge("kitchenflow.inventory.total", inventoryItemRepository, InventoryItemRepository::count);
        meterRegistry.gauge("kitchenflow.inventory.low.stock.total", inventoryItemRepository,
                repository -> countLowStockInventoryItems());
        meterRegistry.gauge("kitchenflow.menu.total", menuItemRepository, MenuItemRepository::count);
        meterRegistry.gauge("kitchenflow.menu.unavailable.total", menuItemRepository,
                repository -> countUnavailableMenuItems());
        meterRegistry.gauge("kitchenflow.suppliers.total", supplierRepository, SupplierRepository::count);
    }

    public void recordCustomerCreated() {
        customersCreated.increment();
    }

    public void recordCustomerUpdated() {
        customersUpdated.increment();
    }

    public void recordCustomerDeleted() {
        customersDeleted.increment();
    }

    public void recordOrderCreated() {
        ordersCreated.increment();
    }

    public void recordOrderStatusUpdated(OrderStatus status) {
        meterRegistry.counter("kitchenflow.orders.status.updated", "status", status.name()).increment();
    }

    public void recordOrderDeleted() {
        ordersDeleted.increment();
    }

    public void recordInventoryCreated() {
        inventoryCreated.increment();
    }

    public void recordInventoryUpdated() {
        inventoryUpdated.increment();
    }

    public void recordInventoryRestocked(BigDecimal quantityAdded) {
        inventoryRestocked.increment();
        inventoryRestockedQuantity.increment(quantityAdded.doubleValue());
    }

    public void recordInventoryDeleted() {
        inventoryDeleted.increment();
    }

    public void recordMenuCreated() {
        menuCreated.increment();
    }

    public void recordMenuUpdated() {
        menuUpdated.increment();
    }

    public void recordMenuDeleted() {
        menuDeleted.increment();
    }

    public void recordSupplierCreated() {
        suppliersCreated.increment();
    }

    public void recordSupplierUpdated() {
        suppliersUpdated.increment();
    }

    public void recordSupplierDeleted() {
        suppliersDeleted.increment();
    }

    private long countLowStockInventoryItems() {
        return inventoryItemRepository.findAll().stream()
                .filter(this::isLowStock)
                .count();
    }

    private long countUnavailableMenuItems() {
        return menuItemRepository.findAll().stream()
                .filter(item -> !isMenuItemAvailable(item))
                .count();
    }

    private boolean isMenuItemAvailable(MenuItemEntity item) {
        return item.recipe().stream().allMatch(requirement -> inventoryItemRepository.findById(requirement.inventoryItemId())
                .map(inventoryItem -> hasSufficientStock(inventoryItem, requirement.quantityRequired()))
                .orElse(false));
    }

    private boolean hasSufficientStock(InventoryItemEntity inventoryItem, BigDecimal quantityRequired) {
        return inventoryItem.quantityInStock().compareTo(quantityRequired) >= 0;
    }

    private boolean isLowStock(InventoryItemEntity inventoryItem) {
        return inventoryItem.quantityInStock().compareTo(inventoryItem.reorderThreshold()) <= 0;
    }
}
