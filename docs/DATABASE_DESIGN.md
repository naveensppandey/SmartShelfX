# SmartShelfX — Database Design & Schema Specification

This document provides the complete database design for SmartShelfX, detailing every table, column data type, primary keys, foreign key constraints, indexes, and normalization considerations.

---

## 1. Entity-Relationship Overview

The database uses a normalized relational model designed for MySQL 8.0:

```
    ┌────────────────┐
    │     users      │
    └───────┬────────┘
            │ 1 (performed_by)
            ▼
    ┌────────────────┐ 1        N ┌──────────────────────┐
    │   categories   │───────────▶│       products       │◀──┐
    └────────────────┘            └──────────┬───────────┘   │
                                             │ 1             │
                         ┌───────────────────┼──────────┐    │
                         │ 1                 │ 1        │    │
                         ▼ N                 ▼ N        │    │
               ┌───────────────────┐ ┌───────────────┐  │    │
               │ stock_transactions│ │ notifications │  │    │
               └───────────────────┘ └───────────────┘  │    │
                                                        │    │
               ┌───────────────────┐ 1               N  │    │
               │  purchase_orders  │─────────────────┐  │    │
               └─────────┬─────────┘                 │  │    │
                         │ 1                         ▼  │    │
                         │                   ┌──────────┴───┐│
                         └──────────────────▶│purchase_order││
                                           N │    _items    ││
                                             └──────────────┘│
                                                             │
                                             ┌──────────────┐│
                                             │ sales_records│┘
                                             └──────────────┘
```

---

## 2. Table Specifications

### 2.1 Table: `users`
Stores system accounts, roles, and password hashes for authentication.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for user |
| `username` | `VARCHAR(50)` | NOT NULL, UNIQUE | Login handle (3-50 chars) |
| `email` | `VARCHAR(100)` | NOT NULL, UNIQUE | User contact email |
| `password` | `VARCHAR(255)` | NOT NULL | BCrypt encrypted password hash (never plain text) |
| `role` | `VARCHAR(30)` | NOT NULL | `ADMIN`, `WAREHOUSE_MANAGER`, or `VENDOR` |
| `full_name` | `VARCHAR(100)` | NOT NULL | Real name of the user |
| `enabled` | `BOOLEAN` | DEFAULT TRUE | Active account flag (soft deactivation) |
| `created_at` | `DATETIME` | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |
| `updated_at` | `DATETIME` | ON UPDATE CURRENT_TIMESTAMP | Last profile modification |

---

### 2.2 Table: `categories`
Groups products into logical business categories (e.g., Electronics, Perishables, Office Supplies).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Category identifier |
| `name` | `VARCHAR(100)` | NOT NULL, UNIQUE | Human-readable category title |
| `description` | `VARCHAR(500)` | NULL | Optional description of the category |
| `created_at` | `DATETIME` | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

---

### 2.3 Table: `products`
The core inventory table representing individual items managed in the warehouse.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique product ID |
| `name` | `VARCHAR(150)` | NOT NULL | Commercial product name |
| `sku` | `VARCHAR(50)` | NOT NULL, UNIQUE | Stock Keeping Unit (e.g., `ELEC-KEY-001`) |
| `description` | `TEXT` | NULL | Detailed item description |
| `category_id` | `BIGINT` | FOREIGN KEY (`categories.id`) | Foreign key linking to category |
| `vendor_id` | `BIGINT` | NULL, FOREIGN KEY (`users.id`) | Assigned vendor user ID (if vendor managed) |
| `price` | `DECIMAL(10, 2)`| NOT NULL | Selling price per unit (must be > 0) |
| `cost_price`| `DECIMAL(10, 2)`| NOT NULL | Purchase cost per unit (must be > 0) |
| `current_stock` | `INT` | NOT NULL, DEFAULT 0 | Current quantity available (>= 0) |
| `min_stock_threshold` | `INT` | NOT NULL, DEFAULT 10 | Reorder threshold; triggers low-stock alert |
| `reorder_quantity` | `INT` | NOT NULL, DEFAULT 50 | Standard replenishment batch size |
| `unit` | `VARCHAR(20)` | DEFAULT 'pcs' | Measurement unit (e.g., 'pcs', 'kg', 'box') |
| `created_at` | `DATETIME` | DEFAULT CURRENT_TIMESTAMP | Date product was added |
| `updated_at` | `DATETIME` | ON UPDATE CURRENT_TIMESTAMP | Date product was last edited |

---

