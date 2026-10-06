package com.example.restlab;

import com.example.restlab.common.util.AESEncryptionUtil;
import com.example.restlab.model.dto.auth.LoginRequest;
import com.example.restlab.model.dto.auth.RegisterRequest;
import com.example.restlab.model.dto.post.PostCreateDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Tests covering all 6 Experiments:
 * - Exp 2.1.1: CRUD & Bean Validation
 * - Exp 2.1.2: Global Exception Handling & Correlation ID
 * - Exp 2.2.1: Pagination & Sorting
 * - Exp 2.2.2: JOIN FETCH & Caching
 * - Exp 2.3.1: JWT Authentication & RBAC
 * - Exp 2.3.2: AES Encryption & Refresh Token Rotation
 */
@SpringBootTest
@AutoConfigureMockMvc
class RestApiLabApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AESEncryptionUtil aesEncryptionUtil;

    @Test
    @DisplayName("Context Loads Verification")
    void contextLoads() {
        assertNotNull(aesEncryptionUtil);
    }

    @Test
    @DisplayName("Exp 2.3.1: JWT Authentication - Login and Token Issuance")
    void testAuthenticationAndJwtIssuance() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"))
                .andReturn();

        // Verify Correlation ID header was injected (Exp 2.1.2)
        assertNotNull(result.getResponse().getHeader("X-Correlation-ID"));
    }

    @Test
    @DisplayName("Exp 2.1.2: Global Exception Handling & Bean Validation Failure")
    void testBeanValidationHandling() throws Exception {
        // Obtain Auth Token first
        String token = obtainAccessToken("admin", "admin123");

        // Blank title and too short content to trigger Bean Validation errors
        PostCreateDto invalidDto = new PostCreateDto("", "short", null, "");

        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    @DisplayName("Exp 2.2.1: Pagination and Sorting Verification")
    void testPaginationAndSorting() throws Exception {
        String token = obtainAccessToken("john_doe", "user123");

        mockMvc.perform(get("/api/v1/posts?page=0&size=2&sort=title,asc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.totalPages").isNumber());
    }

    @Test
    @DisplayName("Exp 2.2.2: JOIN FETCH Query Verification")
    void testJoinFetchOptimization() throws Exception {
        String token = obtainAccessToken("john_doe", "user123");

        mockMvc.perform(get("/api/v1/posts/optimized-fetch")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Exp 2.3.2: AES-256 GCM Cryptography Utility Verification")
    void testAesEncryptionAndDecryption() {
        String sensitiveOAuthSecret = "mock_oauth_secret_token_value_1234567890!@#$";
        String encrypted = aesEncryptionUtil.encrypt(sensitiveOAuthSecret);

        assertNotNull(encrypted);
        assertNotEquals(sensitiveOAuthSecret, encrypted);

        String decrypted = aesEncryptionUtil.decrypt(encrypted);
        assertEquals(sensitiveOAuthSecret, decrypted);
    }

    @Test
    @DisplayName("Exp 2.3.2: Refresh Token Rotation Lifecycle")
    void testRefreshTokenRotationLifecycle() throws Exception {
        // 1. Register a fresh user
        String username = "rotation_user_" + System.currentTimeMillis();
        RegisterRequest registerReq = new RegisterRequest(username, "secret123", username + "@example.com", "ROLE_USER");

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String initialRefreshToken = regJson.path("data").path("refreshToken").asText();

        // 2. Perform Token Refresh (Rotates Refresh Token)
        String refreshPayload = String.format("{\"refreshToken\":\"%s\"}", initialRefreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String newRefreshToken = refreshJson.path("data").path("refreshToken").asText();

        // Assert that the old refresh token was invalidated and replaced with a new token
        assertNotEquals(initialRefreshToken, newRefreshToken);

        // 3. Attempt to reuse the consumed old refresh token -> Must be rejected (Replay Protection)
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isForbidden());
    }

    private String obtainAccessToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
