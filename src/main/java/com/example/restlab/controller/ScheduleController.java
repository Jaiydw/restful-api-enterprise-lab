package com.example.restlab.controller;

import com.example.restlab.common.api.ApiResponse;
import com.example.restlab.model.dto.schedule.ScheduleCreateDto;
import com.example.restlab.model.dto.schedule.ScheduleResponseDto;
import com.example.restlab.model.dto.schedule.ScheduleUpdateDto;
import com.example.restlab.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Schedule Controller (Experiment 2.1.1)
 * Provides CRUD operations for scheduling social media and blog publications.
 */
@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> createSchedule(@Valid @RequestBody ScheduleCreateDto dto) {
        ScheduleResponseDto created = scheduleService.createSchedule(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Publication schedule created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> getScheduleById(@PathVariable Long id) {
        ScheduleResponseDto schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(ApiResponse.success(schedule, "Schedule retrieved successfully"));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<ScheduleResponseDto>>> getSchedulesByPostId(@PathVariable Long postId) {
        List<ScheduleResponseDto> list = scheduleService.getSchedulesByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(list, "Post schedules retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleUpdateDto dto) {
        ScheduleResponseDto updated = scheduleService.updateSchedule(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Schedule updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule deleted successfully"));
    }
}
