package com.example.restlab.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ThirdPartyCredential Entity (Experiment 2.3.2)
 * Demonstrates military-grade encrypted storage of third-party OAuth credentials,
 * API client secrets, and sensitive tokens using AES-256 GCM.
 */
@Entity
@Table(name = "third_party_credentials")
public class ThirdPartyCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String serviceName; // GITHUB, GOOGLE, SLACK, TWITTER

    @Column(nullable = false, length = 150)
    private String clientId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedClientSecret;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String encryptedAccessToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ThirdPartyCredential() {
    }

    public ThirdPartyCredential(String serviceName, String clientId, String encryptedClientSecret,
                                String encryptedAccessToken, User user) {
        this.serviceName = serviceName;
        this.clientId = clientId;
        this.encryptedClientSecret = encryptedClientSecret;
        this.encryptedAccessToken = encryptedAccessToken;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getEncryptedClientSecret() {
        return encryptedClientSecret;
    }

    public void setEncryptedClientSecret(String encryptedClientSecret) {
        this.encryptedClientSecret = encryptedClientSecret;
    }

    public String getEncryptedAccessToken() {
        return encryptedAccessToken;
    }

    public void setEncryptedAccessToken(String encryptedAccessToken) {
        this.encryptedAccessToken = encryptedAccessToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
