# SmartShelfX — Comprehensive Project & Architecture Explanation

> **Welcome!** If you are learning full-stack development, preparing for a software engineering technical interview, or explaining this project to a hiring manager, this document breaks down every concept, directory, class, and data flow in plain, understandable English.

---

## 1. System High-Level Architecture

SmartShelfX follows a **Modern Three-Tier Distributed Micro-Service Architecture**:

```
+--------------------------------------------------------------+
|                    Angular 21 SPA Frontend                   |
|  - Modern standalone components                              |
|  - Angular Material & Chart.js data visualization            |
|  - HTTP Interceptor for automatic JWT Bearer token delivery   |
+------------------------------+-------------------------------+
                               |
                               | REST APIs (HTTP / JSON)
                               v
+--------------------------------------------------------------+
|               Spring Boot 3.4.x Java 21 Backend              |
|  - Stateless Spring Security with BCrypt & JWT               |
|  - Layered Architecture: Controller -> Service -> Repository |
|  - ACID Transaction management with Spring Data JPA          |
|  - Microservice client to AI Engine with fallback resilience |
+---------------+------------------------------+---------------+
                |                              |
                | JDBC / JPA                   | REST (JSON)
                v                              v
+-------------------------------+  +---------------------------+
|         MySQL 8 Database      |  | Python 3.10 AI Microservice|
|  - Relational schema          |  | - Flask REST API          |
|  - Foreign keys & indices     |  | - Pandas Data Cleaning    |
|  - Audit transaction history  |  | - Scikit-learn Linear Reg |
+-------------------------------+  +---------------------------+
```

---

## 2. Directory Layout & Package Breakdown

### 2.1 Backend Package Layout (`com.smartshelfx.*`)

Every Java class is grouped according to its single responsibility:

| Package | Purpose & Plain English Explanation | Key Classes |
| :--- | :--- | :--- |
| **`com.smartshelfx.entity`** | Java representation of database tables. Hibernate maps these classes directly to MySQL tables using JPA annotations (`@Entity`, `@Table`, `@Id`). | `Product`, `Category`, `StockTransaction`, `PurchaseOrder`, `PurchaseOrderItem`, `User`, `Notification` |
| **`com.smartshelfx.repository`**| Data Access Object (DAO) interfaces extending Spring Data JPA's `JpaRepository`. Provides CRUD methods (`save`, `findById`, `delete`) and custom JPQL queries with zero boilerplate SQL. | `ProductRepository`, `PurchaseOrderRepository`, `StockTransactionRepository`, `UserRepository`, etc. |
| **`com.smartshelfx.dto`** | **Data Transfer Objects**. Decouples internal database entities from external HTTP requests and responses. Shields sensitive columns (like password hashes) and validates input fields (`@NotBlank`, `@Min`). | `ProductRequest`, `StockTransactionRequest`, `PurchaseOrderRequest`, `JwtResponse`, `DashboardStatsResponse` |
| **`com.smartshelfx.service`** | The **Heart & Brain** of the application. Encapsulates business logic, calculations, inventory validations, and transaction boundaries (`@Transactional`). | `ProductService`, `StockTransactionService`, `PurchaseOrderService`, `ForecastService`, `AuthService` |
| **`com.smartshelfx.controller`** | REST API endpoints. Accepts incoming HTTP requests (`GET`, `POST`, `PUT`, `DELETE`), extracts parameters, calls services, and returns HTTP status codes (`200 OK`, `201 Created`, `400 Bad Request`). | `ProductController`, `StockTransactionController`, `PurchaseOrderController`, `AuthController` |
| **`com.smartshelfx.security`** | Security infrastructure: JWT generation/validation, password encryption via BCrypt, stateless authentication filter, and UserDetailsService. | `JwtUtils`, `JwtAuthenticationFilter`, `UserDetailsServiceImpl`, `AuthEntryPointJwt` |
| **`com.smartshelfx.config`** | Application configuration beans: Spring Security filter chain (`SecurityConfig`), CORS mappings, PasswordEncoder, and RestTemplate bean for AI microservice communication. | `SecurityConfig`, `ApplicationConfig`, `DataSeeder` |
| **`com.smartshelfx.exception`** | Centralized error handling. Intercepts exceptions thrown anywhere in the application and converts them to standardized, clean JSON responses instead of exposing stack traces. | `GlobalExceptionHandler`, `ResourceNotFoundException`, `InsufficientStockException`, `DuplicateResourceException` |

---

### 2.2 Python AI Engine Layout (`ai-engine/`)

| File / Folder | Purpose |
| :--- | :--- |
| **`app.py`** | Flask web server exposing `/health` and `/api/forecast` endpoints on port 5000 with CORS support. |
| **`model.py`** | `DemandForecaster` class encapsulating data validation, chronology sorting, Linear Regression demand fitting, MAE/RMSE calculation, and safety-stock restocking logic. |
| **`tests/test_forecast.py`** | Automated unit tests verified by `pytest` covering normal forecasts, insufficient historical data fallback, negative input guards, and deterministic restocking mathematics. |

---

### 2.3 Angular Frontend Layout (`frontend/src/app/`)

| Directory | Purpose |
| :--- | :--- |
| **`core/models/`** | TypeScript interface definitions mirroring Spring Boot DTOs (`Product`, `StockTransaction`, `PurchaseOrder`, `JwtResponse`). |
| **`core/services/`** | Angular HTTP services (`AuthService`, `ProductService`, `OrderService`, `ForecastService`, `NotificationService`). |
| **`core/guards/`** | `authGuard` preventing unauthenticated users from accessing protected portal routes. |
| **`core/interceptors/`** | `jwtInterceptor` automatically appending `Authorization: Bearer <token>` to all outgoing backend HTTP calls. |
| **`layout/`** | `MainLayoutComponent` featuring responsive sidebar, top navbar, role pills, and live unread notifications badge. |
| **`pages/`** | 12 dedicated standalone pages for complete inventory lifecycle management. |

