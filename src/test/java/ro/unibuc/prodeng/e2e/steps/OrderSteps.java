package ro.unibuc.prodeng.e2e.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.CreateOrderRequest;
import ro.unibuc.prodeng.request.UpdateOrderStatusRequest;
import ro.unibuc.prodeng.response.CustomerResponse;
import ro.unibuc.prodeng.response.OrderResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OrderSteps {

    private static final String BASE_URL = System.getenv()
            .getOrDefault("E2E_BASE_URL", System.getProperty("e2e.base-url", "http://localhost:8080"));

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<String> latestResponse;
    private final List<String> createdCustomerIds = new ArrayList<>();
    private final List<String> createdOrderIds = new ArrayList<>();
    private final Map<String, String> scenarioEmails = new HashMap<>();
    private String lastCreatedOrderId;

    @ParameterType("[^\\s]+@[^\\s]+")
    public String email(String email) {
        return email;
    }

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
        scenarioEmails.clear();
    }

    @Given("a customer named {word} with email {email} and phone {word}")
    public void createCustomer(String name, String email, String phoneNumber) throws Exception {
        String scenarioEmail = scenarioEmail(email);
        CreateCustomerRequest request = new CreateCustomerRequest(name, scenarioEmail, phoneNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateCustomerRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(BASE_URL + "/api/customers", entity, String.class);
        assertThat("customer should be created", response.getStatusCode().value(), is(201));

        CustomerResponse customer = objectMapper.readValue(response.getBody(), CustomerResponse.class);
        createdCustomerIds.add(customer.id());
    }

    @When("the client creates an order {string} with quantity {int} for {email}")
    public void createOrder(String itemName, int quantity, String email) throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(itemName, quantity, scenarioEmail(email), null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(request, headers);

        latestResponse = restTemplate.postForEntity(BASE_URL + "/api/orders", entity, String.class);
        assertThat("order should be created", latestResponse.getStatusCode().value(), is(201));

        OrderResponse order = objectMapper.readValue(latestResponse.getBody(), OrderResponse.class);
        createdOrderIds.add(order.id());
        lastCreatedOrderId = order.id();
    }

    @Then("the client receives status code of {int}")
    public void verifyStatusCode(int statusCode) {
        assertThat("status code is incorrect", latestResponse.getStatusCode().value(), is(statusCode));
    }

    @Then("^the client can retrieve (\\d+) order\\(s\\) for ([^\\s]+@[^\\s]+)$")
    public void verifyOrderCount(int count, String email) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(ordersUri(email), String.class);

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

    @Then("the order {string} for {email} is marked as ready")
    public void verifyOrderIsReady(String itemName, String email) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(ordersUri(email), String.class);

        List<OrderResponse> orders =
                objectMapper.readValue(response.getBody(), new TypeReference<List<OrderResponse>>() {});
        OrderResponse order = orders.stream()
                .filter(current -> current.itemName().equals(itemName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Order not found: " + itemName));

        assertThat("order should be marked as ready", order.status(), is(OrderStatus.READY));
    }

    private java.net.URI ordersUri(String email) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/orders")
                .queryParam("customerEmail", scenarioEmail(email))
                .build()
                .encode()
                .toUri();
    }

    private String scenarioEmail(String email) {
        return scenarioEmails.computeIfAbsent(email, this::uniqueEmail);
    }

    private String uniqueEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex < 0) {
            return email;
        }

        return email.substring(0, atIndex)
                + "."
                + System.currentTimeMillis()
                + "@"
                + email.substring(atIndex + 1);
    }
}
