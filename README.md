# SmartShelfX — AI-Based Inventory Management System

**SmartShelfX** is an enterprise-grade, full-stack inventory management and demand forecasting platform engineered for modern distribution centers, warehouses, and retail businesses.

Built with **Angular 21**, **Spring Boot 3.4.x (Java 21)**, **MySQL 8.0**, and a decoupled **Python Flask AI microservice**, the system balances rigorous transactional integrity, role-based security, automated restocking logic, and intuitive data visualizations.

---

## 1. System Architecture

SmartShelfX is designed as a distributed, decoupled three-tier system:

```
┌────────────────────────────────────────────────────────┐
│               Angular 21 SPA Frontend                  │
│  - 12 Responsive Standalone Pages                      │
│  - Angular Material & Chart.js Visualizations          │
│  - JWT Bearer Authentication Interceptor               │
│  - Port: 4200                                          │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP / JSON
                            ▼
┌────────────────────────────────────────────────────────┐         HTTP / JSON       ┌───────────────────────────────┐
│           Spring Boot 3.4 Backend (Java 21)            │ ◄───────────────────────► │   Python Flask AI Engine      │
│  - Stateless Spring Security + BCrypt + JWT            │                           │   - Pandas Preprocessing      │
│  - Controller -> Service -> Repository Architecture    │                           │   - Scikit-learn Linear Reg   │
│  - Idempotent Transaction Boundaries (@Transactional)  │                           │   - Restocking Model Formula  │
│  - Java Fallback Engine if AI Service is Offline       │                           │   - Port: 5000                │
│  - Port: 8080                                          │                           └───────────────────────────────┘
└───────────────────────────┬────────────────────────────┘
                            │ JDBC / JPA
                            ▼
┌────────────────────────────────────────────────────────┐
│             MySQL 8.0 Relational Database              │
│  - Products, Categories, Stock Transactions            │
│  - Purchase Orders, Order Items, Notifications, Users  │
│  - Port: 3306 (database: smartshelfx)                  │
└────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

| Layer | Technologies | Version |
| :--- | :--- | :--- |
| **Frontend** | Angular, TypeScript, Angular Material, SCSS, Chart.js | Angular 21, Chart.js 4.5 |
| **Backend** | Java, Spring Boot, Spring Web, Spring Data JPA, Spring Security, JJWT | Java 21 LTS, Spring Boot 3.4.1 |
| **Database** | MySQL Server, Hibernate ORM | MySQL 8.0 |
| **AI Engine** | Python, Flask, Pandas, Scikit-learn, NumPy, Pytest | Python 3.10+ |

---

## 3. Implemented Frontend Pages (12 Pages)

1. **Authentication (`/login`)**: Login and Registration forms with pre-fill demo role buttons.
2. **Dashboard (`/dashboard`)**: KPI overview cards, 14-day velocity chart, category doughnut chart, urgent restocking table, and recent activity log.
3. **Product Inventory (`/products`)**: Real-time catalog table, keyword search, category filter, stock status chips, delete confirmation modal.
4. **Product Add/Edit (`/products/new`, `/products/edit/:id`)**: Form with validation for SKU uniqueness, unit cost, price, and reorder levels.
5. **Categories (`/categories`)**: Category CRUD with live product count and deletion protection for active categories.
6. **Stock Transactions (`/transactions`)**: Stock-In & Stock-Out modal with instant balance preview, audit logging, and guard against negative inventory.
7. **Demand Forecasting (`/forecasting`)**: AI demand prediction engine with horizon selector (7 or 30 days), interactive daily demand curve, MAE metrics, and one-click purchase order creation.
8. **Purchase Orders (`/purchase-orders`)**: Order management with status workflow (`PENDING` $\to$ `APPROVED` $\to$ `RECEIVED` or `REJECTED`), order detail modal, line-item calculator, and an automated restock suggestions tab.
9. **Analytics & Reports (`/analytics`)**: Financial valuation breakdown, catalog stock health distribution, top capital assets, and export/print capability.
10. **Notifications (`/notifications`)**: Real-time threshold alerts (`LOW_STOCK`, `OUT_OF_STOCK`, `ORDER_UPDATE`), unread counter, and mark-as-read actions.
11. **User Management (`/users`)**: Administrator panel to view users, inspect assigned roles, and toggle account activation.
12. **User Profile (`/profile`)**: Authenticated user identity card, active permissions list, and JWT session metadata.

---

## 4. Automated Restocking Logic

Inventory replenishment suggestions are calculated using the deterministic formula:

$$\text{Recommended Order Quantity} = \max\Big(0, \big(\text{Lead Time Demand} + \text{Safety Stock}\big) - \text{Current Stock} - \text{Stock Already on Order}\Big)$$

- **Lead Time Demand**: Predicted daily demand multiplied by vendor delivery days.
- **Safety Stock**: Configured buffer against supply disruptions.
- **Idempotency Guarantee**: Receiving an approved order updates stock exactly once, preventing double-stocking on repeated requests.

---

## 5. Pre-Seeded Demo Accounts

The backend automatically seeds the database on startup with three ready-to-test role accounts:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | `ADMIN` | Full permissions across all features, settings, and users |
| `manager` | `Manager@123` | `WAREHOUSE_MANAGER` | Stock movements, purchase orders, catalog, forecasting |
| `vendor` | `Vendor@123` | `VENDOR` | Supplier view with access to assigned catalog products and purchase orders |

---

## 6. Getting Started & Running Locally

### Prerequisites
- **Java**: JDK 21 installed (`java -version`)
- **Maven**: 3.9+ installed (`mvn -v`)
- **Node.js**: v20+ or v24+ installed (`node -v`)
- **Python**: 3.10+ installed (`python --version`)
- **MySQL**: MySQL 8.0 running on `localhost:3306`

---

### Step 1: Database Setup
Ensure MySQL is running on port 3306, then create the database:
```sql
CREATE DATABASE IF NOT EXISTS smartshelfx;
```
*(Database credentials can be adjusted in `backend/src/main/resources/application.properties`)*.

---

### Step 2: Start the Python AI Microservice
```powershell
cd ai-engine
.\.venv\Scripts\Activate.ps1
python app.py
```
*AI service will start on `http://localhost:5000` (Health check: `http://localhost:5000/health`).*

