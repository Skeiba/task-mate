package com.salah.taskmate.task;

import com.salah.taskmate.category.Category;
import com.salah.taskmate.category.CategoryService;
import com.salah.taskmate.shared.exception.AiServiceException;
import com.salah.taskmate.task.enums.TaskPriority;
import com.salah.taskmate.task.enums.TaskStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

        taskRequest.setTitle(normalizeText(taskRequest.getTitle()));
        taskRequest.setContent(normalizeText(taskRequest.getContent()));

        if (taskRequest.getDueDate() != null && taskRequest.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }

        User user = userService.findUserById(userId);

        List<Category> categories = taskRequest.getCategoryIds() != null
                ? categoryService.getCategoriesByIdsAndUserId(taskRequest.getCategoryIds(), userId)
                : List.of();

        Task task = taskMapper.toEntity(taskRequest, user, categories);

        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }


    @Override
    public TaskResponse updateTask(UUID taskId, UUID userId, TaskRequest taskRequest) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));

        if (taskRequest.getDueDate() != null && taskRequest.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }

        task.setTitle(normalizeText(taskRequest.getTitle()));
        task.setContent(normalizeText(taskRequest.getContent()));
        task.setDueDate(taskRequest.getDueDate());
        task.setPriority(taskRequest.getPriority());
        task.setStatus(taskRequest.getStatus());
        task.setFavorite(taskRequest.isFavorite());

        if (taskRequest.getCategoryIds() != null) {
            List<Category> categories = categoryService.getCategoriesByIdsAndUserId(taskRequest.getCategoryIds(), userId);
            task.getCategories().clear();
            task.getCategories().addAll(categories);
        }

        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public TaskResponse getTaskById(UUID taskId, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));
        updateTaskStatusIfMissed(task);
        return taskMapper.toResponse(task);
    }

    @Override
    public Page<TaskResponse> getAllTasks(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findAllByUserId(userId, pageable);
        tasks.forEach(this::updateTaskStatusIfMissed);
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

    @Override
    public TaskResponse toggleFavorite(UUID taskId, UUID userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException(TASK_NOT_FOUND_MESSAGE + taskId));

        task.setFavorite(!task.isFavorite());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public List<TaskResponse> getTasksByDate(UUID userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Task> tasks = taskRepository.findByUserIdAndDueDateBetween(userId, startOfDay, endOfDay);
        return tasks.stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public UUID getTaskByTitle(String naturalLanguageInput, UUID userId) {
        if (naturalLanguageInput == null || naturalLanguageInput.isBlank()) {
            throw new AiServiceException("Task title cannot be empty", null);
        }
        String cleanedTitle = naturalLanguageInput.trim();
        try {
            return Optional.ofNullable(taskRepository.findByTitleAndUserId(cleanedTitle, userId))
                    .orElseThrow(() -> new AiServiceException(
                            "No task found with title: " + cleanedTitle + " for user " + userId, null));
        } catch (Exception e) {
            throw new AiServiceException("Failed to retrieve task by title", e);
        }
    }

    private String normalizeText(String text) {
        if (text == null) return null;
        return text.trim().replaceAll("\\s+", " ");
    }

    private void updateTaskStatusIfMissed(Task task){
        if (task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDateTime.now())
                && task.getStatus() != TaskStatus.MISSED
                && task.getStatus() != TaskStatus.DONE
        ) {
            task.setStatus(TaskStatus.MISSED);
            taskRepository.save(task);
        }
    }


}
