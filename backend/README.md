# SmartShelfX Backend Architecture & Package Guide

This document explains the purpose and responsibility of each package in the Spring Boot backend (`com.smartshelfx`).

The backend follows the clean **N-Tier Layered Architecture** standard for enterprise Spring Boot applications:

```
[ HTTP Client / Angular Frontend ]
                 │ (JSON over HTTP)
                 ▼
          1. Controller Layer       <-- @RestController (Handles HTTP requests, inputs, routes)
                 │
                 ▼
          2. Service Layer          <-- @Service (Core business logic, calculations, validations)
                 │
                 ▼
          3. Repository Layer       <-- @Repository (Spring Data JPA interfaces querying MySQL)
                 │
                 ▼
          4. Database               <-- MySQL 8.0 (Persists data in normalized relational tables)
```

---

## Package Responsibilities

### 1. `com.smartshelfx.controller`
- **Purpose**: Exposes REST API endpoints to the outside world (the Angular frontend or external tools like Postman/cURL).
- **Responsibilities**:
  - Maps URL routes (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`).
  - Reads input parameters, path variables, and JSON request bodies (`@RequestBody`).
  - Triggers Bean Validation (`@Valid`).
  - Calls the appropriate Service methods.
  - Returns standard HTTP response codes (e.g., `200 OK`, `201 Created`, `204 No Content`).
- **Rule**: Controllers do NOT contain business logic or raw database queries.

### 2. `com.smartshelfx.service`
- **Purpose**: Houses all core business rules and orchestration logic.
- **Responsibilities**:
  - Validates stock quantities (prevents negative balances).
  - Enforces purchase order state transitions (`PENDING` -> `APPROVED` -> `RECEIVED`).
  - Updates inventory balances consistently within database transactions (`@Transactional`).
  - Connects to the external Python AI Forecasting microservice.
  - Generates automated restocking suggestions and low-stock alerts.
- **Rule**: All complex rules belong here, keeping controllers thin and clean.

### 3. `com.smartshelfx.repository`
- **Purpose**: Provides data access and persistence abstraction.
- **Responsibilities**:
  - Extends Spring Data JPA's `JpaRepository<T, ID>`.
  - Automatically provides CRUD operations (`save`, `findById`, `findAll`, `deleteById`).
  - Provides custom query methods (e.g., `findBySku`, `findByCurrentStockLessThanEqual`, `findByStatus`).
- **Rule**: Repositories interact directly with the database; they do not enforce business decisions.

### 4. `com.smartshelfx.entity`
- **Purpose**: Represents database tables as Java objects (JPA Entities).
- **Responsibilities**:
  - Mapped with `@Entity` and `@Table`.
  - Defines table columns (`@Column`), primary keys (`@Id`, `@GeneratedValue`), and foreign key relationships (`@ManyToOne`, `@JoinColumn`).
  - Contains entities for `User`, `Product`, `Category`, `StockTransaction`, `PurchaseOrder`, `PurchaseOrderItem`, `Notification`, and `DemandForecast`.
- **Rule**: Entities represent internal database state. They should not be directly exposed as API outputs when private fields (like hashed passwords) need protection.

### 5. `com.smartshelfx.dto` (Data Transfer Objects)
- **Purpose**: Structured objects used to receive data from client requests or return tailored responses.
- **Responsibilities**:
  - Request DTOs: `LoginRequest`, `RegisterRequest`, `ProductRequest`, `StockTransactionRequest`, `PurchaseOrderRequest`.
  - Response DTOs: `JwtResponse`, `ProductResponse`, `DashboardStatsResponse`, `RestockSuggestionResponse`.
  - Carries validation annotations (`@NotBlank`, `@Min`, `@NotNull`, `@Email`).
- **Rule**: Decouples API contract from internal database schema, preventing accidental exposure of sensitive entity fields.

### 6. `com.smartshelfx.security`
- **Purpose**: Manages user authentication, token generation, and role-based authorization.
- **Responsibilities**:
  - Configures Spring Security filter chains (`SecurityFilterChain`).
  - `JwtUtils`: Generates and validates signed HMAC-SHA256 JSON Web Tokens.
  - `JwtAuthenticationFilter`: Extracts JWT from `Authorization: Bearer <token>` header on every incoming request and sets `SecurityContextHolder`.
  - `UserDetailsServiceImpl`: Loads user by username from MySQL database for authentication.
  - Password encoding using `BCryptPasswordEncoder`.

### 7. `com.smartshelfx.config`
- **Purpose**: Application-wide configurations and Spring Beans.
- **Responsibilities**:
  - CORS (Cross-Origin Resource Sharing) configuration allowing the Angular frontend (`http://localhost:4200`) to communicate with the backend (`http://localhost:8080`).
  - `RestTemplate` bean configuration for communicating with the Python AI microservice with timeout settings.
  - Database initialization / demo data seeding if required.

### 8. `com.smartshelfx.exception`
- **Purpose**: Centralized and consistent error handling across all controllers.
- **Responsibilities**:
  - `@RestControllerAdvice` global exception handler (`GlobalExceptionHandler`).
  - Catches custom domain exceptions (`ResourceNotFoundException`, `BadRequestException`, `InsufficientStockException`, `DuplicateSkuException`).
  - Catches validation errors (`MethodArgumentNotValidException`) and transforms them into user-friendly error maps.
  - Returns structured `ErrorResponse` objects with timestamps, HTTP status codes, and clear explanations without leaking internal stack traces.

---

## Request Lifecycle Example

Let's trace what happens when an inventory manager adds stock:

1. **Client Request**: Frontend sends `POST /api/transactions` with payload `{ "productId": 10, "type": "IN", "quantity": 50, "notes": "Received shipment" }` and Bearer JWT token in headers.
2. **Security Filter**: `JwtAuthenticationFilter` validates token, confirms user identity and role (`WAREHOUSE_MANAGER`), sets security context.
3. **Controller**: `StockTransactionController` receives `StockTransactionRequest` annotated with `@Valid`.
4. **Service**: `StockTransactionService` loads the product from `ProductRepository`, checks current stock, updates balance (`currentStock + 50`), creates a `StockTransaction` entity, records audit details, and saves both in a `@Transactional` boundary.
5. **Repository**: `StockTransactionRepository` and `ProductRepository` execute SQL `INSERT` and `UPDATE` statements into MySQL.
6. **Response**: Controller maps the updated state to a response DTO and returns HTTP `201 Created` with the transaction confirmation.