### 2.4 Table: `stock_transactions`
Audit trail recording all inventory movements (incoming shipments, sales deductions, adjustments).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Transaction receipt ID |
| `product_id` | `BIGINT` | NOT NULL, FOREIGN KEY (`products.id`) | Product being moved |
| `transaction_type` | `VARCHAR(10)` | NOT NULL | `IN` (restock/return) or `OUT` (sale/dispatch) |
| `quantity` | `INT` | NOT NULL | Units moved (must be > 0) |
| `previous_stock` | `INT` | NOT NULL | Stock level before the transaction |
| `new_stock` | `INT` | NOT NULL | Stock level after the transaction |
| `notes` | `VARCHAR(255)` | NULL | Reference note, invoice #, or reason |
| `user_id` | `BIGINT` | NULL, FOREIGN KEY (`users.id`) | Staff member who executed transaction |
| `transaction_date` | `DATETIME` | DEFAULT CURRENT_TIMESTAMP | Exact date and time of movement |

---

### 2.5 Table: `purchase_orders`
Tracks vendor purchase orders from initiation to delivery.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Order ID |
| `order_number` | `VARCHAR(50)` | NOT NULL, UNIQUE | Human identifier (e.g., `PO-20261021-001`) |
| `vendor_id` | `BIGINT` | NULL, FOREIGN KEY (`users.id`) | Supplier user ID |
| `status` | `VARCHAR(20)` | NOT NULL | `PENDING`, `APPROVED`, `REJECTED`, `RECEIVED` |
| `total_amount` | `DECIMAL(12, 2)` | NOT NULL, DEFAULT 0.00 | Total financial value of the order |
| `notes` | `VARCHAR(500)` | NULL | Special instructions or vendor terms |
| `created_by` | `BIGINT` | NULL, FOREIGN KEY (`users.id`) | Staff/admin who placed order |
| `created_at` | `DATETIME` | DEFAULT CURRENT_TIMESTAMP | Timestamp when order was placed |
| `updated_at` | `DATETIME` | ON UPDATE CURRENT_TIMESTAMP | Last status update timestamp |
| `received_at` | `DATETIME` | NULL | Timestamp when warehouse verified receipt |

---

### 2.6 Table: `purchase_order_items`
Line items within each purchase order (Many-to-One relationship to `purchase_orders`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Line item ID |
| `purchase_order_id`| `BIGINT` | NOT NULL, FOREIGN KEY (`purchase_orders.id`) | Parent purchase order |
| `product_id` | `BIGINT` | NOT NULL, FOREIGN KEY (`products.id`) | Product being ordered |
| `quantity_ordered`| `INT` | NOT NULL | Quantity requested (> 0) |
| `unit_cost` | `DECIMAL(10, 2)` | NOT NULL | Agreed purchase cost per unit |
| `total_cost` | `DECIMAL(10, 2)` | NOT NULL | Calculated (`quantity_ordered * unit_cost`) |

---

### 2.7 Table: `notifications`
Alerts generated for staff and admins regarding inventory events.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Alert ID |
| `title` | `VARCHAR(150)` | NOT NULL | Short alert title |
| `message` | `VARCHAR(500)` | NOT NULL | Plain English description of the event |
| `type` | `VARCHAR(30)` | NOT NULL | `LOW_STOCK`, `OUT_OF_STOCK`, `ORDER_UPDATE`, `SYSTEM` |
| `is_read` | `BOOLEAN` | NOT NULL, DEFAULT FALSE | Read/unread flag |
| `product_id` | `BIGINT` | NULL, FOREIGN KEY (`products.id`) | Optional linked product ID |
| `created_at` | `DATETIME` | DEFAULT CURRENT_TIMESTAMP | Time alert was triggered |

---

### 2.8 Table: `sales_records`
Historical sales transactions used by the Python AI engine to forecast future demand.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Sale ID |
| `product_id` | `BIGINT` | NOT NULL, FOREIGN KEY (`products.id`) | Product sold |
| `quantity_sold` | `INT` | NOT NULL | Number of units sold (> 0) |
| `sale_date` | `DATE` | NOT NULL | Date sale occurred |
| `total_revenue` | `DECIMAL(10, 2)` | NOT NULL | Sale price * quantity_sold |
| `created_at` | `DATETIME` | DEFAULT CURRENT_TIMESTAMP | Record created timestamp |

---

## 3. Data Integrity & Business Invariants

1. **Non-Negative Stock**: `products.current_stock` must never be less than zero. In application code, before any `OUT` transaction, the check `current_stock >= requested_quantity` is strictly enforced.
2. **SKU Uniqueness**: `products.sku` has a unique database constraint and is checked during product creation and update.
3. **Idempotent Purchase Order Receiving**: When a purchase order transitions to `RECEIVED`, inventory stock increases exactly once. If `status == RECEIVED`, duplicate submissions are rejected with a 400 Bad Request error.
4. **Cascade Rules**:
   - Deleting a category does NOT delete products (foreign key sets to `NULL` or prevents deletion if products exist).
   - Deleting a purchase order cascades to delete its line items.
