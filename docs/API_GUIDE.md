# SmartShelfX — REST API Specification & Guide

This document describes all REST API endpoints provided by the SmartShelfX Spring Boot Backend (default host: `http://localhost:8080`).

---

## 1. Authentication & Security Headers

All endpoints under `/api/**` (except `/api/auth/login` and `/api/auth/register`) require an HTTP `Authorization` header containing a valid JWT Bearer token:

```http
Authorization: Bearer <your_jwt_token_here>
Content-Type: application/json
```

### Pre-Seeded Demo Accounts

| Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | `ADMIN` | Full control across all features and users |
| `manager` | `Manager@123` | `WAREHOUSE_MANAGER` | Stock movements, purchase orders, forecasting |
| `vendor` | `Vendor@123` | `VENDOR` | Read assigned products and orders |

---

## 2. Authentication APIs (`/api/auth`)

### 2.1 User Login
- **Endpoint**: `POST /api/auth/login`
- **Auth**: Public
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "Admin@123"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsIn...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@smartshelfx.com",
    "fullName": "System Administrator",
    "role": "ADMIN"
  }
  ```

### 2.2 User Registration
- **Endpoint**: `POST /api/auth/register`
- **Auth**: Public
- **Request Body**:
  ```json
  {
    "username": "john_doe",
    "email": "john@example.com",
    "password": "Password@123",
    "fullName": "John Doe",
    "role": "WAREHOUSE_MANAGER"
  }
  ```
- **Response**: `201 Created` with JWT response payload.

### 2.3 Get Current User Profile
- **Endpoint**: `GET /api/auth/me`
- **Auth**: Authenticated
- **Response**: `200 OK` with User profile object.

---

## 3. Product Management APIs (`/api/products`)

### 3.1 List Products (with Search & Category Filter)
- **Endpoint**: `GET /api/products`
- **Query Parameters**:
  - `keyword` *(optional)*: filter by name or SKU
  - `categoryId` *(optional)*: filter by category ID
  - `stockStatus` *(optional)*: `ALL`, `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`
- **Response**: `200 OK` (Array of Product objects)

### 3.2 Get Product by ID
- **Endpoint**: `GET /api/products/{id}`
- **Response**: `200 OK` (Product object)

### 3.3 Create Product
- **Endpoint**: `POST /api/products`
- **Roles**: `ADMIN`, `WAREHOUSE_MANAGER`
- **Request Body**:
  ```json
  {
    "name": "Ergonomic Office Chair",
    "sku": "CHAIR-ERG-01",
    "description": "Adjustable lumbar support desk chair",
    "categoryId": 1,
    "vendorName": "Comfort Seating Corp",
    "price": 189.99,
    "costPrice": 110.00,
    "currentStock": 25,
    "minStockThreshold": 10,
    "reorderQuantity": 20,
    "unit": "units"
  }
  ```
- **Response**: `201 Created`

### 3.4 Update Product
- **Endpoint**: `PUT /api/products/{id}`
- **Roles**: `ADMIN`, `WAREHOUSE_MANAGER`
- **Response**: `200 OK`

### 3.5 Delete Product
- **Endpoint**: `DELETE /api/products/{id}`
- **Roles**: `ADMIN`
- **Response**: `204 No Content`

### 3.6 Low Stock & Out of Stock Alerts
- **Endpoint**: `GET /api/products/low-stock`
- **Endpoint**: `GET /api/products/out-of-stock`

---

## 4. Category APIs (`/api/categories`)

- **`GET /api/categories`**: List all categories with product count.
- **`GET /api/categories/{id}`**: Get category details.
- **`POST /api/categories`**: Create category (`ADMIN`, `WAREHOUSE_MANAGER`).
- **`PUT /api/categories/{id}`**: Update category.
- **`DELETE /api/categories/{id}`**: Delete category (protected against categories with active products).

---

## 5. Stock Movement APIs (`/api/transactions`)

### 5.1 Record Stock In / Stock Out
- **Endpoint**: `POST /api/transactions`
- **Roles**: `ADMIN`, `WAREHOUSE_MANAGER`
- **Request Body**:
  ```json
  {
    "productId": 1,
    "transactionType": "OUT",
    "quantity": 5,
    "notes": "Retail storefront fulfillment shipment"
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "id": 14,
    "productId": 1,
    "productName": "Wireless Bluetooth Mouse",
    "productSku": "TECH-MOU-01",
    "transactionType": "OUT",
    "quantity": 5,
    "previousStock": 15,
    "newStock": 10,
    "notes": "Retail storefront fulfillment shipment",
    "transactionDate": "2026-10-21T18:30:00"
  }
  ```

### 5.2 List Transactions History
- **Endpoint**: `GET /api/transactions`
- **Query Parameters**:
  - `productId` *(optional)*: Filter transactions for a single product.
  - `type` *(optional)*: `IN` or `OUT`.

---

## 6. Purchase Orders & Restocking APIs (`/api/purchase-orders`)

### 6.1 List Purchase Orders
- **Endpoint**: `GET /api/purchase-orders`
- **Response**: `200 OK` (Array of PurchaseOrder objects with nested line items)

### 6.2 Create Purchase Order
- **Endpoint**: `POST /api/purchase-orders`
- **Roles**: `ADMIN`, `WAREHOUSE_MANAGER`
- **Request Body**:
  ```json
  {
    "vendorName": "Global Silicon Distributors",
    "notes": "Express restock for Q4 peak inventory",
    "items": [
      {
        "productId": 1,
        "quantityOrdered": 50,
        "unitCost": 15.00
      }
    ]
  }
  ```
- **Response**: `201 Created`

### 6.3 Update Order Status (Approve / Reject / Receive)
- **Endpoint**: `PUT /api/purchase-orders/{id}/status`
- **Roles**: `ADMIN`, `WAREHOUSE_MANAGER`
- **Request Body**:
  ```json
  {
    "status": "RECEIVED",
    "notes": "Delivered and verified at loading dock 4"
  }
  ```
- **Response**: `200 OK`
  > **Note**: Setting status to `RECEIVED` atomically increases inventory stock for every product in the order and generates `IN` stock transactions.

### 6.4 Restock Suggestions
- **Endpoint**: `GET /api/purchase-orders/suggestions`
- **Response**: `200 OK` (Array of restock items where current stock $\le$ threshold)

---

## 7. AI Demand Forecasting APIs (`/api/forecasting`)

### 7.1 Forecast Product Demand
- **Endpoint**: `GET /api/forecasting/product/{productId}?days=30`
- **Roles**: `ADMIN`, `WAREHOUSE_MANAGER`
- **Response**: `200 OK`
  ```json
  {
    "productId": 1,
    "productName": "Wireless Bluetooth Mouse",
    "productSku": "TECH-MOU-01",
    "forecastPeriodDays": 30,
    "totalPredictedDemand": 48.0,
    "dailyForecasts": [1.6, 1.6, 1.6, 1.6, ...],
    "historicalDataPoints": 28,
    "modelEvaluation": {
      "modelType": "Scikit-Learn Linear Regression (Chronological Split 75/25)",
      "meanAbsoluteError": 0.42,
      "rootMeanSquaredError": 0.58,
      "baselineMovingAverageMae": 0.65,
      "modelAccuracyNote": "Evaluated using chronological train/test split. Linear Regression shows 35.4% MAE improvement over moving average baseline."
    },
    "restockingRecommendation": {
      "leadTimeDays": 7,
      "predictedDemandDuringLeadTime": 11.2,
      "safetyStockBuffer": 5,
      "currentWarehouseStock": 10,
      "stockAlreadyOnOrder": 0,
      "recommendedOrderQuantity": 7,
      "restockUrgency": "HIGH",
      "explanation": "Calculated via max(0, leadTimeDemand(11.2) + safetyStock(5) - currentStock(10) - stockOnOrder(0))"
    },
    "sourceService": "PYTHON_FLASK_MICROSERVICE"
  }
  ```

---

## 8. Dashboard Analytics & Notifications

- **`GET /api/analytics/dashboard`**: Returns warehouse valuation, stock distribution, movement velocity, and low-stock alerts.
- **`GET /api/notifications`**: Returns all system notifications.
- **`GET /api/notifications/unread-count`**: Returns `{"unreadCount": 3}`.
- **`PUT /api/notifications/{id}/read`**: Marks notification as read.
- **`PUT /api/notifications/mark-all-read`**: Marks all notifications as read.

---

## 9. User Administration APIs (`/api/users`)

- **`GET /api/users`**: List all user accounts (`ADMIN` only).
- **`GET /api/users/{id}`**: Get user account details.
- **`PUT /api/users/{id}/toggle-status`**: Enable or disable user account (`ADMIN` only). Prevents self-disable.
