package com.example.restlab.service;

import com.example.restlab.common.exception.BadRequestException;
import com.example.restlab.common.exception.ResourceNotFoundException;
import com.example.restlab.common.exception.TokenRefreshException;
import com.example.restlab.common.exception.UnauthorizedException;
import com.example.restlab.model.dto.auth.AuthResponse;
import com.example.restlab.model.dto.auth.LoginRequest;
import com.example.restlab.model.dto.auth.RegisterRequest;
import com.example.restlab.model.dto.auth.TokenRefreshRequest;
import com.example.restlab.model.entity.RefreshToken;
import com.example.restlab.model.entity.Role;
import com.example.restlab.model.entity.User;
import com.example.restlab.repository.RefreshTokenRepository;
import com.example.restlab.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Authentication & Token Lifecycle Service (Experiments 2.3.1 & 2.3.2)
 * Manages user registration, credential verification, JWT issuance,
 * and secure Refresh Token Rotation.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMs;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        Role assignedRole = Role.ROLE_USER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
            assignedRole = Role.ROLE_ADMIN;
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                assignedRole
        );

        User savedUser = userRepository.save(user);
        log.info("New user registered successfully: '{}' with role '{}'", savedUser.getUsername(), savedUser.getRole());

        String accessToken = jwtService.generateAccessToken(savedUser);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getAccessTokenExpirationMs(),
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for username: '{}'", request.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }

        log.info("User '{}' authenticated successfully.", user.getUsername());

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getAccessTokenExpirationMs(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Refresh Token Rotation (Experiment 2.3.2)
     * Every refresh request invalidates the consumed refresh token and issues a new pair.
     * This protects against token replay and leak vulnerabilities.
     */
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String requestToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new TokenRefreshException(requestToken, "Refresh token not found in records"));

        if (refreshToken.isRevoked()) {
            log.error("Attempt to reuse revoked refresh token: {}", requestToken);
            throw new TokenRefreshException(requestToken, "Refresh token has been revoked! Re-authentication required.");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(requestToken, "Refresh token was expired. Please login again.");
        }

        User user = refreshToken.getUser();

        // ROTATION: Revoke or delete old refresh token
        refreshTokenRepository.delete(refreshToken);

        // Generate brand new refresh token and access token
        RefreshToken newRefreshToken = createRefreshToken(user);
        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Rotated refresh token successfully for user: '{}'", user.getUsername());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtService.getAccessTokenExpirationMs(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token on logout for user: '{}'", token.getUser().getUsername());
            });
        }
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken(
                user,
                UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""),
                Instant.now().plusMillis(refreshTokenExpirationMs)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
}
