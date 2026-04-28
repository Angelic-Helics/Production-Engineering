package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.CustomerEntity;
import ro.unibuc.prodeng.model.OrderEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.repository.OrderRepository;
import ro.unibuc.prodeng.request.CreateOrderRequest;
import ro.unibuc.prodeng.request.UpdateOrderStatusRequest;
import ro.unibuc.prodeng.response.OrderResponse;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerService customerService;

    public List<OrderResponse> getOrdersByCustomerEmail(String customerEmail) {
        CustomerEntity customer = customerService.getCustomerEntityByEmail(customerEmail);
        return orderRepository.findByCustomerId(customer.id()).stream()
                .map(order -> toResponse(order, customer))
                .toList();
    }

    public OrderResponse getOrderById(String id) {
        OrderEntity order = getOrderEntityById(id);
        CustomerEntity customer = customerService.getCustomerEntityById(order.customerId());
        return toResponse(order, customer);
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        CustomerEntity customer = customerService.getCustomerEntityByEmail(request.customerEmail());
        OrderEntity order = new OrderEntity(
                null,
                request.itemName(),
                request.quantity(),
                normalizeInstructions(request.specialInstructions()),
                OrderStatus.CREATED,
                customer.id()
        );

        return toResponse(orderRepository.save(order), customer);
    }

    public OrderResponse updateStatus(String id, UpdateOrderStatusRequest request) {
        OrderEntity existing = getOrderEntityById(id);
        validateStatusTransition(existing.status(), request.status());

        OrderEntity updated = new OrderEntity(
                existing.id(),
                existing.itemName(),
                existing.quantity(),
                existing.specialInstructions(),
                request.status(),
                existing.customerId()
        );

        OrderEntity saved = orderRepository.save(updated);
        CustomerEntity customer = customerService.getCustomerEntityById(saved.customerId());
        return toResponse(saved, customer);
    }

    public void deleteOrder(String id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }

        orderRepository.deleteById(id);
    }

    private OrderEntity getOrderEntityById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus requestedStatus) {
        if (currentStatus == requestedStatus) {
            return;
        }

        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot change order status from " + currentStatus + " to " + requestedStatus);
        }
    }

    private String normalizeInstructions(String instructions) {
        if (instructions == null || instructions.isBlank()) {
            return null;
        }

        return instructions.trim();
    }

    private OrderResponse toResponse(OrderEntity order, CustomerEntity customer) {
        return new OrderResponse(
                order.id(),
                order.itemName(),
                order.quantity(),
                order.specialInstructions(),
                order.status(),
                customer.name(),
                customer.email()
        );
    }
}
