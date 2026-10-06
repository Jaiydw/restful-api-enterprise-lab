package com.example.restlab.model.dto.post;

import com.example.restlab.model.entity.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Post Creation Payload (Experiment 2.1.1)
 * Enforces Bean Validation constraints before reaching the Service layer.
 */
public class PostCreateDto {

    @NotBlank(message = "Title is mandatory and cannot be blank")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @NotBlank(message = "Content is mandatory and cannot be blank")
    @Size(min = 10, message = "Content must be at least 10 characters long")
    private String content;

    private PostStatus status; // Defaults to DRAFT if omitted

    @Size(max = 200, message = "Tags cannot exceed 200 characters")
    private String tags;

    public PostCreateDto() {
    }

    public PostCreateDto(String title, String content, PostStatus status, String tags) {
        this.title = title;
        this.content = content;
        this.status = status;
        this.tags = tags;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
