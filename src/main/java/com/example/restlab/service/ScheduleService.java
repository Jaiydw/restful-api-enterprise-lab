package com.example.restlab.service;

import com.example.restlab.common.exception.ResourceNotFoundException;
import com.example.restlab.model.dto.schedule.ScheduleCreateDto;
import com.example.restlab.model.dto.schedule.ScheduleResponseDto;
import com.example.restlab.model.dto.schedule.ScheduleUpdateDto;
import com.example.restlab.model.entity.Post;
import com.example.restlab.model.entity.PostStatus;
import com.example.restlab.model.entity.Schedule;
import com.example.restlab.repository.PostRepository;
import com.example.restlab.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduling Service (Experiment 2.1.1)
 * Manages post scheduling lifecycle and platform targets.
 */
@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final PostRepository postRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, PostRepository postRepository) {
        this.scheduleRepository = scheduleRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleCreateDto dto) {
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", dto.getPostId()));

        Schedule schedule = new Schedule(
                dto.getScheduledTime(),
                dto.getPlatform().toUpperCase(),
                "PENDING",
                post
        );

        // Update post status to SCHEDULED
        post.setStatus(PostStatus.SCHEDULED);
        postRepository.save(post);

        Schedule saved = scheduleRepository.save(schedule);
        log.info("Created schedule [ID: {}] for post [ID: {}] on platform: {}",
                saved.getId(), post.getId(), saved.getPlatform());
        return ScheduleResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ScheduleResponseDto getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));
        return ScheduleResponseDto.fromEntity(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedulesByPostId(Long postId) {
        return scheduleRepository.findByPostId(postId).stream()
                .map(ScheduleResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public ScheduleResponseDto updateSchedule(Long id, ScheduleUpdateDto dto) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));

        if (dto.getScheduledTime() != null) {
            schedule.setScheduledTime(dto.getScheduledTime());
        }
        if (dto.getPlatform() != null && !dto.getPlatform().isBlank()) {
            schedule.setPlatform(dto.getPlatform().toUpperCase());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            schedule.setStatus(dto.getStatus().toUpperCase());
        }

        Schedule updated = scheduleRepository.save(schedule);
        log.info("Updated schedule [ID: {}] status to: {}", id, updated.getStatus());
        return ScheduleResponseDto.fromEntity(updated);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));
        scheduleRepository.delete(schedule);
        log.info("Deleted schedule [ID: {}]", id);
    }
}
