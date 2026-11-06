# SmartShelfX — Technical Interview Preparation & Deep Dive Q&A

This guide prepares you to explain and defend every design decision, code pattern, concurrency challenge, and architectural tradeoff made in **SmartShelfX** during a technical interview.

---

## 1. Spring Boot, Java & Backend Architecture

### Q1: Why did you choose a Layered Architecture (Controller $\to$ Service $\to$ Repository)?
**Answer:**
> "I separated responsibilities into three distinct layers to uphold the **Single Responsibility Principle (SRP)** and make unit testing straightforward:
> 1. **Controller Layer**: Handles HTTP protocol concerns (status codes, JSON serialization, URL mappings, bean validation `@Valid`). It does zero business calculation.
> 2. **Service Layer**: Contains purely domain business logic (e.g. checking if stock is sufficient, calculating new balances, enforcing status state transitions, and managing `@Transactional` boundaries).
> 3. **Repository Layer**: Abstracts database access using Spring Data JPA interfaces without coupling SQL statements into business logic.
>
> This allows me to unit test `StockTransactionService` in isolation by mocking `ProductRepository` and `StockTransactionRepository` using Mockito in less than 50ms without spinning up a live MySQL server."

---

### Q2: What happens if two warehouse workers attempt to reduce stock for the same product at the exact same millisecond? (Concurrency / Race Condition)
**Answer:**
> "If two requests read current stock as `5` simultaneously and both deduct `4`, an unprotected system would perform two writes resulting in `1`, effectively overselling `3` units (a classic **Lost Update** anomaly).
>
> In SmartShelfX, we protect against this in two ways:
> 1. **`@Transactional` Isolation**: Every stock modification is wrapped in an atomic transaction.
> 2. **Pessimistic vs Optimistic Locking**:
>    - In Spring Data JPA, we can apply `@Version` for **Optimistic Locking** using a version column. If worker A commits first, worker B's update fails with `OptimisticLockException` and can be retried or rejected with a clean message.
>    - Alternatively, for high-contention flash-sale items, we can use **Pessimistic Write Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) which issues a MySQL `SELECT ... FOR UPDATE`, placing an exclusive row-level lock so worker B blocks until worker A's transaction completes."

---

### Q3: How does Spring Security validate JWT tokens statelessly?
**Answer:**
> "SmartShelfX configures `SessionCreationPolicy.STATELESS`. This means Spring Security never creates an `HttpSession` in memory and stores zero session state on the server.
>
> For each incoming request:
> 1. `JwtAuthenticationFilter` intercepts the request before it reaches Spring's controllers.
> 2. It inspects the `Authorization: Bearer <token>` header.
> 3. It cryptographically verifies the token's HMAC-SHA256 signature using the secret key and checks the expiration timestamp.
> 4. If valid, it extracts the username and authorities (`ROLE_ADMIN`, `ROLE_WAREHOUSE_MANAGER`) and sets an `Authentication` object inside `SecurityContextHolder.getContext().setAuthentication(auth)`.
> 5. When the request finishes, the security context is automatically cleared for that thread."

---

### Q4: Why use DTOs instead of passing JPA `@Entity` classes directly into controllers?
**Answer:**
> "Exposing JPA entities directly in REST controllers causes four serious issues:
> 1. **Security Vulnerability (Mass Assignment)**: A malicious client could send `"enabled": true` or `"role": "ADMIN"` in the JSON payload, overwriting sensitive database fields.
> 2. **Infinite Recursion / LazyInitializationException**: Bidirectional relationships (e.g. `PurchaseOrder` $\leftrightarrow$ `PurchaseOrderItem`) cause infinite JSON serialization loops or errors if the session is closed.
> 3. **Leaking Sensitive Data**: Entities contain password hashes, audit fields, and internal IDs that should never be transmitted to the frontend.
> 4. **API Versioning Fragility**: Renaming a database column would immediately break existing mobile or web clients."

---

## 2. Database Design & Hibernate/JPA

### Q5: How did you ensure purchase order receiving is strictly idempotent?
**Answer:**
> "In inventory systems, **idempotency** is critical: receiving a delivery order multiple times must not inflate stock counts repeatedly.
>
> In `PurchaseOrderService.updateOrderStatus()`, we implement a strict state machine:
> 1. If an order's current status is already `RECEIVED`, the method immediately throws an `IllegalStateException` or `DuplicateResourceException` stating that stock was already credited.
> 2. Only if the status is currently `APPROVED` does the method proceed to loop through each line item, increment warehouse stock, insert audit transaction records, and change the status to `RECEIVED` within the same transaction.
> 3. This guarantees that double-clicking the 'Receive' button in Angular or network retries will never corrupt inventory levels."

