package com.example.restlab.service;

import com.example.restlab.common.exception.ForbiddenException;
import com.example.restlab.common.exception.ResourceNotFoundException;
import com.example.restlab.common.util.AESEncryptionUtil;
import com.example.restlab.model.dto.credential.CredentialCreateDto;
import com.example.restlab.model.dto.credential.CredentialResponseDto;
import com.example.restlab.model.entity.Role;
import com.example.restlab.model.entity.ThirdPartyCredential;
import com.example.restlab.model.entity.User;
import com.example.restlab.repository.ThirdPartyCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Secure Credential Vault Service (Experiment 2.3.2)
 * Demonstrates AES-256 GCM encryption of OAuth tokens, API secrets,
 * and sensitive infrastructure credentials prior to database persistence.
 */
@Service
public class CredentialService {

    private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

    private final ThirdPartyCredentialRepository credentialRepository;
    private final AESEncryptionUtil aesEncryptionUtil;
    private final AuthService authService;

    public CredentialService(ThirdPartyCredentialRepository credentialRepository,
                             AESEncryptionUtil aesEncryptionUtil,
                             AuthService authService) {
        this.credentialRepository = credentialRepository;
        this.aesEncryptionUtil = aesEncryptionUtil;
        this.authService = authService;
    }

    @Transactional
    public CredentialResponseDto storeCredential(CredentialCreateDto dto, String username) {
        User user = authService.getUserByUsername(username);

        log.info("Encrypting sensitive credentials for service: '{}' using AES-256 GCM...", dto.getServiceName());

        // Encrypt secrets using AES-256 GCM
        String encryptedClientSecret = aesEncryptionUtil.encrypt(dto.getClientSecret());
        String encryptedAccessToken = dto.getAccessToken() != null
                ? aesEncryptionUtil.encrypt(dto.getAccessToken())
                : null;

        ThirdPartyCredential credential = new ThirdPartyCredential(
                dto.getServiceName().toUpperCase(),
                dto.getClientId(),
                encryptedClientSecret,
                encryptedAccessToken,
                user
        );

        ThirdPartyCredential saved = credentialRepository.save(credential);
        log.info("Securely saved AES-encrypted credential [ID: {}] in database.", saved.getId());

        return CredentialResponseDto.fromEntity(saved, dto.getClientSecret());
    }

    @Transactional(readOnly = true)
    public List<CredentialResponseDto> getCredentialsForUser(String username) {
        User user = authService.getUserByUsername(username);
        List<ThirdPartyCredential> list;

        // Admin can view all, normal user sees only their own
        if (user.getRole() == Role.ROLE_ADMIN) {
            list = credentialRepository.findAll();
        } else {
            list = credentialRepository.findByUserId(user.getId());
        }

        return list.stream()
                .map(c -> {
                    String decryptedSecret = aesEncryptionUtil.decrypt(c.getEncryptedClientSecret());
                    return CredentialResponseDto.fromEntity(c, decryptedSecret);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CredentialResponseDto getDecryptedCredential(Long id, String username) {
        ThirdPartyCredential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));

        User user = authService.getUserByUsername(username);
        if (!credential.getUser().getId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("Access denied: You cannot inspect another user's encrypted credentials");
        }

        String decryptedSecret = aesEncryptionUtil.decrypt(credential.getEncryptedClientSecret());
        log.info("Decrypted AES credentials for service: '{}' for user: '{}'",
                credential.getServiceName(), username);

        return CredentialResponseDto.fromEntity(credential, decryptedSecret);
    }
}
