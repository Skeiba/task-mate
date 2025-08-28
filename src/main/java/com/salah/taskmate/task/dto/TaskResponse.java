package com.salah.taskmate.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.salah.taskmate.category.dto.CategoryResponse;
import com.salah.taskmate.task.enums.TaskPriority;
import com.salah.taskmate.task.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
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
    @JsonProperty("isFavorite")
    private boolean isFavorite;
    private List<CategoryResponse> categories;
}
