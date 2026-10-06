package com.example.restlab.config;

import com.example.restlab.common.util.AESEncryptionUtil;
import com.example.restlab.model.entity.*;
import com.example.restlab.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Database Seed Initializer
 * Automatically seeds test users (admin, user1, user2), sample posts, schedules,
 * and AES-encrypted third-party credentials for immediate testing out of the box.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ScheduleRepository scheduleRepository;
    private final ThirdPartyCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AESEncryptionUtil aesEncryptionUtil;

    public DataInitializer(UserRepository userRepository,
                           PostRepository postRepository,
                           ScheduleRepository scheduleRepository,
                           ThirdPartyCredentialRepository credentialRepository,
                           PasswordEncoder passwordEncoder,
                           AESEncryptionUtil aesEncryptionUtil) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.scheduleRepository = scheduleRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.aesEncryptionUtil = aesEncryptionUtil;
    }

    @Override
    public void run(String... args) {
        log.info("Seeding initial data for Enterprise REST API Experiments...");

        // 1. Create Default Users (ROLE_ADMIN and ROLE_USER)
        User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@example.com", Role.ROLE_ADMIN);
        User user1 = new User("john_doe", passwordEncoder.encode("user123"), "john@example.com", Role.ROLE_USER);
        User user2 = new User("jane_smith", passwordEncoder.encode("user123"), "jane@example.com", Role.ROLE_USER);

        userRepository.save(admin);
        userRepository.save(user1);
        userRepository.save(user2);

        // 2. Create Sample Posts across statuses
        Post post1 = new Post(
                "Getting Started with Spring Boot 3.4 REST APIs",
                "Learn how to design and build production-ready RESTful microservices with Spring Boot.",
                PostStatus.PUBLISHED,
                "springboot,java,rest,api",
                admin
        );
        Post post2 = new Post(
                "Understanding the N+1 Query Problem in JPA Hibernate",
                "Deep dive into ORM query latency, lazy loading pitfalls, and how JOIN FETCH solves N+1.",
                PostStatus.PUBLISHED,
                "jpa,hibernate,performance,sql",
                user1
        );
        Post post3 = new Post(
                "Securing Microservices with Stateless JWT and RBAC",
                "Complete guide to stateless authentication, method security with @PreAuthorize, and token rotation.",
                PostStatus.SCHEDULED,
                "security,jwt,rbac,springboot",
                user1
        );
        Post post4 = new Post(
                "Military-Grade Encryption with AES-256 GCM in Java",
                "How to safely store OAuth client secrets and credentials using authenticated symmetric encryption.",
                PostStatus.PUBLISHED,
                "cryptography,aes,security,privacy",
                admin
        );
        Post post5 = new Post(
                "High-Throughput In-Memory Caching using Caffeine",
                "Benchmarking cache hits vs database queries for sub-millisecond API response times.",
                PostStatus.DRAFT,
                "caching,performance,caffeine",
                user2
        );

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);
        postRepository.save(post4);
        postRepository.save(post5);

        // 3. Create Sample Schedules for posts
        Schedule schedule1 = new Schedule(
                LocalDateTime.now().plusDays(2),
                "TWITTER",
                "PENDING",
                post3
        );
        Schedule schedule2 = new Schedule(
                LocalDateTime.now().plusDays(3),
                "LINKEDIN",
                "PENDING",
                post3
        );
        Schedule schedule3 = new Schedule(
                LocalDateTime.now().plusDays(5),
                "MEDIUM",
                "PENDING",
                post1
        );

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);

        // 4. Create Sample AES-256 Encrypted Third-Party Credentials (Exp 2.3.2)
        String secretGithub = "mock_github_pat_secret_98472983471092834701";
        String secretSlack = "mock_slack_bot_token_secret_key_99";

        ThirdPartyCredential cred1 = new ThirdPartyCredential(
                "GITHUB_OAUTH",
                "github_client_id_001",
                aesEncryptionUtil.encrypt(secretGithub),
                aesEncryptionUtil.encrypt("oauth_token_gh_sample"),
                admin
        );

        ThirdPartyCredential cred2 = new ThirdPartyCredential(
                "SLACK_BOT_API",
                "slack_bot_id_002",
                aesEncryptionUtil.encrypt(secretSlack),
                aesEncryptionUtil.encrypt("slack_token_sample"),
                user1
        );

        credentialRepository.save(cred1);
        credentialRepository.save(cred2);

        log.info("Database initialized successfully!");
        log.info("Default Accounts Available:");
        log.info("  1. Admin: username='admin', password='admin123', role='ROLE_ADMIN'");
        log.info("  2. User:  username='john_doe', password='user123', role='ROLE_USER'");
        log.info("  3. User:  username='jane_smith', password='user123', role='ROLE_USER'");
    }
}
