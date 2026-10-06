package com.example.restlab.repository;

import com.example.restlab.model.dto.post.PostSummaryDto;
import com.example.restlab.model.entity.Post;
import com.example.restlab.model.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Post Repository (Experiments 2.1.1, 2.2.1, 2.2.2)
 * Demonstrates:
 * 1. Pagination & Sorting via Spring Data Pageable
 * 2. JOIN FETCH to eliminate the N+1 query problem
 * 3. Native SQL query for database-level analytical aggregations
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Experiment 2.2.1: Paginated queries
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    // Experiment 2.2.2: JOIN FETCH query solving N+1 problem by fetching Author and Schedules in single query
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.schedules WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.schedules")
    List<Post> findAllWithDetails();

    // Experiment 2.2.2: Native SQL analytical query
    @Query(value = "SELECT p.status AS status, COUNT(*) AS totalCount FROM posts p GROUP BY p.status", nativeQuery = true)
    List<PostSummaryDto> getPostStatusSummaryNative();
}
