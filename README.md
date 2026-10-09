# Supply Chain Management System

A locally runnable college mini project for managing suppliers, products, stock, orders, deliveries, and simple reports.

## Tech Stack

- Backend: Java 21, Spring Boot, Spring Web, Spring Data JPA, Bean Validation, Flyway, MariaDB driver, Maven Wrapper
- Frontend: React, Vite, TypeScript
- Database: XAMPP MariaDB on `localhost:3306`, database `supply_chain`

Authentication, deployment, payments, live GPS tracking, and advanced analytics are intentionally out of scope for this first version.

## Project Structure

```text
backend/    Spring Boot REST API
frontend/   React + Vite dashboard
database/   Safe database creation script
```

## Prerequisites

- XAMPP MariaDB running on port `3306`
- Java 21. On this machine, Java is available at:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

- Node.js and npm. Verified here: Node `v24.9.0`, npm `11.6.0`
- Maven does not need to be installed globally; use `backend\mvnw.cmd`

## Database Setup

This project never drops or resets the database automatically.

Create the database only if it does not already exist:

```powershell
mysql -u root -p < database\init-database.sql
```

If `mysql` is not on `PATH`, use the XAMPP client path instead:

```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -p < database\init-database.sql
```

For a blank local XAMPP root password, press Enter when prompted. Flyway creates the tables from `backend/src/main/resources/db/migration/V1__create_supply_chain_schema.sql` when the backend starts.

## Backend Configuration

Use environment variables for local credentials:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mariadb://localhost:3306/supply_chain"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD=""
$env:APP_CORS_ALLOWED_ORIGINS="http://localhost:5173"
```

Alternatively, copy `backend/src/main/resources/application-example.properties` to `backend/src/main/resources/application-local.properties`, then run with the `local` profile. Keep `application-local.properties` uncommitted.

## Run Backend

```powershell
cd backend
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

Backend API runs at `http://localhost:8080/api`.

## Run Frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

## Important API Routes

- `GET /api/dashboard/summary`
- `GET /api/dashboard/recent-orders`
- `GET /api/products`, `POST /api/products`, `PUT /api/products/{id}`, `DELETE /api/products/{id}`
- `GET /api/suppliers`, `POST /api/suppliers`, `PUT /api/suppliers/{id}`, `DELETE /api/suppliers/{id}`
- `GET /api/orders`, `POST /api/orders`, `PATCH /api/orders/{id}/status`
- `GET /api/deliveries`
- `GET /api/reports/summary`

## Business Rules Implemented

- Product SKUs are unique.
- Product price and stock cannot be negative.
- Orders contain one product for this mini project.
- Creating an order checks stock and decreases stock inside a transaction.
- Insufficient stock rejects the order without changing stock.
- Order price is stored as a snapshot.
- Valid transitions: `PENDING -> SHIPPED`, `PENDING -> CANCELLED`, `SHIPPED -> DELIVERED`.
- Cancelling a pending order restores stock exactly once.
- Delivered and cancelled orders are terminal.
- Delivery delay is calculated from expected and actual/current dates.
- Dashboard and reports use persisted backend data.

## Verified Checks

Backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Result: 9 tests run, 0 failures, 0 errors.

Frontend build:

```powershell
cd frontend
npm run build
```

Result: TypeScript and Vite production build completed successfully.

## Manual Demo Checklist

1. Start XAMPP MariaDB.
2. Create `supply_chain` if needed using `database\init-database.sql`.
3. Start the backend.
4. Start the frontend.
5. Add a supplier.
6. Add a product linked to that supplier.
7. Create an order with a valid quantity and confirm product stock decreases.
8. Try creating an order with too much quantity and confirm it is rejected.
9. Move a pending order to shipped, then delivered.
10. Create another pending order, cancel it, and confirm stock is restored once.
11. Check dashboard, deliveries, and reports reflect the database state.

## Troubleshooting

- `java` not recognized: set `JAVA_HOME` and update `Path` as shown above.
- Backend cannot connect to database: confirm XAMPP MariaDB is running, port `3306` is free, and `supply_chain` exists.
- Access denied for database user: set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` for your local MariaDB user.
- Frontend cannot load data: confirm backend is running on `8080` and `APP_CORS_ALLOWED_ORIGINS` includes `http://localhost:5173`.
- Port conflict: set `SERVER_PORT` for the backend or change `frontend/vite.config.ts` for Vite.

## Known Limitations

- No login or roles yet; this is for local learning/demo use only.
- Orders support one product per order.
- Product deletion soft-deactivates products with order history.
- No sample data is inserted automatically.
