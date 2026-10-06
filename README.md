# Enterprise RESTful APIs Lab - Spring Boot 3.4

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20%7C%2025%20LTS-orange.svg)](https://adoptium.net)
[![Security](https://img.shields.io/badge/Security-Spring%20Security%206%20%2B%20JWT%20%2B%20AES--256-blue.svg)](https://spring.io/projects/spring-security)
[![Tests](https://img.shields.io/badge/Tests-7%20Passed%20(100%25)-success.svg)]()

A comprehensive, production-grade Spring Boot application implementing the complete series of backend experiments (**Experiments 2.1.1 through 2.3.2**).

---

## 🎯 Experiments Implemented

| Experiment | Title | Core Technologies & Concepts |
| :--- | :--- | :--- |
| **Exp 2.1.1** | **RESTful APIs Using Spring Boot** | Layered Architecture (Controller, Service, Repository), Post & Schedule CRUD, Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Future`), Standardized `ApiResponse<T>`, Global CORS |
| **Exp 2.1.2** | **Global Exception Handling & Logging** | `@RestControllerAdvice` (`GlobalExceptionHandler`), Custom Exceptions, Structured Logging Filter, Correlation ID Tracing via SLF4J MDC (`X-Correlation-ID`), `ErrorResponse` |
| **Exp 2.2.1** | **Pagination & Sorting REST APIs** | Scalable Read APIs via Spring Data `Pageable` & `Sort`, multi-criteria sorting (`sort=title,asc&sort=createdAt,desc`), metadata-rich `PagedResponse<T>` |
| **Exp 2.2.2** | **Backend Performance & Caching** | Solving N+1 Query Problem with `JOIN FETCH`, High-Throughput In-Memory Caching (Caffeine: `@Cacheable`, `@CachePut`, `@CacheEvict`), Native SQL Query Aggregation, Performance Benchmarking endpoints |
| **Exp 2.3.1** | **JWT Authentication & RBAC** | Spring Security 6 stateless filter chain, `JwtAuthenticationFilter`, Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`), Method Security with `@PreAuthorize` |
| **Exp 2.3.2** | **AES Encryption & Token Rotation** | Military-grade AES-256 GCM authenticated encryption for sensitive OAuth credentials, Secure Credential Vault, Access Token + Refresh Token Rotation with replay attack protection |

---

## 🚀 Quick Start

### Run the Application
You can start the server immediately using the provided batch script or PowerShell:
```cmd
run-app.cmd
```
or via PowerShell:
```powershell
.\run-app.ps1
```
or using Maven directly:
```bash
mvn spring-boot:run
```
The application will boot on **`http://localhost:8080`**.

---

## 🔑 Default Accounts (Pre-Seeded)

The application automatically seeds test accounts on startup:

| Username | Password | Role | Access Scope |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | `ROLE_ADMIN` | Full administrative access, delete any post, decrypt credentials vault |
| **`john_doe`** | `user123` | `ROLE_USER` | Create/edit/delete own posts, manage own schedules & credentials |
| **`jane_smith`** | `user123` | `ROLE_USER` | Regular user |

---

## 📬 Postman Collection

A complete Postman collection is ready in the repository:
```
postman/REST_API_Enterprise_Lab.postman_collection.json
```
- Open Postman -> **Import** -> Select `postman/REST_API_Enterprise_Lab.postman_collection.json`.
- The collection features automated test scripts that automatically capture and save the `accessToken` and `refreshToken` upon login!

---

## 📊 Endpoints Summary

### Authentication & Token Lifecycle (`/api/v1/auth`)
- `POST /register` - Register a new user account with initial JWT & Refresh Token
- `POST /login` - Authenticate and obtain JWT Access Token (15 min) and Refresh Token (7 days)
- `POST /refresh` - **Refresh Token Rotation**: Invalidate old token and issue fresh token pair
- `POST /logout` - Invalidate and revoke active refresh token

### Posts & Caching (`/api/v1/posts`)
- `POST /` - Create post (Bean Validation enforced)
- `GET /{id}` - Get post by ID (**Cached via Caffeine in RAM**)
- `PUT /{id}` - Update post (**Refreshes cache via `@CachePut`**)
- `DELETE /{id}` - Delete post (**Evicts cache via `@CacheEvict`**, RBAC enforced)
- `GET /` - **Paginated & Sortable** (`?page=0&size=10&sort=createdAt,desc`)
- `GET /status/{status}` - Filter posts by status with pagination
- `GET /optimized-fetch` - **JOIN FETCH single-query solution** resolving N+1 problem
- `GET /native-summary` - **Native SQL analytical query** aggregating counts by status

### Scheduling (`/api/v1/schedules`)
- `POST /` - Schedule a post (`@Future` validation for schedule time)
- `GET /{id}` - Retrieve schedule details
- `GET /post/{postId}` - List all schedules for a post
- `PUT /{id}` - Update schedule status or timing
- `DELETE /{id}` - Delete schedule

### Secure Credential Vault (`/api/v1/vault/credentials`)
- `POST /` - Store OAuth credentials (Encrypted with **AES-256 GCM**)
- `GET /` - List credentials with masked secret view (`sk_******12`)
- `GET /{id}/decrypt` - Decrypt sensitive credentials (**`ROLE_ADMIN` only**)

### Performance Benchmarking (`/api/v1/benchmark`)
- `GET /n-plus-one` - Executes and measures unoptimized lazy-loading N+1 query problem
- `GET /join-fetch` - Executes and measures single JOIN FETCH query
- `GET /cache-test/{id}` - Executes 5 back-to-back requests, measuring cache miss vs RAM cache hits

---

## 🗄️ H2 Database Console
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:restlabdb`
- **User:** `sa`
- **Password:** *(leave blank)*

---

## 🧪 Running Automated Tests
Run the integration test suite:
```bash
mvn test
```
All 7 test suites verify Bean validation, Correlation IDs, Pagination, JOIN FETCH, AES Cryptography, and Token Rotation.

---
For in-depth theory, architecture diagrams, and benchmark data, see **[`EXPERIMENTS_LAB_REPORT.md`](file:///c:/Users/imjay/OneDrive/Documents/Development/FS1-CSS-Layout/EXPERIMENTS_LAB_REPORT.md)**.
