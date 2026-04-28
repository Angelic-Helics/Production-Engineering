package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.CustomerEntity;
import ro.unibuc.prodeng.repository.CustomerRepository;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.UpdateCustomerRequest;
import ro.unibuc.prodeng.response.CustomerResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testGetAllCustomers_withMultipleCustomers_returnsAllCustomers() {
        List<CustomerEntity> customers = Arrays.asList(
                new CustomerEntity("1", "Alice", "alice@example.com", "+40111111111"),
                new CustomerEntity("2", "Bob", "bob@example.com", "+40222222222")
        );
        when(customerRepository.findAll()).thenReturn(customers);

        List<CustomerResponse> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).name());
        assertEquals("Bob", result.get(1).name());
    }

    @Test
    void testGetCustomerById_existingCustomer_returnsCustomer() {
        CustomerEntity customer = new CustomerEntity("1", "Alice", "alice@example.com", "+40111111111");
        when(customerRepository.findById("1")).thenReturn(Optional.of(customer));

        CustomerResponse result = customerService.getCustomerById("1");

        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
    }

    @Test
    void testGetCustomerById_missingCustomer_throwsEntityNotFoundException() {
        when(customerRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> customerService.getCustomerById("999"));
    }

    @Test
    void testGetCustomerByEmail_existingCustomer_returnsCustomer() {
        CustomerEntity customer = new CustomerEntity("1", "Alice", "alice@example.com", "+40111111111");
        when(customerRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(customer));

        CustomerResponse result = customerService.getCustomerByEmail("alice@example.com");

        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
    }

    @Test
    void testGetCustomerByEmail_missingCustomer_throwsEntityNotFoundException() {
        when(customerRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> customerService.getCustomerByEmail("ghost@example.com"));
    }

    @Test
    void testCreateCustomer_validCustomer_createsAndReturnsCustomer() {
        CreateCustomerRequest request =
                new CreateCustomerRequest("Alice", "alice@example.com", "+40111111111");
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity entity = invocation.getArgument(0);
            return new CustomerEntity("generated-id-123", entity.name(), entity.email(), entity.phoneNumber());
        });

        CustomerResponse result = customerService.createCustomer(request);

        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertEquals("+40111111111", result.phoneNumber());
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }

    @Test
    void testCreateCustomer_duplicateEmail_throwsIllegalArgumentException() {
        CreateCustomerRequest request =
                new CreateCustomerRequest("Alice", "alice@example.com", "+40111111111");
        when(customerRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new CustomerEntity("1", "Existing", "alice@example.com", "+40000000000")));

        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(request));
    }

    @Test
    void testUpdateCustomer_existingCustomer_updatesCustomer() {
        CustomerEntity existing = new CustomerEntity("1", "Alice", "alice@example.com", "+40111111111");
        when(customerRepository.findById("1")).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse result = customerService.updateCustomer(
                "1",
                new UpdateCustomerRequest("Alice Updated", "+40333333333")
        );

        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("Alice Updated", result.name());
        assertEquals("+40333333333", result.phoneNumber());
    }

    @Test
    void testDeleteCustomer_missingCustomer_throwsEntityNotFoundException() {
        when(customerRepository.existsById("999")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> customerService.deleteCustomer("999"));
    }

    @Test
    void testDeleteCustomer_existingCustomer_deletesCustomer() {
        when(customerRepository.existsById("1")).thenReturn(true);

        customerService.deleteCustomer("1");

        verify(customerRepository, times(1)).deleteById("1");
    }
}
