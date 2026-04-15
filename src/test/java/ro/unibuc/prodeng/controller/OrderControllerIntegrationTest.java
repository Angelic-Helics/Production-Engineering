package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.repository.CustomerRepository;
import ro.unibuc.prodeng.repository.OrderRepository;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.CreateOrderRequest;
import ro.unibuc.prodeng.request.UpdateOrderStatusRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    private void createCustomer(String name, String email, String phoneNumber) throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(name, email, phoneNumber);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String createOrder(String itemName, int quantity, String customerEmail, String notes) throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(itemName, quantity, customerEmail, notes);

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemName").value(itemName))
                .andExpect(jsonPath("$.quantity").value(quantity))
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()))
                .andExpect(jsonPath("$.customerEmail").value(customerEmail))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetOrder_validOrderCreation_retrievesOrderSuccessfully() throws Exception {
        createCustomer("Alice", "alice@example.com", "+40111111111");
        String orderId = createOrder("Margherita Pizza", 2, "alice@example.com", "Extra basil");

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("Margherita Pizza"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.customerEmail").value("alice@example.com"));
    }

    @Test
    void testGetOrdersByCustomer_multipleCustomers_filtersCorrectly() throws Exception {
        createCustomer("Alice", "alice@example.com", "+40111111111");
        createCustomer("Bob", "bob@example.com", "+40222222222");
        createOrder("Burger", 1, "alice@example.com", null);
        createOrder("Pasta", 2, "alice@example.com", null);
        createOrder("Salad", 1, "bob@example.com", null);

        mockMvc.perform(get("/api/orders").param("customerEmail", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/orders").param("customerEmail", "bob@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUpdateStatus_validTransition_updatesStatusSuccessfully() throws Exception {
        createCustomer("Alice", "alice@example.com", "+40111111111");
        String orderId = createOrder("Soup", 1, "alice@example.com", "No croutons");

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.PREPARING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.PREPARING.name()));
    }

    @Test
    void testUpdateStatus_afterCompletion_returnsBadRequest() throws Exception {
        createCustomer("Alice", "alice@example.com", "+40111111111");
        String orderId = createOrder("Risotto", 1, "alice@example.com", null);

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.COMPLETED))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.CANCELLED))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDeleteOrder_existingOrder_deletesSuccessfully() throws Exception {
        createCustomer("Alice", "alice@example.com", "+40111111111");
        String orderId = createOrder("Burger", 1, "alice@example.com", null);

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders").param("customerEmail", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateOrder_nonExistingCustomer_returnsNotFound() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("Burger", 1, "ghost@example.com", null);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
