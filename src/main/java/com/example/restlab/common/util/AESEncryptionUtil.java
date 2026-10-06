package com.example.restlab.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 GCM Cryptography Utility (Experiment 2.3.2)
 * Provides military-grade authenticated symmetric encryption and decryption
 * for securing third-party credentials, OAuth tokens, and sensitive database fields.
 */
@Component
public class AESEncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(AESEncryptionUtil.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final SecretKey secretKey;

    public AESEncryptionUtil(@Value("${app.encryption.aes-secret-key}") String masterSecret) {
        try {
            // Derive a deterministic 256-bit AES key from the master secret using SHA-256
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(masterSecret.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            log.info("AES-256 GCM Encryption engine initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize AES encryption key: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize AES key", e);
        }
    }

    /**
     * Encrypts plaintext string using AES-GCM with a randomized IV.
     * Output format: Base64(IV + CipherText)
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Error during AES encryption: {}", e.getMessage());
            throw new RuntimeException("AES encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts Base64(IV + CipherText) string using AES-GCM.
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, this.secretKey, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error during AES decryption: {}", e.getMessage());
            throw new RuntimeException("AES decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to mask sensitive credentials for safe logging or non-privileged display
     */
    public static String mask(String secret) {
        if (secret == null || secret.length() <= 4) {
            return "****";
        }
        return secret.substring(0, 2) + "******" + secret.substring(secret.length() - 2);
    }
}
