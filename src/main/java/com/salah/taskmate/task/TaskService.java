package com.salah.taskmate.task;

import com.salah.taskmate.task.enums.TaskPriority;
import com.salah.taskmate.task.enums.TaskStatus;
import com.salah.taskmate.task.dto.TaskRequest;
import com.salah.taskmate.task.dto.TaskResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(UUID userId, TaskRequest taskRequest);
    TaskResponse updateTask(UUID taskId, UUID userId, TaskRequest taskRequest);
    TaskResponse getTaskById(UUID taskId,  UUID userId);
    Page<TaskResponse> getAllTasks(UUID userId, int  page, int size);
    void deleteTask(UUID taskId, UUID userId);
    TaskResponse changeStatus(UUID taskId, UUID userId, TaskStatus taskStatus);
    TaskResponse changePriority(UUID taskId, UUID userId, TaskPriority taskPriority);
    TaskResponse addCategories(UUID taskId, UUID userId, List<UUID> categoryIds);
    TaskResponse toggleFavorite(UUID taskId, UUID id);
}
