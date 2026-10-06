package com.example.restlab.service;

import com.example.restlab.common.api.PagedResponse;
import com.example.restlab.common.exception.ForbiddenException;
import com.example.restlab.common.exception.ResourceNotFoundException;
import com.example.restlab.model.dto.post.PostCreateDto;
import com.example.restlab.model.dto.post.PostResponseDto;
import com.example.restlab.model.dto.post.PostSummaryDto;
import com.example.restlab.model.dto.post.PostUpdateDto;
import com.example.restlab.model.entity.Post;
import com.example.restlab.model.entity.PostStatus;
import com.example.restlab.model.entity.Role;
import com.example.restlab.model.entity.User;
import com.example.restlab.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Post Business Service
 * Integrates:
 * - Experiment 2.1.1: Layered architecture, CRUD
 * - Experiment 2.2.1: Pagination & Sorting via Spring Data Pageable
 * - Experiment 2.2.2: In-Memory Caching (@Cacheable, @CachePut, @CacheEvict),
 *                     JOIN FETCH query optimization vs N+1 demonstration,
 *                     and Native SQL aggregation.
 */
@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final AuthService authService;

    public PostService(PostRepository postRepository, AuthService authService) {
        this.postRepository = postRepository;
        this.authService = authService;
    }

    @Transactional
    public PostResponseDto createPost(PostCreateDto dto, String username) {
        User author = authService.getUserByUsername(username);

        Post post = new Post(
                dto.getTitle(),
                dto.getContent(),
                dto.getStatus() != null ? dto.getStatus() : PostStatus.DRAFT,
                dto.getTags(),
                author
        );

        Post savedPost = postRepository.save(post);
        log.info("Post created [ID: {}] by author '{}'", savedPost.getId(), username);
        return PostResponseDto.fromEntity(savedPost);
    }

    /**
     * Cacheable Read Operation (Experiment 2.2.2)
     * When requested multiple times, subsequent requests are fulfilled from Caffeine in-memory cache,
     * bypassing database execution entirely.
     */
    @Cacheable(value = "posts", key = "#id")
    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id) {
        log.info("[CACHE MISS] Fetching Post [ID: {}] from Database...", id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return PostResponseDto.fromEntity(post);
    }

    /**
     * CachePut Update Operation (Experiment 2.2.2)
     * Updates database record and simultaneously refreshes the in-memory cache value.
     */
    @CachePut(value = "posts", key = "#id")
    @Transactional
    public PostResponseDto updatePost(Long id, PostUpdateDto dto, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User currentUser = authService.getUserByUsername(username);
        // Only author or ADMIN can update
        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to modify this post");
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            post.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            post.setContent(dto.getContent());
        }
        if (dto.getStatus() != null) {
            post.setStatus(dto.getStatus());
        }
        if (dto.getTags() != null) {
            post.setTags(dto.getTags());
        }

        Post updated = postRepository.save(post);
        log.info("[CACHE UPDATE] Post [ID: {}] updated in database and in cache", id);
        return PostResponseDto.fromEntity(updated);
    }

    /**
     * CacheEvict Delete Operation (Experiment 2.2.2)
     * Removes entity from database and evicts stale cache entry.
     */
    @CacheEvict(value = "posts", key = "#id")
    @Transactional
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User currentUser = authService.getUserByUsername(username);
        // Enforce RBAC: Author or ADMIN can delete
        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You do not have permission to delete this post");
        }

        postRepository.delete(post);
        log.info("[CACHE EVICT] Post [ID: {}] removed from DB and evicted from cache", id);
    }

    /**
     * Paginated & Sorted Retrieval (Experiment 2.2.1)
     */
    @Transactional(readOnly = true)
    public PagedResponse<PostResponseDto> getAllPostsPaged(Pageable pageable) {
        log.info("Fetching paginated posts: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Post> page = postRepository.findAll(pageable);
        List<PostResponseDto> dtos = page.getContent().stream()
                .map(PostResponseDto::fromEntity)
                .toList();
        return PagedResponse.from(page, dtos);
    }

    /**
     * Paginated & Filtered by Status (Experiment 2.2.1)
     */
    @Transactional(readOnly = true)
    public PagedResponse<PostResponseDto> getPostsByStatusPaged(PostStatus status, Pageable pageable) {
        Page<Post> page = postRepository.findByStatus(status, pageable);
        List<PostResponseDto> dtos = page.getContent().stream()
                .map(PostResponseDto::fromEntity)
                .toList();
        return PagedResponse.from(page, dtos);
    }

    /**
     * JOIN FETCH Query Optimization (Experiment 2.2.2)
     * Solves N+1 problem: Fetches Posts, Authors, and Schedules in a SINGLE optimized SQL query.
     */
    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPostsWithDetailsOptimized() {
        log.info("[JOIN FETCH OPTIMIZATION] Executing single JOIN FETCH query for posts, authors, and schedules...");
        List<Post> posts = postRepository.findAllWithDetails();
        return posts.stream().map(PostResponseDto::fromEntity).toList();
    }

    /**
     * Demonstration of N+1 Query Problem (Experiment 2.2.2)
     * Retrieves all posts with regular findAll(), then traverses lazy relations (Author, Schedules),
     * causing 1 initial query + N queries for authors + N queries for schedules.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> demonstrateNPlusOneProblem() {
        log.warn("[N+1 QUERY DEMO] Beginning unoptimized query execution...");
        long start = System.currentTimeMillis();

        // 1 query to get N posts
        List<Post> posts = postRepository.findAll();

        List<Map<String, Object>> records = new ArrayList<>();
        // For each post, accessing lazy proxies generates additional queries
        for (Post p : posts) {
            Map<String, Object> item = new HashMap<>();
            item.put("postId", p.getId());
            item.put("title", p.getTitle());
            // Triggers query for Author if not loaded
            item.put("author", p.getAuthor().getUsername());
            // Triggers query for Schedules collection if not loaded
            item.put("schedulesCount", p.getSchedules().size());
            records.add(item);
        }

        long elapsedMs = System.currentTimeMillis() - start;

        Map<String, Object> result = new HashMap<>();
        result.put("description", "Unoptimized Lazy Loading (Causes 1 + 2N SQL queries)");
        result.put("totalPosts", posts.size());
        result.put("queriesExecutedEstimate", 1 + (posts.size() * 2));
        result.put("executionTimeMs", elapsedMs);
        result.put("records", records);

        return result;
    }

    /**
     * Native SQL Analytical Aggregation (Experiment 2.2.2)
     */
    @Transactional(readOnly = true)
    public List<PostSummaryDto> getPostStatusSummary() {
        log.info("[NATIVE SQL] Executing native SQL aggregation query on posts table...");
        return postRepository.getPostStatusSummaryNative();
    }
}
