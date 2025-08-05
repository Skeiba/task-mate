package com.salah.taskmate.task;

import com.salah.taskmate.category.Category;
import com.salah.taskmate.category.CategoryService;
import com.salah.taskmate.shared.enums.TaskPriority;
import com.salah.taskmate.shared.enums.TaskStatus;
import com.salah.taskmate.task.dto.TaskRequest;
import com.salah.taskmate.task.dto.TaskResponse;
import com.salah.taskmate.user.User;
import com.salah.taskmate.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements  TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final CategoryService categoryService;

    private static final String TASK_NOT_FOUND_MESSAGE = "Task with id %s not found";

    @Override
    public TaskResponse createTask(UUID userId, TaskRequest taskRequest) {

        if (taskRequest.getDueDate() != null && taskRequest.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }

        User user = userService.findById(userId);

        Task task = taskMapper.toEntity(taskRequest, user);

        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponse updateTask(UUID taskId, UUID userId, TaskRequest taskRequest) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));

        task.setTitle(taskRequest.getTitle());
        task.setContent(taskRequest.getContent());
        task.setDueDate(taskRequest.getDueDate());
        task.setPriority(taskRequest.getPriority());
        task.setStatus(taskRequest.getStatus());

        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public TaskResponse getTaskById(UUID taskId, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));
        return taskMapper.toResponse(task);
    }

    @Override
    public Page<TaskResponse> getAllTasks(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findAllByUserId(userId, pageable);
        return tasks.map(taskMapper::toResponse);
    }

    @Override
    public void deleteTask(UUID taskId, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse changeStatus(UUID taskId, UUID userId, TaskStatus taskStatus) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));
        task.setStatus(taskStatus);
        return  taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse changePriority(UUID taskId, UUID userId, TaskPriority taskPriority) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));
        task.setPriority(taskPriority);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse addCategories(UUID taskId, UUID userId, List<UUID> categoryIds) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));

        List<Category> categories = categoryService.getCategoriesByIdsAndUserId(categoryIds, userId);
        task.getCategories().addAll(categories);
        return taskMapper.toResponse(taskRepository.save(task));
    }
}
