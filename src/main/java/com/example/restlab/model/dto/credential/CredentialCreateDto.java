package com.example.restlab.model.dto.credential;

import jakarta.validation.constraints.NotBlank;

/**
 * Third-Party Credential Request (Experiment 2.3.2)
 * Carries plaintext secrets to be encrypted via AES-256 GCM before database persistence.
 */
public class CredentialCreateDto {

    @NotBlank(message = "Service name is required (e.g. GITHUB, SLACK, STRIPE)")
    private String serviceName;

    @NotBlank(message = "Client ID / Key is required")
    private String clientId;

    @NotBlank(message = "Client Secret is required")
    private String clientSecret;

    private String accessToken;

    public CredentialCreateDto() {
    }

    public CredentialCreateDto(String serviceName, String clientId, String clientSecret, String accessToken) {
        this.serviceName = serviceName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
    }

    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
