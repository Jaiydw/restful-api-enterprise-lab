package com.example.restlab.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Schedule Entity (Experiments 2.1.1 & 2.2.2)
 * Manages automated publication timings and target social platforms.
 */
@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(nullable = false, length = 50)
    private String platform; // TWITTER, LINKEDIN, MEDIUM, DEVTO

    @Column(nullable = false, length = 20)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Schedule() {
    }

    public Schedule(LocalDateTime scheduledTime, String platform, String status, Post post) {
        this.scheduledTime = scheduledTime;
        this.platform = platform;
        this.status = status;
        this.post = post;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
