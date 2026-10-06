package com.example.restlab.controller;

import com.example.restlab.common.api.ApiResponse;
import com.example.restlab.model.dto.post.PostResponseDto;
import com.example.restlab.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performance Benchmarking & Optimization Controller (Experiment 2.2.2)
 * Provides live empirical benchmarking between:
 * 1. Unoptimized N+1 Queries vs Single JOIN FETCH Query
 * 2. Database Read (Cache Miss) vs In-Memory Caffeine (Cache Hit)
 */
@RestController
@RequestMapping("/api/v1/benchmark")
public class BenchmarkDemoController {

    private final PostService postService;

    public BenchmarkDemoController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Demonstrates the N+1 Query Problem (Exp 2.2.2)
     */
    @GetMapping("/n-plus-one")
    public ResponseEntity<ApiResponse<Map<String, Object>>> benchmarkNPlusOne() {
        Map<String, Object> demo = postService.demonstrateNPlusOneProblem();
        return ResponseEntity.ok(ApiResponse.success(demo, "N+1 query execution completed"));
    }

    /**
     * Demonstrates JOIN FETCH single-query solution (Exp 2.2.2)
     */
    @GetMapping("/join-fetch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> benchmarkJoinFetch() {
        long start = System.currentTimeMillis();
        List<PostResponseDto> posts = postService.getAllPostsWithDetailsOptimized();
        long duration = System.currentTimeMillis() - start;

        Map<String, Object> result = new HashMap<>();
        result.put("description", "Optimized Query via JOIN FETCH (Executes exactly 1 single SQL query)");
        result.put("totalPosts", posts.size());
        result.put("queriesExecutedEstimate", 1);
        result.put("executionTimeMs", duration);
        result.put("records", posts);

        return ResponseEntity.ok(ApiResponse.success(result, "JOIN FETCH execution completed"));
    }

    /**
     * Benchmarks Cache Performance (Cache Miss vs Cache Hits) (Exp 2.2.2)
     * Executes 5 back-to-back requests for post with given ID.
     * Iteration 1 incurs DB latency; Iterations 2-5 are served directly from Caffeine memory.
     */
    @GetMapping("/cache-test/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> benchmarkCache(@PathVariable Long id) {
        List<Map<String, Object>> iterations = new ArrayList<>();
        long totalCacheHitsTime = 0;

        for (int i = 1; i <= 5; i++) {
            long t0 = System.nanoTime();
            PostResponseDto dto = postService.getPostById(id);
            long elapsedNanos = System.nanoTime() - t0;
            double elapsedMs = elapsedNanos / 1_000_000.0;

            Map<String, Object> iter = new HashMap<>();
            iter.put("iteration", i);
            iter.put("type", i == 1 ? "Cache Miss / Warmup" : "Cache Hit (Caffeine Memory)");
            iter.put("elapsedMs", String.format("%.3f ms", elapsedMs));
            iterations.add(iter);

            if (i > 1) {
                totalCacheHitsTime += elapsedNanos;
            }
        }

        double avgCacheHitMs = (totalCacheHitsTime / 4.0) / 1_000_000.0;

        Map<String, Object> report = new HashMap<>();
        report.put("postId", id);
        report.put("iterations", iterations);
        report.put("averageCacheHitLatency", String.format("%.3f ms", avgCacheHitMs));
        report.put("conclusion", "Caffeine in-memory caching reduces latency by eliminating repeated database round-trips.");

        return ResponseEntity.ok(ApiResponse.success(report, "Cache benchmark completed successfully"));
    }
}
