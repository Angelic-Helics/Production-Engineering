package ro.unibuc.prodeng.e2e.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import ro.unibuc.prodeng.model.CustomerEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.CreateOrderRequest;
import ro.unibuc.prodeng.request.UpdateOrderStatusRequest;
import ro.unibuc.prodeng.response.OrderResponse;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OrderSteps {

    private static final String BASE_URL = "http://localhost:8080";

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<String> latestResponse;
    private final List<String> createdCustomerIds = new ArrayList<>();
    private final List<String> createdOrderIds = new ArrayList<>();
    private String lastCreatedOrderId;

    @After
    public void cleanup() {
        for (String orderId : createdOrderIds) {
            try {
                restTemplate.delete(BASE_URL + "/api/orders/" + orderId);
            } catch (Exception ignored) {
            }
        }
        createdOrderIds.clear();

        for (String customerId : createdCustomerIds) {
            try {
                restTemplate.delete(BASE_URL + "/api/customers/" + customerId);
            } catch (Exception ignored) {
            }
        }
        createdCustomerIds.clear();
    }

    @Given("a customer named {word} with email {word} and phone {word}")
    public void createCustomer(String name, String email, String phoneNumber) throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(name, email, phoneNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateCustomerRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(BASE_URL + "/api/customers", entity, String.class);
        CustomerEntity customer = objectMapper.readValue(response.getBody(), CustomerEntity.class);
        createdCustomerIds.add(customer.id());
    }

    @When("the client creates an order {string} with quantity {int} for {word}")
    public void createOrder(String itemName, int quantity, String email) throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(itemName, quantity, email, null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(request, headers);

        latestResponse = restTemplate.postForEntity(BASE_URL + "/api/orders", entity, String.class);
        OrderResponse order = objectMapper.readValue(latestResponse.getBody(), OrderResponse.class);
        createdOrderIds.add(order.id());
        lastCreatedOrderId = order.id();
    }

    @Then("the client receives status code of {int}")
    public void verifyStatusCode(int statusCode) {
        assertThat("status code is incorrect", latestResponse.getStatusCode().value(), is(statusCode));
    }

    @Then("the client can retrieve {int} order(s) for {word}")
    public void verifyOrderCount(int count, String email) throws Exception {
        String url = BASE_URL + "/api/orders?customerEmail=" + email;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<OrderResponse> orders =
                objectMapper.readValue(response.getBody(), new TypeReference<List<OrderResponse>>() {});
        assertThat("order count is incorrect", orders.size(), is(count));
    }

    @When("the client marks the order as ready")
    public void markOrderAsReady() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.READY);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateOrderStatusRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(
                BASE_URL + "/api/orders/" + lastCreatedOrderId + "/status",
                HttpMethod.PATCH,
                entity,
                String.class
        );
    }

    @Then("the order {string} for {word} is marked as ready")
    public void verifyOrderIsReady(String itemName, String email) throws Exception {
        String url = BASE_URL + "/api/orders?customerEmail=" + email;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<OrderResponse> orders =
                objectMapper.readValue(response.getBody(), new TypeReference<List<OrderResponse>>() {});
        OrderResponse order = orders.stream()
                .filter(current -> current.itemName().equals(itemName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Order not found: " + itemName));

        assertThat("order should be marked as ready", order.status(), is(OrderStatus.READY));
    }
}
