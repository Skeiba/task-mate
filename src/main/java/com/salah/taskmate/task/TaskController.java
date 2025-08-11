package com.salah.taskmate.task;

import com.salah.taskmate.security.CustomUserDetails;
import com.salah.taskmate.shared.annotation.StandardApiResponse;
import com.salah.taskmate.task.enums.TaskPriority;
import com.salah.taskmate.task.enums.TaskStatus;
import com.salah.taskmate.task.dto.TaskRequest;
import com.salah.taskmate.task.dto.TaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @StandardApiResponse(message = "Task created successfully")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TaskRequest taskRequest) {

        TaskResponse response = taskService.createTask(userDetails.getId(), taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @StandardApiResponse(message = "Tasks retrieved successfully")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TaskResponse> tasks = taskService.getAllTasks(userDetails.getId(), page, size);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @StandardApiResponse(message = "Task retrieved successfully")
    public ResponseEntity<TaskResponse> getTaskById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID taskId) {

        TaskResponse task = taskService.getTaskById(taskId, userDetails.getId());
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}")
    @StandardApiResponse(message = "Task updated successfully")
    public ResponseEntity<TaskResponse> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskRequest taskRequest) {

        TaskResponse response = taskService.updateTask(taskId, userDetails.getId(), taskRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    @StandardApiResponse(message = "Task deleted successfully")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID taskId) {

        taskService.deleteTask(taskId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/status")
    @StandardApiResponse(message = "Task status updated successfully")
    public ResponseEntity<TaskResponse> changeStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID taskId,
            @RequestParam TaskStatus status) {

        TaskResponse response = taskService.changeStatus(taskId, userDetails.getId(), status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/priority")
    @StandardApiResponse(message = "Task priority updated successfully")
    public ResponseEntity<TaskResponse> changePriority(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID taskId,
            @RequestParam TaskPriority priority) {

        TaskResponse response = taskService.changePriority(taskId, userDetails.getId(), priority);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/categories")
    @StandardApiResponse(message = "Categories added to task successfully")
    public ResponseEntity<TaskResponse> addCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID taskId,
            @RequestBody List<UUID> categoryIds) {

        TaskResponse response = taskService.addCategories(taskId, userDetails.getId(), categoryIds);
        return ResponseEntity.ok(response);
    }
}
