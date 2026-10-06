package com.example.restlab.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.LocalDateTime;

/**
 * Standardized API Response Envelope (Experiment 2.1.1 & 2.1.2)
 * Ensures consistent and predictable JSON structure across all endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;
    private String correlationId;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.correlationId = MDC.get("correlationId");
    }

    public ApiResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.correlationId = MDC.get("correlationId");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, 200, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "Operation completed successfully", data);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, 201, message, data);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, status, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
