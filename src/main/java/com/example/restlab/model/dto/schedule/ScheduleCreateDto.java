package com.example.restlab.model.dto.schedule;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Schedule Creation Payload (Experiment 2.1.1)
 * Validates post association and future scheduling time.
 */
public class ScheduleCreateDto {

    @NotNull(message = "Post ID is mandatory")
    private Long postId;

    @NotNull(message = "Scheduled time is mandatory")
    @Future(message = "Scheduled time must be set in the future")
    private LocalDateTime scheduledTime;

    @NotBlank(message = "Publishing platform is mandatory (e.g. TWITTER, LINKEDIN, MEDIUM)")
    private String platform;

    public ScheduleCreateDto() {
    }

    public ScheduleCreateDto(Long postId, LocalDateTime scheduledTime, String platform) {
        this.postId = postId;
        this.scheduledTime = scheduledTime;
        this.platform = platform;
    }

    // Getters and Setters
    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
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
}
