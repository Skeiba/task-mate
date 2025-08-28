package com.salah.taskmate.task;

import com.salah.taskmate.category.Category;
import com.salah.taskmate.category.CategoryMapper;
import com.salah.taskmate.category.dto.CategoryResponse;
import com.salah.taskmate.task.dto.TaskRequest;
import com.salah.taskmate.task.dto.TaskResponse;
import com.salah.taskmate.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final CategoryMapper categoryMapper;

    public Task toEntity(TaskRequest taskRequest, User user, List<Category> categories) {
        Set<Category> categorySet = categories != null ? new HashSet<>(categories) : new HashSet<>();
        return Task.builder()
                .title(taskRequest.getTitle())
                .content(taskRequest.getContent())
                .dueDate(taskRequest.getDueDate())
                .status(taskRequest.getStatus())
                .priority(taskRequest.getPriority())
                .isFavorite(taskRequest.isFavorite())
                .user(user)
                .categories(categorySet)
                .build();
    }

    public TaskResponse toResponse(Task task) {
        List<CategoryResponse> categoryResponses = task.getCategories().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .content(task.getContent())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .username(task.getUser().getUsername())
                .isFavorite(task.isFavorite())
                .categories(categoryResponses)
                .build();
    }
}
