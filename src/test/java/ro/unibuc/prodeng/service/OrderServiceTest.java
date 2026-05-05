package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.metrics.KitchenFlowMetrics;
import ro.unibuc.prodeng.model.CustomerEntity;
import ro.unibuc.prodeng.model.OrderEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.repository.OrderRepository;
import ro.unibuc.prodeng.request.CreateOrderRequest;
import ro.unibuc.prodeng.request.UpdateOrderStatusRequest;
import ro.unibuc.prodeng.response.OrderResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private KitchenFlowMetrics kitchenFlowMetrics;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testGetOrdersByCustomerEmail_existingCustomer_returnsOrders() {
        CustomerEntity customer = new CustomerEntity("c1", "Alice", "alice@example.com", "+40111111111");
        OrderEntity order = new OrderEntity("o1", "Burger", 2, "Extra sauce", OrderStatus.CREATED, "c1");

        when(customerService.getCustomerEntityByEmail("alice@example.com")).thenReturn(customer);
        when(orderRepository.findByCustomerId("c1")).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getOrdersByCustomerEmail("alice@example.com");

        assertEquals(1, result.size());
        assertEquals("Burger", result.get(0).itemName());
        assertEquals("Alice", result.get(0).customerName());
    }

    @Test
    void testGetOrdersByCustomerEmail_missingCustomer_throwsEntityNotFoundException() {
        when(customerService.getCustomerEntityByEmail("ghost@example.com"))
                .thenThrow(new EntityNotFoundException("ghost@example.com"));

        assertThrows(EntityNotFoundException.class,
                () -> orderService.getOrdersByCustomerEmail("ghost@example.com"));
    }

    @Test
    void testGetOrderById_existingOrder_returnsOrder() {
        OrderEntity order = new OrderEntity("o1", "Pizza", 1, "No onions", OrderStatus.CREATED, "c1");
        CustomerEntity customer = new CustomerEntity("c1", "Alice", "alice@example.com", "+40111111111");

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(customerService.getCustomerEntityById("c1")).thenReturn(customer);

        OrderResponse result = orderService.getOrderById("o1");

        assertEquals("Pizza", result.itemName());
        assertEquals("Alice", result.customerName());
        assertEquals(OrderStatus.CREATED, result.status());
    }

    @Test
    void testGetOrderById_missingOrder_throwsEntityNotFoundException() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById("missing"));
    }

    @Test
    void testGetOrderById_missingCustomer_throwsEntityNotFoundException() {
        OrderEntity order = new OrderEntity("o1", "Pizza", 1, "No onions", OrderStatus.CREATED, "c1");

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(customerService.getCustomerEntityById("c1")).thenThrow(new EntityNotFoundException("c1"));

        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById("o1"));
    }

    @Test
    void testCreateOrder_withTrimmedInstructions_createsOrder() {
        CustomerEntity customer = new CustomerEntity("c1", "Alice", "alice@example.com", "+40111111111");
        CreateOrderRequest request = new CreateOrderRequest("Burger", 2, "alice@example.com", "  Extra sauce  ");

        when(customerService.getCustomerEntityByEmail("alice@example.com")).thenReturn(customer);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = orderService.createOrder(request);

        assertEquals("Burger", result.itemName());
        assertEquals("Extra sauce", result.specialInstructions());
        assertEquals(OrderStatus.CREATED, result.status());
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(kitchenFlowMetrics, times(1)).recordOrderCreated();
    }

    @Test
    void testCreateOrder_withBlankInstructions_returnsNullInstructions() {
        CustomerEntity customer = new CustomerEntity("c1", "Alice", "alice@example.com", "+40111111111");
        CreateOrderRequest request = new CreateOrderRequest("Burger", 2, "alice@example.com", "   ");

        when(customerService.getCustomerEntityByEmail("alice@example.com")).thenReturn(customer);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = orderService.createOrder(request);

        assertNull(result.specialInstructions());
    }

    @Test
    void testCreateOrder_withNullInstructions_returnsNullInstructions() {
        CustomerEntity customer = new CustomerEntity("c1", "Alice", "alice@example.com", "+40111111111");
        CreateOrderRequest request = new CreateOrderRequest("Burger", 2, "alice@example.com", null);

        when(customerService.getCustomerEntityByEmail("alice@example.com")).thenReturn(customer);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = orderService.createOrder(request);

        assertNull(result.specialInstructions());
    }

    @Test
    void testUpdateStatus_validTransition_updatesStatus() {
        OrderEntity existing = new OrderEntity("o1", "Burger", 1, "No onions", OrderStatus.CREATED, "c1");
        CustomerEntity customer = new CustomerEntity("c1", "Alice", "alice@example.com", "+40111111111");

        when(orderRepository.findById("o1")).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerService.getCustomerEntityById("c1")).thenReturn(customer);

        OrderResponse result = orderService.updateStatus("o1", new UpdateOrderStatusRequest(OrderStatus.PREPARING));

        assertEquals(OrderStatus.PREPARING, result.status());
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(kitchenFlowMetrics, times(1)).recordOrderStatusUpdated(OrderStatus.PREPARING);
    }

    @Test
    void testUpdateStatus_sameStatus_keepsStatus() {
        OrderEntity existing = new OrderEntity("o1", "Burger", 1, "No onions", OrderStatus.CREATED, "c1");
        CustomerEntity customer = new CustomerEntity("c1", "Alice", "alice@example.com", "+40111111111");

        when(orderRepository.findById("o1")).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerService.getCustomerEntityById("c1")).thenReturn(customer);

        OrderResponse result = orderService.updateStatus("o1", new UpdateOrderStatusRequest(OrderStatus.CREATED));

        assertEquals(OrderStatus.CREATED, result.status());
    }

    @Test
    void testUpdateStatus_fromCompletedToCancelled_throwsIllegalArgumentException() {
        OrderEntity existing = new OrderEntity("o1", "Burger", 1, null, OrderStatus.COMPLETED, "c1");
        when(orderRepository.findById("o1")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateStatus("o1", new UpdateOrderStatusRequest(OrderStatus.CANCELLED)));
    }

    @Test
    void testUpdateStatus_fromCancelledToPreparing_throwsIllegalArgumentException() {
        OrderEntity existing = new OrderEntity("o1", "Burger", 1, null, OrderStatus.CANCELLED, "c1");
        when(orderRepository.findById("o1")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateStatus("o1", new UpdateOrderStatusRequest(OrderStatus.PREPARING)));
    }

    @Test
    void testUpdateStatus_missingOrder_throwsEntityNotFoundException() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.updateStatus("missing", new UpdateOrderStatusRequest(OrderStatus.PREPARING)));
    }

    @Test
    void testCreateOrder_missingCustomer_throwsEntityNotFoundException() {
        when(customerService.getCustomerEntityByEmail("ghost@example.com"))
                .thenThrow(new EntityNotFoundException("ghost@example.com"));

        assertThrows(EntityNotFoundException.class, () ->
                orderService.createOrder(new CreateOrderRequest("Burger", 2, "ghost@example.com", null)));
    }

    @Test
    void testDeleteOrder_existingOrder_deletesOrder() {
        when(orderRepository.existsById("o1")).thenReturn(true);

        orderService.deleteOrder("o1");

        verify(orderRepository, times(1)).deleteById("o1");
        verify(kitchenFlowMetrics, times(1)).recordOrderDeleted();
    }

    @Test
    void testDeleteOrder_missingOrder_throwsEntityNotFoundException() {
        when(orderRepository.existsById("missing")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> orderService.deleteOrder("missing"));
    }
}
