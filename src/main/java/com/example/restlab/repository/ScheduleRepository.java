package com.example.restlab.repository;

import com.example.restlab.model.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByPostId(Long postId);

    List<Schedule> findByStatus(String status);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.post WHERE s.status = :status")
    List<Schedule> findByStatusWithPost(@Param("status") String status);
}
