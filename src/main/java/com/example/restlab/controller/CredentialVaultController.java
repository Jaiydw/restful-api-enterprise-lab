package com.example.restlab.controller;

import com.example.restlab.common.api.ApiResponse;
import com.example.restlab.model.dto.credential.CredentialCreateDto;
import com.example.restlab.model.dto.credential.CredentialResponseDto;
import com.example.restlab.service.CredentialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Secure Credential Vault Controller (Experiment 2.3.2)
 * Demonstrates AES-256 GCM authenticated encryption of OAuth tokens, API secrets,
 * and third-party credentials.
 */
@RestController
@RequestMapping("/api/v1/vault/credentials")
public class CredentialVaultController {

    private final CredentialService credentialService;

    public CredentialVaultController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    /**
     * Store Third-Party Credential (Exp 2.3.2)
     * Encrypts plaintext secret with AES-256 before writing to database.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CredentialResponseDto>> storeCredential(
            @Valid @RequestBody CredentialCreateDto dto,
            Authentication authentication) {
        CredentialResponseDto stored = credentialService.storeCredential(dto, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(stored, "Sensitive credential successfully encrypted with AES-256 GCM and stored"));
    }

    /**
     * List Masked Credentials (Exp 2.3.2)
     * Displays safe masked secrets to prevent unauthorized exposure.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CredentialResponseDto>>> listCredentials(Authentication authentication) {
        List<CredentialResponseDto> list = credentialService.getCredentialsForUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(list, "Encrypted credentials retrieved with masked secret view"));
    }

    /**
     * Decrypt Specific Credential (Exp 2.3.2)
     * Restricted to privileged role (ADMIN or owner) to securely reveal secret.
     */
    @GetMapping("/{id}/decrypt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CredentialResponseDto>> decryptCredential(
            @PathVariable Long id,
            Authentication authentication) {
        CredentialResponseDto decrypted = credentialService.getDecryptedCredential(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(decrypted, "Credential decrypted using AES-256 GCM key successfully"));
    }
}
