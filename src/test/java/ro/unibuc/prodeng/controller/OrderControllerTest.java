package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.request.CreateOrderRequest;
import ro.unibuc.prodeng.request.UpdateOrderStatusRequest;
import ro.unibuc.prodeng.response.OrderResponse;
import ro.unibuc.prodeng.service.OrderService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OrderResponse order =
            new OrderResponse("o1", "Burger", 2, "Extra sauce", OrderStatus.CREATED, "Alice", "alice@example.com");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetOrdersByCustomerEmail_returnsOrders() throws Exception {
        when(orderService.getOrdersByCustomerEmail("alice@example.com")).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders").param("customerEmail", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].itemName", is("Burger")));

        verify(orderService, times(1)).getOrdersByCustomerEmail("alice@example.com");
    }

    @Test
    void testGetOrderById_returnsOrder() throws Exception {
        when(orderService.getOrderById("o1")).thenReturn(order);

        mockMvc.perform(get("/api/orders/{id}", "o1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("o1")))
                .andExpect(jsonPath("$.customerEmail", is("alice@example.com")));

        verify(orderService, times(1)).getOrderById("o1");
    }

    @Test
    void testGetOrderById_missingOrder_returnsNotFound() throws Exception {
        when(orderService.getOrderById("missing")).thenThrow(new EntityNotFoundException("missing"));

        mockMvc.perform(get("/api/orders/{id}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateOrder_returnsCreatedOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("Burger", 2, "alice@example.com", "Extra sauce");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemName", is("Burger")))
                .andExpect(jsonPath("$.status", is(OrderStatus.CREATED.name())));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void testCreateOrder_missingCustomer_returnsNotFound() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("Burger", 2, "ghost@example.com", null);
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new EntityNotFoundException("ghost@example.com"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateOrder_invalidRequest_returnsBadRequest() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("", 0, "not-an-email", null);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testUpdateStatus_returnsUpdatedOrder() throws Exception {
        when(orderService.updateStatus(eq("o1"), any(UpdateOrderStatusRequest.class))).thenReturn(
                new OrderResponse("o1", "Burger", 2, "Extra sauce", OrderStatus.PREPARING, "Alice", "alice@example.com"));

        mockMvc.perform(patch("/api/orders/{id}/status", "o1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.PREPARING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(OrderStatus.PREPARING.name())));

        verify(orderService, times(1)).updateStatus(eq("o1"), any(UpdateOrderStatusRequest.class));
    }

    @Test
    void testUpdateStatus_invalidTransition_returnsBadRequest() throws Exception {
        when(orderService.updateStatus(eq("o1"), any(UpdateOrderStatusRequest.class)))
                .thenThrow(new IllegalArgumentException("Cannot change order status from COMPLETED to CANCELLED"));

        mockMvc.perform(patch("/api/orders/{id}/status", "o1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.CANCELLED))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDeleteOrder_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", "o1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder("o1");
    }

    @Test
    void testDeleteOrder_missingOrder_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("o1")).when(orderService).deleteOrder("o1");

        mockMvc.perform(delete("/api/orders/{id}", "o1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
