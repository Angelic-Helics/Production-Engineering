package ro.unibuc.prodeng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import ro.unibuc.prodeng.repository.CustomerRepository;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.CreateOrderRequest;
import ro.unibuc.prodeng.service.CustomerService;
import ro.unibuc.prodeng.service.OrderService;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableMongoRepositories
public class ProdEngApplication {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CustomerRepository customerRepository;

	public static void main(String[] args) {
		SpringApplication.run(ProdEngApplication.class, args);
	}

	@PostConstruct
	public void runAfterObjectCreated() {
		if (customerRepository.findByEmail("frodo@theshire.me").isEmpty()) {
			CreateCustomerRequest customerRequest = new CreateCustomerRequest(
					"Frodo Baggins",
					"frodo@theshire.me",
					"+40700000001"
			);
			customerService.createCustomer(customerRequest);
			orderService.createOrder(new CreateOrderRequest(
					"Second Breakfast Platter",
					1,
					"frodo@theshire.me",
					"Serve warm"
			));
		}
	}
}
