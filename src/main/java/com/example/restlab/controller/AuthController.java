package com.example.restlab.controller;

import com.example.restlab.common.api.ApiResponse;
import com.example.restlab.model.dto.auth.AuthResponse;
import com.example.restlab.model.dto.auth.LoginRequest;
import com.example.restlab.model.dto.auth.RegisterRequest;
import com.example.restlab.model.dto.auth.TokenRefreshRequest;
import com.example.restlab.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication & Token Lifecycle Controller (Experiments 2.3.1 & 2.3.2)
 * Handles user onboarding, JWT token issuance, and Refresh Token Rotation.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "User registered successfully with initial JWT and Refresh Token"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful. JWT token issued."));
    }

    /**
     * Refresh Token Rotation Endpoint (Experiment 2.3.2)
     * Consumes current refresh token, invalidates it, and issues a fresh token pair.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Refresh token rotated successfully. New JWT issued."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody(required = false) TokenRefreshRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully. Refresh token invalidated."));
    }
}
