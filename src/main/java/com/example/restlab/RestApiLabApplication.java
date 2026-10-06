package com.example.restlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RESTful API Enterprise Lab - Main Application
 * Integrates:
 * - Experiment 2.1.1: RESTful API Layered Architecture, Bean Validation, Standardized Responses, CORS
 * - Experiment 2.1.2: Global Exception Handling (@ControllerAdvice) & Structured Logging (MDC Correlation IDs)
 * - Experiment 2.2.1: Pagination & Sorting via Spring Data Pageable
 * - Experiment 2.2.2: Backend Caching (Caffeine), N+1 Problem Resolution (JOIN FETCH), Native SQL Aggregations
 * - Experiment 2.3.1: JWT Authentication & Role-Based Access Control (RBAC) with Spring Security
 * - Experiment 2.3.2: AES-256 GCM Sensitive Data Encryption & Refresh Token Rotation Lifecycle
 */
@SpringBootApplication
public class RestApiLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApiLabApplication.class, args);
    }
}
