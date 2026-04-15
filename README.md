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

Next:
- Implement the remaining KitchenFlow feature slice for menu, inventory, and suppliers
- Add project-specific endpoints to `requests.http`
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