---

### Step 3: Start the Spring Boot Backend
```powershell
cd backend
mvn spring-boot:run
```
*Backend API will start on `http://localhost:8080` and seed initial catalog items & demo accounts.*

---

### Step 4: Start the Angular Frontend
```powershell
cd frontend
npm start
```
*Open your browser and navigate to `http://localhost:4200`.*

---

## 7. Running Automated Test Suites

### Backend Unit & Integration Tests (19 Tests)
```powershell
cd backend
mvn test
```
*Verifies AuthService, ProductService, PurchaseOrderService, StockTransactionService, CategoryService, and ForecastService.*

### Python AI Engine Unit Tests (4 Tests)
```powershell
cd ai-engine
.\.venv\Scripts\pytest tests
```
*Verifies DemandForecaster data cleaning, linear regression fit, chronological splits, MAE evaluation, and restocking formulas.*

### Frontend Production Build Verification
```powershell
cd frontend
npm run build
```
*Validates full TypeScript compilation, template checking, and lazy bundle generation.*

---

## 8. Detailed Documentation

Deep-dive documentation is available in the `docs/` folder:

- [Database Design & Schema Guide](file:///c:/Users/navee/Desktop/JavaBackend/docs/DATABASE_DESIGN.md): Tables, foreign keys, indexes, ER diagrams.
- [Project & Architecture Explanation](file:///c:/Users/navee/Desktop/JavaBackend/docs/PROJECT_EXPLANATION.md): Plain English walkthrough of every layer, class, and execution flow.
- [REST API Specification Guide](file:///c:/Users/navee/Desktop/JavaBackend/docs/API_GUIDE.md): Endpoints, JSON request bodies, responses, and authorization roles.
- [Technical Interview Preparation](file:///c:/Users/navee/Desktop/JavaBackend/docs/INTERVIEW_PREPARATION.md): High-value technical interview questions and architectural defenses.
