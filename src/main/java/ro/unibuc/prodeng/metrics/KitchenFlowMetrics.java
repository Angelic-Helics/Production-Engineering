package ro.unibuc.prodeng.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import ro.unibuc.prodeng.model.OrderStatus;

@Component
public class KitchenFlowMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter customersCreated;
    private final Counter customersUpdated;
    private final Counter customersDeleted;
    private final Counter ordersCreated;
    private final Counter ordersDeleted;

    public KitchenFlowMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
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
}
