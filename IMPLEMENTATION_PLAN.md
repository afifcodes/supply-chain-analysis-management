# Supply Chain Management System Implementation Plan

## Current Workspace And Tooling

- Workspace: `D:\projects\supply-chain-management`
- Existing project files: none found during initial inspection
- Git: `git version 2.54.0.windows.1`
- Java: not on `PATH`; usable JDK found at `C:\Program Files\Android\Android Studio\jbr` (`openjdk 21.0.7`)
- Maven: not on `PATH`; project will include Maven Wrapper so builds do not require a global Maven install
- Node.js: `v24.9.0`
- npm: `11.6.0`
- Database target: local XAMPP MariaDB on `localhost:3306`, database `supply_chain`

## Implementation Assumptions

- This is a college mini project, so orders will contain one product per order.
- Authentication, deployment, payments, live tracking, and advanced analytics are intentionally excluded.
- Database credentials will be supplied through environment variables or ignored local config.
- The project will provide SQL to create the database and schema, but will not drop or reset existing data automatically.
- Backend tests will use an in-memory H2 database in MySQL compatibility mode for business-rule coverage.

## Phase Checklist

### Phase A - Inspect And Plan

- [x] Inspect workspace before changing project files.
- [x] Read `PRD.md` completely.
- [x] Read `CODEX_PROMPT.md` completely.
- [x] Check Java, Maven, Node.js, npm, and Git availability.
- [x] Create this implementation plan.

### Phase B - Backend And Database

- [x] Scaffold Spring Boot backend with Maven Wrapper.
- [x] Add safe configuration examples and ignored local config.
- [x] Add versioned SQL schema for `supply_chain`.
- [x] Implement JPA entities, repositories, DTOs, validation, services, controllers, CORS, and exception handling.
- [x] Implement transactional order creation, stock decrease, valid status transitions, cancellation, restocking, and delivery delay calculation.
- [x] Add focused backend tests for important business rules.
- [x] Run backend tests/build checks and fix errors.

### Phase C - Frontend

- [x] Scaffold React + Vite + TypeScript frontend.
- [x] Build responsive dashboard shell with sidebar, top bar, tables, forms, badges, loading/error/empty states.
- [x] Implement products, suppliers, orders, delivery tracking, and reports.
- [x] Connect every screen to backend REST APIs through one API client.
- [x] Run frontend build checks and fix errors.

### Phase D - Integration

- [x] Document how to create the database only if it does not exist.
- [x] Document backend and frontend local run commands.
- [x] Verify available automated tests.
- [x] Provide a manual end-to-end demo checklist.

### Phase E - Handoff

- [x] Update `README.md` with prerequisites, setup, configuration, schema initialization, commands, troubleshooting, and known limitations.
- [x] Summarize created/changed files and actual checks run.
