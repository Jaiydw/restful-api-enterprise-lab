package com.example.restlab.controller;

import com.example.restlab.common.api.ApiResponse;
import com.example.restlab.common.api.PagedResponse;
import com.example.restlab.model.dto.post.PostCreateDto;
import com.example.restlab.model.dto.post.PostResponseDto;
import com.example.restlab.model.dto.post.PostSummaryDto;
import com.example.restlab.model.dto.post.PostUpdateDto;
import com.example.restlab.model.entity.PostStatus;
import com.example.restlab.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Post REST API Controller
 * Covers:
 * - Exp 2.1.1: REST CRUD, Bean Validation, Standardized Responses
 * - Exp 2.2.1: Pagination & Sorting with customizable sort properties and direction
 * - Exp 2.2.2: In-Memory Caching, JOIN FETCH, and Native SQL aggregation
 * - Exp 2.3.1: Role-Based Access Control (@PreAuthorize)
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Create Post (Exp 2.1.1)
     * Enforces Bean Validation; requires authenticated user.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @Valid @RequestBody PostCreateDto dto,
            Authentication authentication) {
        PostResponseDto created = postService.createPost(dto, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Post created successfully"));
    }

    /**
     * Get Post by ID (Exp 2.1.1 & Exp 2.2.2 Caching)
     * Cached via Caffeine in-memory cache.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPostById(@PathVariable Long id) {
        PostResponseDto post = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(post, "Post retrieved successfully"));
    }

    /**
     * Update Post (Exp 2.1.1 & Exp 2.2.2 CachePut)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDto>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateDto dto,
            Authentication authentication) {
        PostResponseDto updated = postService.updatePost(id, dto, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Post updated successfully"));
    }

    /**
     * Delete Post (Exp 2.1.1 & Exp 2.3.1 RBAC & Exp 2.2.2 CacheEvict)
     * Authors can delete their own posts; ADMINs can delete any post.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> deletePost(
            @PathVariable Long id,
            Authentication authentication) {
        postService.deletePost(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully and evicted from cache"));
    }

    /**
     * Paginated & Sortable Posts API (Exp 2.2.1)
     * Example: GET /api/v1/posts?page=0&size=5&sort=createdAt,desc&sort=title,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostResponseDto>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        List<Sort.Order> orders = new ArrayList<>();
        if (sort[0].contains(",")) {
            // Multiple sort criteria: e.g. ["createdAt,desc", "title,asc"]
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                Sort.Direction direction = _sort.length > 1 && _sort[1].equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
                orders.add(new Sort.Order(direction, _sort[0]));
            }
        } else {
            // Single sort criteria: e.g. sort=createdAt&sort=desc
            Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            orders.add(new Sort.Order(direction, sort[0]));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        PagedResponse<PostResponseDto> pagedResponse = postService.getAllPostsPaged(pageable);
        return ResponseEntity.ok(ApiResponse.success(pagedResponse, "Paginated posts retrieved successfully"));
    }

    /**
     * Filter by Status with Pagination (Exp 2.2.1)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponseDto>>> getPostsByStatus(
            @PathVariable PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<PostResponseDto> pagedResponse = postService.getPostsByStatusPaged(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(pagedResponse, "Filtered posts retrieved successfully"));
    }

    /**
     * Optimized Read via JOIN FETCH (Exp 2.2.2)
     * Resolves N+1 query problem by loading Post, Author, and Schedules in a single query.
     */
    @GetMapping("/optimized-fetch")
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPostsWithDetailsOptimized() {
        List<PostResponseDto> list = postService.getAllPostsWithDetailsOptimized();
        return ResponseEntity.ok(ApiResponse.success(list, "Optimized JOIN FETCH posts retrieved successfully without N+1 overhead"));
    }

    /**
     * Native SQL Query Projection (Exp 2.2.2)
     * Aggregates post count grouped by status directly in the database engine.
     */
    @GetMapping("/native-summary")
    public ResponseEntity<ApiResponse<List<PostSummaryDto>>> getPostStatusSummary() {
        List<PostSummaryDto> summary = postService.getPostStatusSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Native SQL aggregation executed successfully"));
    }
}
