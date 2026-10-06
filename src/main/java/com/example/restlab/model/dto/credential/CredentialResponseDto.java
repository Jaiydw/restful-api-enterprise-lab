package com.example.restlab.model.dto.credential;

import com.example.restlab.common.util.AESEncryptionUtil;
import com.example.restlab.model.entity.ThirdPartyCredential;

import java.time.LocalDateTime;

public class CredentialResponseDto {

    private Long id;
    private String serviceName;
    private String clientId;
    private String maskedClientSecret;
    private String maskedAccessToken;
    private String decryptedClientSecret; // Populated only when explicitly requested by authorized ADMIN
    private String rawEncryptedCipherInDb; // To demonstrate encrypted storage in H2 database
    private LocalDateTime updatedAt;

    public CredentialResponseDto() {
    }

    public CredentialResponseDto(Long id, String serviceName, String clientId, String maskedClientSecret,
                                 String maskedAccessToken, String decryptedClientSecret,
                                 String rawEncryptedCipherInDb, LocalDateTime updatedAt) {
        this.id = id;
        this.serviceName = serviceName;
        this.clientId = clientId;
        this.maskedClientSecret = maskedClientSecret;
        this.maskedAccessToken = maskedAccessToken;
        this.decryptedClientSecret = decryptedClientSecret;
        this.rawEncryptedCipherInDb = rawEncryptedCipherInDb;
        this.updatedAt = updatedAt;
    }

    public static CredentialResponseDto fromEntity(ThirdPartyCredential credential, String decryptedSecret) {
        if (credential == null) {
            return null;
        }

        return new CredentialResponseDto(
                credential.getId(),
                credential.getServiceName(),
                credential.getClientId(),
                AESEncryptionUtil.mask(decryptedSecret),
                credential.getEncryptedAccessToken() != null ? "******(AES Encrypted)" : null,
                decryptedSecret,
                credential.getEncryptedClientSecret(), // Demonstrates real AES-256 GCM ciphertext
                credential.getUpdatedAt()
        );
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

    public String getMaskedClientSecret() {
        return maskedClientSecret;
    }

    public void setMaskedClientSecret(String maskedClientSecret) {
        this.maskedClientSecret = maskedClientSecret;
    }

    public String getMaskedAccessToken() {
        return maskedAccessToken;
    }

    public void setMaskedAccessToken(String maskedAccessToken) {
        this.maskedAccessToken = maskedAccessToken;
    }

    public String getDecryptedClientSecret() {
        return decryptedClientSecret;
    }

    public void setDecryptedClientSecret(String decryptedClientSecret) {
        this.decryptedClientSecret = decryptedClientSecret;
    }

    public String getRawEncryptedCipherInDb() {
        return rawEncryptedCipherInDb;
    }

    public void setRawEncryptedCipherInDb(String rawEncryptedCipherInDb) {
        this.rawEncryptedCipherInDb = rawEncryptedCipherInDb;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
