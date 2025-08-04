package com.salah.taskmate.task.dto;

import com.salah.taskmate.shared.enums.TaskPriority;
import com.salah.taskmate.shared.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Size(max = 1000, message = "Content must be at most 1000 characters")
    private String content;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    private TaskStatus status = TaskStatus.PENDING;

    private TaskPriority priority = TaskPriority.MEDIUM;

    private UUID userId;
}
