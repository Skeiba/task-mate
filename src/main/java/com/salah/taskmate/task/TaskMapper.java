package com.salah.taskmate.task;

import com.salah.taskmate.task.dto.TaskRequest;
import com.salah.taskmate.task.dto.TaskResponse;
import com.salah.taskmate.user.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public Task toEntity(TaskRequest taskRequest, User user) {
        return Task.builder()
                .title(taskRequest.getTitle())
                .content(taskRequest.getContent())
                .dueDate(taskRequest.getDueDate())
                .status(taskRequest.getStatus())
                .priority(taskRequest.getPriority())
                .user(user)
                .build();
    }

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .content(task.getContent())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .username(task.getUser().getUsername())
                .build();
    }
}
