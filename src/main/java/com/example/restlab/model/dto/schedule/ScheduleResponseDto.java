package com.example.restlab.model.dto.schedule;

import com.example.restlab.model.entity.Schedule;

import java.time.LocalDateTime;

public class ScheduleResponseDto {

    private Long id;
    private Long postId;
    private String postTitle;
    private LocalDateTime scheduledTime;
    private String platform;
    private String status;
    private LocalDateTime createdAt;

    public ScheduleResponseDto() {
    }

    public ScheduleResponseDto(Long id, Long postId, String postTitle, LocalDateTime scheduledTime,
                               String platform, String status, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.postTitle = postTitle;
        this.scheduledTime = scheduledTime;
        this.platform = platform;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ScheduleResponseDto fromEntity(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        Long postId = schedule.getPost() != null ? schedule.getPost().getId() : null;
        String postTitle = schedule.getPost() != null ? schedule.getPost().getTitle() : null;

        return new ScheduleResponseDto(
                schedule.getId(),
                postId,
                postTitle,
                schedule.getScheduledTime(),
                schedule.getPlatform(),
                schedule.getStatus(),
                schedule.getCreatedAt()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
