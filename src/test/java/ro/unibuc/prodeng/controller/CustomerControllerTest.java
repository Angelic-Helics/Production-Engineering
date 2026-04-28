package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.UpdateCustomerRequest;
import ro.unibuc.prodeng.response.CustomerResponse;
import ro.unibuc.prodeng.service.CustomerService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.InjectMocks;
import org.mockito.Mock;

@ExtendWith(SpringExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CustomerResponse customer1 =
            new CustomerResponse("1", "John Doe", "john@example.com", "+40111111111");
    private final CustomerResponse customer2 =
            new CustomerResponse("2", "Jane Smith", "jane@example.com", "+40222222222");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllCustomers_returnsListOfCustomers() throws Exception {
        List<CustomerResponse> customers = Arrays.asList(customer1, customer2);
        when(customerService.getAllCustomers()).thenReturn(customers);

        mockMvc.perform(get("/api/customers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].email", is("jane@example.com")));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void testGetCustomerById_existingCustomer_returnsCustomer() throws Exception {
        when(customerService.getCustomerById("1")).thenReturn(customer1);

        mockMvc.perform(get("/api/customers/{id}", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.phoneNumber", is("+40111111111")));

        verify(customerService, times(1)).getCustomerById("1");
    }

    @Test
    void testGetCustomerById_missingCustomer_returnsNotFound() throws Exception {
        when(customerService.getCustomerById("999")).thenThrow(new EntityNotFoundException("999"));

        mockMvc.perform(get("/api/customers/{id}", "999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(customerService, times(1)).getCustomerById("999");
    }

    @Test
    void testGetCustomerByEmail_existingCustomer_returnsCustomer() throws Exception {
        when(customerService.getCustomerByEmail("john@example.com")).thenReturn(customer1);

        mockMvc.perform(get("/api/customers/by-email").param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("john@example.com")));

        verify(customerService, times(1)).getCustomerByEmail("john@example.com");
    }

    @Test
    void testCreateCustomer_validRequest_createsCustomer() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John Doe",
                "john@example.com",
                "+40111111111"
        );
        when(customerService.createCustomer(any(CreateCustomerRequest.class))).thenReturn(customer1);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));

        verify(customerService, times(1)).createCustomer(any(CreateCustomerRequest.class));
    }

    @Test
    void testUpdateCustomer_existingCustomer_updatesCustomer() throws Exception {
        UpdateCustomerRequest request = new UpdateCustomerRequest("John Updated", "+40333333333");
        CustomerResponse updated =
                new CustomerResponse("1", "John Updated", "john@example.com", "+40333333333");
        when(customerService.updateCustomer(eq("1"), any(UpdateCustomerRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/customers/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.phoneNumber", is("+40333333333")));

        verify(customerService, times(1)).updateCustomer(eq("1"), any(UpdateCustomerRequest.class));
    }

    @Test
    void testCreateCustomer_invalidRequest_returnsBadRequest() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("", "invalid-email", "+40111111111");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDeleteCustomer_existingCustomer_returnsNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/customers/{id}", "1"))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer("1");
    }

    @Test
    void testDeleteCustomer_missingCustomer_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("999")).when(customerService).deleteCustomer("999");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/customers/{id}", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        verify(customerService, times(1)).deleteCustomer("999");
    }
}