---

## 3. Step-by-Step Execution Lifecycle

Let's walk through common user operations step-by-step:

### A. User Authentication (Login)
1. User enters username and password in Angular (`pages/login`).
2. Angular makes a `POST /api/auth/login` request.
3. Spring Boot's `SecurityConfig` permits this public endpoint.
4. `AuthController` calls `AuthService.login()`.
5. Spring Security's `AuthenticationManager` verifies the username and validates the password hash using **BCrypt**.
6. `JwtUtils` signs a cryptographic JWT token containing the username, role, and expiration timestamp (24 hours).
7. Spring Boot returns the token and user metadata in a `JwtResponse`.
8. Angular stores the token in `localStorage` and redirects the user to `/dashboard`.
9. All future HTTP requests automatically include the header:
   ```http
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsIn...
   ```

---

### B. Recording a Stock-Out Transaction
1. Warehouse Manager opens the "Stock Transactions" modal, selects a product, sets Type = `OUT`, Quantity = `15`, and clicks Submit.
2. Angular sends `POST /api/transactions`.
3. `JwtAuthenticationFilter` inspects the Bearer token, validates signature and expiration, and populates `SecurityContextHolder`.
4. `StockTransactionController` delegates to `StockTransactionService.recordTransaction()`.
5. The method is marked **`@Transactional`**:
   - Fetches the product from MySQL with database row lock.
   - **Business Guard**: Checks `if (product.getCurrentStock() < quantity)`. If true, aborts immediately by throwing `InsufficientStockException("Insufficient stock available...")`. No records are saved.
   - Calculates `newStock = previousStock - quantity`.
   - Updates `product.setCurrentStock(newStock)`.
   - If `newStock <= product.getMinStockThreshold()`, automatically generates an alert notification in the `notifications` table!
   - Saves a new immutable `StockTransaction` audit record with previous stock, new stock, date, and user ID.
   - Commits the transaction to MySQL atomically.
6. Returns `201 Created` with full transaction details to Angular.

---

### C. Running AI Demand Forecasting
1. Manager selects a product (e.g. "Mechanical Gaming Keyboard") and clicks "Run Forecast (30 Days)" on the Demand Forecasting page.
2. Angular calls `GET /api/forecasting/product/{id}?days=30`.
3. Spring Boot's `ForecastService`:
   - Retrieves historical stock-out transactions for this product from the database.
   - Assembles a JSON payload with daily demand points.
   - Issues a REST call to the Python microservice at `http://localhost:5000/api/forecast`.
4. Python AI Service (`model.py`):
   - Validates historical data.
   - Arranges daily consumption into a chronological sequence.
   - Splits data chronologically (75% training, 25% testing) without future leakage.
   - Trains a Linear Regression model with day index features.
   - Evaluates performance against a Moving Average baseline and computes MAE (Mean Absolute Error).
   - Generates predicted daily demand across the requested horizon.
   - Applies the deterministic restocking formula:
     $$\text{Recommended Order Quantity} = \max(0, \text{Lead Time Demand} + \text{Safety Stock} - \text{Current Stock} - \text{Stock on Order})$$
5. Spring Boot receives the Python response and forwards it to the Angular UI.
6. *Resilience Feature*: If the Python service is offline or unreachable, Spring Boot's `ForecastService` catches `ResourceAccessException`, falls back gracefully to an internal moving-average calculation, and labels the output clearly with `sourceService = "JAVA_FALLBACK_MA_ENGINE"`.

---

### D. Idempotent Purchase Order Receiving
1. A delivery truck arrives at the warehouse with 50 units of Wireless Mice.
2. Manager finds PO `#PO-1002` (Status: `APPROVED`) and clicks **"Receive Order"**.
3. Angular calls `PUT /api/purchase-orders/1002/status` with `{"status": "RECEIVED"}`.
4. `PurchaseOrderService.updateOrderStatus()` checks the existing status:
   - If order is already `RECEIVED`, it aborts and throws an exception:
     > *"Purchase order PO-1002 has already been received. Duplicate inventory additions are prevented."*
   - If order is `APPROVED`, it iterates through all line items:
     - Increases `product.currentStock += item.quantityOrdered`.
     - Automatically creates an official `IN` stock transaction in the audit ledger.
   - Marks PO status as `RECEIVED` and saves `receivedAt` timestamp.
5. All updates execute within a single `@Transactional` boundary, guaranteeing that the database remains strictly consistent even if power fails mid-process.

---

## 4. Key Design Patterns Used

| Pattern | Where it is used | Why it matters |
| :--- | :--- | :--- |
| **Layered Architecture** | Controller $\to$ Service $\to$ Repository | Clear separation of concerns; easy to test each layer independently with Mockito. |
| **Data Transfer Object (DTO)** | `com.smartshelfx.dto.*` | Prevents exposing sensitive fields (passwords, tokens) and decouples internal DB schema from external API contracts. |
| **Repository Pattern** | Spring Data JPA interfaces | Eliminates raw SQL string bugs, supports pagination/sorting, and prevents SQL injection vulnerabilities. |
| **Circuit Breaker / Fallback** | `ForecastService.java` | Keeps the application operational even when downstream AI services experience outages. |
| **Interceptor Pattern** | `jwt.interceptor.ts` & `JwtAuthenticationFilter.java` | Centralizes authentication logic once rather than duplicating token handling across every API call. |
| **Idempotent State Machine** | `PurchaseOrderService.java` | Prevents double-stocking or erratic status changes on concurrent or repeated clicks. |
