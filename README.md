# KitchenFlow

## Team
- **Team Name:** KitchenFlow Team
- **Members:**
  - Stoinoiu Alexandru - Customer & Order Management
  - Ristea Alexandru  - Menu, Inventory & Supplier Management

## Project Description

KitchenFlow is a SaaS restaurant management platform designed to handle both customer-facing operations and supplier-side workflows through a RESTful API. The system enables restaurants to manage menu items, process customer orders, track ingredient inventory, and coordinate with suppliers for restocking.

The application models a real-world business scenario where the restaurant acts as a central entity between customers and suppliers. Customer orders directly impact ingredient stock levels, while supplier interactions ensure that inventory is replenished when it falls below critical thresholds. This creates a complete operational cycle from ingredient acquisition to final product delivery.

KitchenFlow goes beyond simple CRUD operations by implementing core business logic such as stock validation, automatic availability checks for menu items, order lifecycle management, and restocking workflows. The system ensures that orders can only be placed when sufficient ingredients are available and supports tracking both customer orders and supplier deliveries through multiple status stages.

### Key Features
- Customer management and order placement with full order lifecycle tracking
- Menu management with recipe definitions and automatic availability based on stock
- Ingredient inventory tracking with low-stock detection and validation logic
- Supplier management and restocking workflows for maintaining inventory levels

### Technical Stack
- **Backend:** Spring Boot (Java 21)
- **Database:** MongoDB
- **API:** RESTful
- **Testing:** JUnit, Mockito, Cucumber
- **Monitoring:** Prometheus, Grafana
- **Deployment:** Docker

## Local Development

Lab 2 is set up to run locally from VS Code on Windows.

### Prerequisites
- Java 21
- Docker Desktop
- VS Code
- Optional: VS Code `REST Client` extension for `requests.http`

### Run Locally
Start MongoDB:
```powershell
.\start_mongo_only.ps1
```

Build the project:
```powershell
.\build_local.ps1
```

Run the Spring Boot service:
```powershell
.\run_local.ps1
```

Stop MongoDB services:
```powershell
.\stop_mongo_only.ps1
```

### Local URLs
- API: `http://localhost:8080/api/customers`
- Mongo Express: `http://localhost:8090`
- Mongo Express credentials: username `unibuc`, password `adobe`

## Lab 2 Status

Completed:
- Spring Boot starter project integrated into this repository
- Local Java 21 and Docker-based development flow verified
- MongoDB connection working locally
- Customer and Order endpoints available and testable through `requests.http`
- Menu, inventory, and supplier management endpoints added with REST request examples

Next:
- Validate the full backend on a local Java 21 installation
- Open pull requests to merge feature work into `main`

## Contributing

All team members follow trunk-based development:
1. Create a branch from `main` using the Lab 1 convention: `feature/<short-description>`
2. Make small changes and commit with clear messages
3. Create PR and request review
4. Address feedback
5. Merge after approval

### Branch Naming
- Feature work: `feature/<short-description>`
- Example: `feature/add-local-lab2-setup`

### Commit Message Style
- Follow the Lab 1 guidance and keep commits short, descriptive, and action-oriented
- Recommended format: `<type>: <short description>`
- Examples:
  - `docs: update readme for local lab 2 setup`
  - `chore: add powershell scripts for local development`
  - `feat: add customer controller`

### Tracking
## Stoinoiu Alexandru - Lab 2
- Today I set up Lab 2 to run locally from VS Code instead of Codespaces. I imported the official Spring Boot starter into our repository, installed Java 21, started MongoDB with Docker, and got the backend building and running on my machine. I also verified that the sample API works locally, including the /api/users endpoint and the MongoDB connection. On top of that, I added Windows-friendly PowerShell scripts and local setup documentation so the project is easier to run outside Codespaces.

- I replaced the sample starter users/todos backend with our real Customer & Order Management feature for KitchenFlow. I implemented the full backend flow for this feature slice: models, repositories, services, controllers, requests, responses, validation, and error handling. I also updated the test coverage and the requests.http file so the new endpoints can be verified locally.

## Ristea Alexandru - Lab 2
- I implemented the Menu, Inventory, and Supplier Management backend slice for KitchenFlow using the same Spring Boot and MongoDB architecture as the existing customer and order flows. This includes Mongo entities, repositories, services, controllers, request and response DTOs, validation, low-stock checks, menu recipe definitions, supplier assignment, and restock operations.

- I also updated `requests.http` with runnable examples for the new endpoints and added unit tests for the new controllers and services so the feature can be exercised consistently once the project is run with the required Java 21 setup.

## Stoinoiu Alexandru - Lab 4-5

- `CustomerServiceTest`
  - This file tests the customer business logic in isolation.
  - I mocked the repository so the service can be tested without MongoDB.
  - I covered success cases like getting, creating, updating, and deleting customers.
  - I also covered failure cases like missing customers and duplicate email, which helps branch coverage.

- `OrderServiceTest`
  - This file tests the order service, which has the most branch logic on my side.
  - I mocked both the order repository and customer service.
  - I tested creating orders, reading orders, updating order status, and deleting orders.
  - I also checked edge cases like blank or null special instructions, missing orders, missing customers, and invalid status changes.

- `CustomerControllerTest` and `OrderControllerTest`
  - These files test the HTTP endpoints using MockMvc.
  - They check that the controllers return the correct status codes and JSON responses.
  - I also verified error handling, like 404 for missing entities and 400 for validation or invalid status transitions.
  - This makes sure the controller layer is wired correctly to the service layer.

- `CustomerControllerIntegrationTest`
  - This file is my Lab 5 integration test.
  - It runs against the real Spring Boot stack with Testcontainers and MongoDB.
  - I verified the full customer flow: create, read by id, read by email, update, and delete.

- `jmeter/customer_api_test_plan_local.jmx`
  - This is my Lab 5 performance test plan.
  - It sends repeated create and get requests to the customer API.
  - I ran it locally in JMeter and confirmed the summary report had 0% errors.

- Commands:
```powershell
.\gradlew test
```
Run unit tests.

```powershell
.\gradlew jacocoTestReport
```
Run unit tests with JaCoCo and generate the coverage report.

```powershell
.\gradlew testIT
```
Run integration tests tagged with `IntegrationTest`.

```powershell
.\run_local.ps1
```
Start the application locally before running the JMeter performance plan.
