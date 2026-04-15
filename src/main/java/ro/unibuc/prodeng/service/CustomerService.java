package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.CustomerEntity;
import ro.unibuc.prodeng.repository.CustomerRepository;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.UpdateCustomerRequest;
import ro.unibuc.prodeng.response.CustomerResponse;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(String id) {
        return toResponse(getCustomerEntityById(id));
    }

    public CustomerResponse getCustomerByEmail(String email) {
        return toResponse(getCustomerEntityByEmail(email));
    }

    public CustomerEntity getCustomerEntityById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    public CustomerEntity getCustomerEntityByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(email));
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        CustomerEntity customer = new CustomerEntity(
                null,
                request.name(),
                request.email(),
                request.phoneNumber()
        );

        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse updateCustomer(String id, UpdateCustomerRequest request) {
        CustomerEntity existing = getCustomerEntityById(id);
        CustomerEntity updated = new CustomerEntity(
                existing.id(),
                request.name(),
                existing.email(),
                request.phoneNumber()
        );

        return toResponse(customerRepository.save(updated));
    }

    public void deleteCustomer(String id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }

        customerRepository.deleteById(id);
    }

    private CustomerResponse toResponse(CustomerEntity customer) {
        return new CustomerResponse(
                customer.id(),
                customer.name(),
                customer.email(),
                customer.phoneNumber()
        );
    }
}
