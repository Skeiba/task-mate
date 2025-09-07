package com.salah.taskmate.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findByIdAndUserId(UUID taskId, UUID userId);

    Page<Task> findAllByUserId(UUID userId, Pageable pageable);

    List<Task> findByUserIdAndDueDateBetween(UUID userId, LocalDateTime dueDateAfter, LocalDateTime dueDateBefore);

    UUID findByTitleAndUserId(String title, UUID userId);
}
