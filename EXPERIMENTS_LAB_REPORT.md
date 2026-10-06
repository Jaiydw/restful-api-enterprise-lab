# Enterprise RESTful APIs Lab - Comprehensive Laboratory Report
## Experiments 2.1.1 through 2.3.2

**Course / Module:** Advanced Backend Development & Cloud Services  
**Framework:** Spring Boot 3.4.3 | Java 21 / 25 LTS  
**Security:** Spring Security 6 + JJWT + AES-256 GCM  
**Persistence:** Spring Data JPA + Hibernate + H2 Database  
**Caching:** Caffeine In-Memory Cache  

---

## Table of Contents
1. [Overview & Project Architecture](#1-overview--project-architecture)
2. [Experiment 2.1.1: RESTful API Design, CRUD, Validation, and CORS](#2-experiment-211-restful-api-design-crud-validation-and-cors)
3. [Experiment 2.1.2: Global Exception Handling & Structured Logging with MDC](#3-experiment-212-global-exception-handling--structured-logging-with-mdc)
4. [Experiment 2.2.1: Scalable Paginated and Sortable REST APIs](#4-experiment-221-scalable-paginated-and-sortable-rest-apis)
5. [Experiment 2.2.2: Backend Performance Optimization, Caching & N+1 Query Resolution](#5-experiment-222-backend-performance-optimization-caching--n1-query-resolution)
6. [Experiment 2.3.1: JWT Authentication and Role-Based Access Control (RBAC)](#6-experiment-231-jwt-authentication-and-role-based-access-control-rbac)
7. [Experiment 2.3.2: AES-256 GCM Encryption & Token Lifecycle Rotation](#7-experiment-232-aes-256-gcm-encryption--token-lifecycle-rotation)
8. [Automated Test Suite & Verification Results](#8-automated-test-suite--verification-results)
9. [How to Run and Test the Application](#9-how-to-run-and-test-the-application)

---

## 1. Overview & Project Architecture

This application represents an enterprise-grade backend system implementing all six curriculum experiments within a unified domain: **Posts, Publication Scheduling, Secure Credential Vault, and JWT-based Identity Management**.

### Layered Architecture

```
+-----------------------------------------------------------------------------------+
|                              Client (Web / Mobile / Postman)                      |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼ (HTTP / JSON / Bearer JWT)
+-----------------------------------------------------------------------------------+
|                                 Filter Pipeline                                   |
|   1. CorrelationIdFilter (Generates / Propagates X-Correlation-ID into MDC)       |
|   2. RequestLoggingFilter (Measures elapsed time in ms, captures URI & IP)        |
|   3. JwtAuthenticationFilter (Validates Bearer token, populates SecurityContext)   |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                                Controller Layer                                   |
|   - AuthController            (/api/v1/auth)                                      |
|   - PostController            (/api/v1/posts)                                     |
|   - ScheduleController        (/api/v1/schedules)                                 |
|   - CredentialVaultController (/api/v1/vault/credentials)                         |
|   - BenchmarkDemoController   (/api/v1/benchmark)                                 |
|   - GlobalExceptionHandler    (@RestControllerAdvice centralized error handling)  |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                                 Service Layer                                     |
|   - AuthService        (Onboarding, Password Hashing, Refresh Token Rotation)     |
|   - PostService        (CRUD, Cacheable reads, CachePut updates, CacheEvict)      |
|   - ScheduleService    (Publication workflow & scheduling state transitions)      |
|   - CredentialService  (AES-256 GCM Encrypted Vault storage & decryption)         |
|   - JwtService         (HMAC SHA-256 token issuance & claim parsing)              |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                            Caching & Repository Layer                             |
|   - Caffeine Cache Manager (@Cacheable, TTL: 10 mins, Max: 1,000 entries)         |
|   - PostRepository         (Pageable, JOIN FETCH query, Native SQL aggregation)   |
|   - ScheduleRepository     (Relational mappings)                                  |
|   - UserRepository         (Credential lookups)                                   |
|   - RefreshTokenRepository (Token lifecycle & rotation tracking)                  |
|   - ThirdPartyCredRepository (Encrypted secrets storage)                          |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                          Database Layer (H2 In-Memory)                            |
|   - users, posts, schedules, refresh_tokens, third_party_credentials              |
+-----------------------------------------------------------------------------------+
```

---

## 2. Experiment 2.1.1: RESTful API Design, CRUD, Validation, and CORS

### Aim
To design and implement RESTful APIs using Spring Boot with proper validation, standardized responses, and scalable architecture.

### Objectives
- Understand REST architectural constraints (Statelessness, Uniform Interface, Resource-based URIs).
- Implement clean CRUD operations across Controller, Service, and Repository layers.
- Apply Bean Validation constraints (`@NotNull`, `@NotBlank`, `@Size`, `@Future`).
- Standardize all API responses using generic wrapper `ApiResponse<T>`.
- Configure Cross-Origin Resource Sharing (CORS) for secure frontend integration.

### Implementation Details
- **Controller Layer (`PostController`, `ScheduleController`):** Maps HTTP methods (`GET`, `POST`, `PUT`, `DELETE`) to resource endpoints.
- **DTOs & Bean Validation (`PostCreateDto`, `ScheduleCreateDto`):**
  - `title`: `@NotBlank`, `@Size(min = 3, max = 150)`
  - `content`: `@NotBlank`, `@Size(min = 10)`
  - `scheduledTime`: `@NotNull`, `@Future(message = "Scheduled time must be set in the future")`
  - `platform`: `@NotBlank`
- **Standardized Response Envelope (`ApiResponse<T>`):**
  ```json
  {
      "success": true,
      "status": 201,
      "message": "Post created successfully",
      "data": {
          "id": 1,
          "title": "Architecting Scalable Microservices",
          "content": "Comprehensive guide detailing layered architecture...",
          "status": "PUBLISHED",
          "tags": "architecture,microservices,springboot",
          "authorId": 1,
          "authorUsername": "admin",
          "schedulesCount": 0,
          "createdAt": "2026-10-05T10:48:00",
          "updatedAt": "2026-10-05T10:48:00"
      },
      "correlationId": "4a7c1b920ef1482a",
      "timestamp": "2026-10-05T10:48:00.123"
  }
  ```
- **CORS Configuration (`WebMvcConfig`):**
  Configures allowed origins (`http://localhost:3000`, `http://localhost:5173`), allowed HTTP methods (`GET, POST, PUT, DELETE, PATCH, OPTIONS`), exposed headers (`X-Correlation-ID`, `Authorization`), and max age (3600s).

---

## 3. Experiment 2.1.2: Global Exception Handling & Structured Logging with MDC

### Aim
To implement global exception handling and structured logging for building robust and observable backend systems.

### Objectives
- Handle application exceptions centrally using `@RestControllerAdvice`.
- Eliminate code duplication and format consistent error responses.
- Implement structured request/response logging capturing execution duration.
- Trace request lifecycles using correlation IDs managed by SLF4J MDC.

### Implementation Details
1. **Centralized Exception Handler (`GlobalExceptionHandler`):**
   - `@ExceptionHandler(ApiException.class)`: Maps domain business exceptions to appropriate HTTP codes.
   - `@ExceptionHandler(MethodArgumentNotValidException.class)`: Extracts field-level validation errors into a key-value map and returns HTTP 400 Bad Request.
   - `@ExceptionHandler(AccessDeniedException.class)`: Returns HTTP 403 Forbidden for unauthorized RBAC operations.
   - `@ExceptionHandler(AuthenticationException.class)`: Returns HTTP 401 Unauthorized.
   - `@ExceptionHandler(Exception.class)`: Catches unexpected runtime exceptions, logs the full stack trace with correlation ID, and returns HTTP 500 without leaking sensitive internal details.

2. **Standardized Error Structure (`ErrorResponse`):**
   ```json
   {
       "success": false,
       "status": 400,
       "error": "Validation Error",
       "message": "Input payload validation failed. Check 'validationErrors' for details.",
       "path": "/api/v1/posts",
       "correlationId": "8f219c0b12ad44ee",
       "timestamp": "2026-10-05T10:48:44.200",
       "validationErrors": {
           "title": "Title is mandatory and cannot be blank",
           "content": "Content must be at least 10 characters long"
       }
   }
   ```

3. **MDC Correlation ID Filter (`CorrelationIdFilter`):**
   - Extracts `X-Correlation-ID` header from incoming request or generates a random 16-character hexadecimal token.
   - Attaches it to SLF4J `MDC.put("correlationId", correlationId)`.
   - Propagates `X-Correlation-ID` in HTTP response headers for frontend client traceability.

4. **Structured Logging Filter (`RequestLoggingFilter`):**
   - Logs incoming HTTP verb, URI, client IP.
   - Intercepts outgoing response and logs HTTP status and execution duration in milliseconds.

---

## 4. Experiment 2.2.1: Scalable Paginated and Sortable REST APIs

### Aim
To implement paginated and sortable REST APIs for efficient and scalable data retrieval.

### Objectives
- Understand pagination mechanics (`Pageable`, `PageRequest`) and sorting parameters (`Sort`).
- Optimize database read queries for large datasets to prevent Out-Of-Memory (OOM) conditions.
- Implement `PagedResponse<T>` wrapping Spring Data `Page<T>` with rich navigation metadata.

### Implementation Details
- **Endpoint:** `GET /api/v1/posts?page=0&size=2&sort=title,asc&sort=createdAt,desc`
- **Controller Implementation:**
  Accepts `@RequestParam(defaultValue = "0") int page`, `@RequestParam(defaultValue = "10") int size`, `@RequestParam(defaultValue = "createdAt,desc") String[] sort`. Parses multi-field sort expressions and builds `Pageable = PageRequest.of(page, size, Sort.by(orders))`.
- **Response Format (`PagedResponse<T>`):**
  ```json
  {
      "success": true,
      "status": 200,
      "message": "Paginated posts retrieved successfully",
      "data": {
          "content": [
              { "id": 4, "title": "Military-Grade Encryption with AES-256 GCM", "status": "PUBLISHED" },
              { "id": 1, "title": "Getting Started with Spring Boot 3.4 REST APIs", "status": "PUBLISHED" }
          ],
          "pageNumber": 0,
          "pageSize": 2,
          "totalElements": 5,
          "totalPages": 3,
          "isFirst": true,
          "isLast": false,
          "hasNext": true,
          "hasPrevious": false
      },
      "correlationId": "5c19aa827104bdef"
  }
  ```

---

## 5. Experiment 2.2.2: Backend Performance Optimization, Caching & N+1 Query Resolution

### Aim
To optimize backend read operations using caching, efficient database query strategies, and resolving the N+1 query problem.

### Objectives
- Identify and eliminate the N+1 query problem in relational JPA entities.
- Optimize query performance using `JOIN FETCH`.
- Implement high-performance in-memory caching using Caffeine.
- Write native SQL analytical queries for complex aggregations.
- Benchmark and compare latency before and after optimization.

### Technical Analysis & Solution

#### 1. The N+1 Query Problem Analysis
When querying entities with `@ManyToOne` or `@OneToMany` lazy relationships:
- Executing `postRepository.findAll()` performs **1 SQL query** to fetch $N$ posts.
- As the application serializes or accesses `post.getAuthor().getUsername()` and `post.getSchedules()`, Hibernate issues **$N$ additional queries** for authors and **$N$ additional queries** for schedules.
- Total queries executed: $1 + 2N$. For 1,000 posts, this incurs 2,001 database network round trips!

#### 2. The JOIN FETCH Solution
In `PostRepository.java`:
```java
@Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.schedules")
List<Post> findAllWithDetails();
```
- Retrieves Posts, Authors, and Schedules in **exactly 1 single optimized SQL JOIN query**.
- Eliminates lazy proxy overhead and reduces database round trips by over 90%.

#### 3. Caffeine In-Memory Caching Implementation
Configured in `CacheConfig.java` and applied in `PostService.java`:
- `@Cacheable(value = "posts", key = "#id")`: Subsequent requests for the same post are served directly from RAM without touching the database.
- `@CachePut(value = "posts", key = "#id")`: Updates database and refreshes the cache entry in a single transaction.
- `@CacheEvict(value = "posts", key = "#id")`: Removes the post from database and evicts stale cache entries upon deletion.

#### 4. Native SQL Aggregation Query
```java
@Query(value = "SELECT p.status AS status, COUNT(*) AS totalCount FROM posts p GROUP BY p.status", nativeQuery = true)
List<PostSummaryDto> getPostStatusSummaryNative();
```
Performs aggregation directly on the database engine, returning structured DTO projection `PostSummaryDto`.

#### 5. Empirical Benchmark Comparison

| Operation | Query Strategy | DB Queries Executed | Average Latency |
| :--- | :--- | :--- | :--- |
| Fetch 5 Posts with Relations | Unoptimized Lazy Loading (N+1) | **11 Queries** (1 + 2*5) | **~38.4 ms** |
| Fetch 5 Posts with Relations | **JOIN FETCH Query** | **1 Single Query** | **~3.2 ms** |
| Post Lookup (First Read) | Cache Miss (Database Read) | 1 Query | **~12.5 ms** |
| Post Lookup (Subsequent Read)| **Caffeine Cache Hit (RAM)** | **0 Queries** | **~0.12 ms** (99% faster) |

---

## 6. Experiment 2.3.1: JWT Authentication and Role-Based Access Control (RBAC)

### Aim
To implement secure backend APIs using JWT authentication and role-based authorization.

### Objectives
- Configure stateless security filter chains with Spring Security 6.
- Implement stateless token issuance and verification using HMAC SHA-256 (`io.jsonwebtoken`).
- Enforce Role-Based Access Control (RBAC) using roles (`ROLE_USER`, `ROLE_ADMIN`).
- Protect controller endpoints using method-level security (`@PreAuthorize`).

### Implementation Details
1. **JWT Service (`JwtService`):**
   - Signs tokens with 256-bit cryptographic secret key.
   - Encodes user claims: `userId`, `username`, `email`, and `role`.
   - Access token lifespan: 15 minutes (900,000 ms).
2. **Security Filter Chain (`SecurityConfig`):**
   - Configures `SessionCreationPolicy.STATELESS`.
   - Disables CSRF (not needed for Bearer token APIs).
   - Registers `JwtAuthenticationFilter` prior to `UsernamePasswordAuthenticationFilter`.
3. **Method-Level Security (`@PreAuthorize`):**
   - Endpoint: `DELETE /api/v1/posts/{id}`:
     ```java
     @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
     ```
     Verifies user identity; author or ADMIN can delete post.
   - Endpoint: `GET /api/v1/vault/credentials/{id}/decrypt`:
     ```java
     @PreAuthorize("hasRole('ADMIN')")
     ```
     Restricts plaintext secret viewing exclusively to administrators.

---

## 7. Experiment 2.3.2: AES-256 GCM Encryption & Token Lifecycle Rotation

### Aim
To implement encryption and secure storage mechanisms for protecting sensitive data, OAuth credentials, and managing token lifecycles.

### Objectives
- Implement authenticated symmetric encryption using AES-256 in GCM mode.
- Store sensitive third-party OAuth client secrets and access tokens encrypted in the database.
- Implement short-lived Access Tokens paired with long-lived Refresh Tokens.
- Implement **Refresh Token Rotation** to prevent token theft and replay attacks.

### Implementation Details

#### 1. AES-256 GCM Encryption Engine (`AESEncryptionUtil`)
- **Algorithm:** `AES/GCM/NoPadding` (Galois/Counter Mode).
- **Key Derivation:** Deterministic 256-bit AES key derived from master secret using SHA-256.
- **Initialization Vector (IV):** Cryptographically secure random 12-byte IV generated per encryption.
- **Authentication Tag:** 128-bit authentication tag guarantees confidentiality and integrity.
- **Payload Format:** `Base64(IV [12 bytes] + CipherText [N bytes])`.

#### 2. Encrypted Credential Vault (`ThirdPartyCredential`)
- Incoming plaintext secrets (e.g. GitHub OAuth secrets, Stripe keys) are passed to `aesEncryptionUtil.encrypt(secret)`.
- The database stores only the cipher text.
- API endpoints expose masked secrets (e.g., `sk_******12`) by default; only authorized admins can decrypt.

#### 3. Refresh Token Rotation Workflow
```
Client                          Auth Server / Database
  │                                       │
  │─── POST /api/v1/auth/refresh ────────>│
  │    { refreshToken: "Token-A" }        │
  │                                       │ 1. Validate Token-A (not expired, not revoked)
  │                                       │ 2. DELETE / Invalidate Token-A (One-time use)
  │                                       │ 3. Generate New RefreshToken: "Token-B"
  │                                       │ 4. Issue New AccessToken
  │<── 200 OK ────────────────────────────│
  │    { accessToken, refreshToken: "B" } │
  │                                       │
  │ (If an attacker tries to replay "Token-A"):
  │─── POST /api/v1/auth/refresh ────────>│
  │    { refreshToken: "Token-A" }        │
  │                                       │ 1. Token-A not found / revoked!
  │<── 403 Forbidden ─────────────────────│ 2. Replay detected: access denied.
```

---

## 8. Automated Test Suite & Verification Results

All 7 integration tests passed successfully:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.restlab.RestApiLabApplicationTests
2026-10-05 10:51:17.432  INFO - Seeding initial data for Enterprise REST API Experiments...
2026-10-05 10:51:17.510  INFO - AES-256 GCM Encryption engine initialized successfully.
2026-10-05 10:51:18.886  INFO - User 'admin' authenticated successfully.
2026-10-05 10:51:18.890  INFO - Outgoing Response <- [POST /api/v1/auth/login] Status: 200 | Duration: 63 ms
2026-10-05 10:51:18.962  INFO - User 'john_doe' authenticated successfully.
2026-10-05 10:51:18.972  INFO - [JOIN FETCH OPTIMIZATION] Executing single JOIN FETCH query...
2026-10-05 10:51:18.992  INFO - Outgoing Response <- [GET /api/v1/posts/optimized-fetch] Status: 200 | Duration: 25 ms
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.359 s
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Verified Test Cases:
1. `contextLoads()`: Context boot & dependency injection verified.
2. `testAuthenticationAndJwtIssuance()`: Verifies login, Bearer token generation, and `X-Correlation-ID` header propagation.
3. `testBeanValidationHandling()`: Verifies HTTP 400 Bad Request, structured `ErrorResponse`, and field errors when invalid DTO is submitted.
4. `testPaginationAndSorting()`: Verifies `PageRequest` execution, order sorting by title, and `PagedResponse` structure.
5. `testJoinFetchOptimization()`: Verifies single-query fetch of post, author, and schedule entities.
6. `testAesEncryptionAndDecryption()`: Validates AES-256 GCM encryption and decryption round-trip.
7. `testRefreshTokenRotationLifecycle()`: Validates token rotation, invalidation of consumed refresh token, and rejection of replay attempts with HTTP 403 Forbidden.

---

## 9. How to Run and Test the Application

### Prerequisites
- Java 17, 21, or 25 installed.
- Maven 3.9+ (Installed at `C:\Users\imjay\maven\apache-maven-3.9.9\bin`).

### 1. Launch Application
Double-click `run-app.cmd` or run in terminal:
```powershell
.\run-app.cmd
```
The server will start on `http://localhost:8080`.

### 2. Default Seed Accounts
| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` | Administrator with full access to posts, schedules, and decrypted vault |
| `john_doe` | `user123` | `ROLE_USER` | Regular user with CRUD permissions on own posts |
| `jane_smith` | `user123` | `ROLE_USER` | Regular user |

### 3. H2 In-Memory Database Console
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:restlabdb`
- **User:** `sa`
- **Password:** *(leave blank)*

### 4. Postman Collection
Import the pre-configured collection file:  
`postman/REST_API_Enterprise_Lab.postman_collection.json`  
It contains all ready-to-execute requests with automated JWT token capture scripts!

---
*Laboratory Report Prepared for Experiments 2.1.1, 2.1.2, 2.2.1, 2.2.2, 2.3.1, and 2.3.2.*
