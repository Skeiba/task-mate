package com.salah.taskmate.task.dto;

import com.salah.taskmate.shared.enums.TaskPriority;
import com.salah.taskmate.shared.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private UUID id;
    private String title;
    private String content;
    private LocalDateTime dueDate;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime createdAt;
    private String username;
}
