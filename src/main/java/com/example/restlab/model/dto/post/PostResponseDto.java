package com.example.restlab.model.dto.post;

import com.example.restlab.model.entity.Post;
import com.example.restlab.model.entity.PostStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Post Response Representation (Serializable for Cache Storage - Exp 2.2.2)
 */
public class PostResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    private PostStatus status;
    private String tags;
    private Long authorId;
    private String authorUsername;
    private int schedulesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDto() {
    }

    public PostResponseDto(Long id, String title, String content, PostStatus status, String tags,
                           Long authorId, String authorUsername, int schedulesCount,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.status = status;
        this.tags = tags;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.schedulesCount = schedulesCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PostResponseDto fromEntity(Post post) {
        if (post == null) {
            return null;
        }

        Long authorId = post.getAuthor() != null ? post.getAuthor().getId() : null;
        String authorUsername = post.getAuthor() != null ? post.getAuthor().getUsername() : null;
        int schedulesCount = post.getSchedules() != null ? post.getSchedules().size() : 0;

        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getStatus(),
                post.getTags(),
                authorId,
                authorUsername,
                schedulesCount,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public int getSchedulesCount() {
        return schedulesCount;
    }

    public void setSchedulesCount(int schedulesCount) {
        this.schedulesCount = schedulesCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
