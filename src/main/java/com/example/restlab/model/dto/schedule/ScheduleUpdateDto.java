package com.example.restlab.model.dto.schedule;

import java.time.LocalDateTime;

public class ScheduleUpdateDto {

    private LocalDateTime scheduledTime;
    private String platform;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    public ScheduleUpdateDto() {
    }

    public ScheduleUpdateDto(LocalDateTime scheduledTime, String platform, String status) {
        this.scheduledTime = scheduledTime;
        this.platform = platform;
        this.status = status;
    }

    // Getters and Setters
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
}