---

### Q6: What foreign keys and indexing strategies did you apply in MySQL?
**Answer:**
> "1. **Foreign Keys**:
>    - `products.category_id` references `categories.id` (`ON DELETE RESTRICT`) to prevent deleting categories that have active products.
>    - `stock_transactions.product_id` references `products.id` to guarantee referential integrity in audit records.
>    - `purchase_order_items.purchase_order_id` references `purchase_orders.id` (`ON DELETE CASCADE`) so deleting or archiving an order cleans up line items cleanly.
>
> 2. **Indexes**:
>    - `products.sku` has a `UNIQUE` index for instant $O(1)$ product lookups and preventing duplicates.
>    - Composite indexes on `stock_transactions(product_id, transaction_date)` optimize time-series queries for demand forecasting and analytics."

---

## 3. AI Demand Forecasting & Restocking Mathematics

### Q7: Explain the deterministic restocking formula used in SmartShelfX.
**Answer:**
> "The formula used is:
> $$\text{Recommended Order Quantity} = \max\Big(0, \big(\text{Lead Time Demand} + \text{Safety Stock}\big) - \text{Current Warehouse Stock} - \text{Stock on Order}\Big)$$
>
> Where:
> - **Lead Time Demand**: The quantity expected to be sold during the supplier's delivery window (e.g., if lead time is 7 days and daily demand is 2 units, lead time demand = 14).
> - **Safety Stock**: Buffer stock to protect against supplier delays or sudden demand surges (e.g., 5 units).
> - **Current Stock**: Units physically available on warehouse shelves right now.
> - **Stock on Order**: Units already ordered via pending purchase orders that haven't arrived yet.
> - **$\max(0, \dots)$**: Ensures we never order negative quantities if warehouse inventory is already sufficient."

---

### Q8: Why did you use Linear Regression with a Chronological Split instead of a random train/test split?
**Answer:**
> "In time-series forecasting, standard `train_test_split(shuffle=True)` creates **data leakage** because the model learns from future data points to predict past data points.
>
> We use a **Chronological Split** (e.g. the first 75% of historical days for training, and the most recent 25% for testing). This mimics real-world conditions where the model only ever has access to past historical demand when predicting tomorrow's consumption.
>
> Furthermore, we benchmark our Linear Regression model against a Moving Average baseline and compute **Mean Absolute Error (MAE)**. If the linear regression MAE is superior, we know the trend-based model provides genuine value."

---

### Q9: What happens if the Python AI service crashes or goes offline?
**Answer:**
> "In microservice architectures, dependency failures must be handled gracefully without taking down the core application.
>
> In `ForecastService.java`, the HTTP call to `http://localhost:5000/api/forecast` is wrapped in a `try-catch` block for `ResourceAccessException` and `RestClientException`. If the Python microservice is unreachable, the backend automatically falls back to an internal **Java Moving-Average Engine**. It calculates demand and restocking quantities, sets `sourceService = 'JAVA_FALLBACK_MA_ENGINE'`, and logs a clear warning. The user in Angular still receives actionable inventory insights."

---

## 4. Frontend & Full-Stack Integration

### Q10: How does the Angular frontend handle authentication and private routes?
**Answer:**
> "We implement two core patterns:
> 1. **`authGuard` (Route Protection)**: Implemented as a functional CanActivateFn guard. When navigating to any route inside the portal, `authGuard` checks `authService.isAuthenticated()`. If false or token expired, it redirects immediately to `/login` preserving the return URL.
> 2. **`jwtInterceptor` (HTTP Interception)**: Registered globally in `app.config.ts`. It intercepts every outgoing HTTP request to `http://localhost:8080/api/**` and automatically clones the request with the `Authorization: Bearer <token>` header without repeating code in each service."

---

### Q11: How do you prevent Vendors from modifying other Vendors' orders or accessing Admin settings?
**Answer:**
> "We implement **Defense in Depth**:
> 1. **Backend Enforcement (True Security)**:
>    - Spring Security `@PreAuthorize` and `requestMatchers()` restrict sensitive endpoints (`/api/users/**`, `/api/products` DELETE, PO approvals) strictly to `ROLE_ADMIN` or `ROLE_WAREHOUSE_MANAGER`.
>    - In `PurchaseOrderService`, queries for vendor users filter results strictly by `vendorId = currentUser.getId()`.
> 2. **Frontend UI Adaptations (User Experience)**:
>    - Angular templates use `*ngIf="canManageOrders()"` or `*ngIf="isAdmin()"` to hide delete buttons, approval buttons, and navigation links from unauthorized roles."
