package com.salah.taskmate.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salah.taskmate.category.CategoryService;
import com.salah.taskmate.category.dto.CategoryRequest;
import com.salah.taskmate.category.dto.CategoryResponse;
import com.salah.taskmate.shared.exception.AiServiceException;
import com.salah.taskmate.task.TaskService;
import com.salah.taskmate.task.dto.TaskRequest;
import com.salah.taskmate.task.dto.TaskResponse;
import com.salah.taskmate.task.enums.TaskPriority;
import com.salah.taskmate.task.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_TASKS_FOR_SUMMARY = 50;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private final DeepSeekChatModel deepSeekChatModel;
    private final TaskService taskService;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;
    private final TaskPromptTemplates promptTemplates;

    @Override
    public TaskResponse parseAndCreateTask(String naturalLanguageInput, UUID userId) {
        try {
            String categoriesJson = getCategoriesAsJson(userId);
            String currentDateTime = getCurrentDateTime();

            Map<String, Object> variables = createTaskParsingVariables(naturalLanguageInput, categoriesJson, currentDateTime);
            Prompt prompt = promptTemplates.createTaskParsingPrompt(naturalLanguageInput, categoriesJson).create(variables);

            String jsonResponse = callAiAndExtractResponse(prompt, "parseAndCreateTask");
            TaskRequest taskRequest = parseTaskRequest(jsonResponse);

            validateTaskRequest(taskRequest);

            return taskService.createTask(userId, taskRequest);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response into TaskRequest", e);
            throw new AiServiceException("Failed to parse task from natural language input", e);
        } catch (Exception e) {
            log.error("Error calling DeepSeek API for task creation", e);
            throw new AiServiceException("AI service temporarily unavailable", e);
        }
    }

    @Override
    public TaskResponse categorizeTask(UUID taskId, UUID userId) {
        try {
            TaskResponse existingTask = taskService.getTaskById(taskId, userId);
            String categoriesJson = getCategoriesAsJson(userId);
            String taskContent = buildTaskContent(existingTask);

            Map<String, Object> variables = createCategorizationVariables(taskContent, categoriesJson);
            Prompt prompt = promptTemplates.createCategorizationPrompt(taskContent, categoriesJson).create(variables);

            String jsonResponse = callAiAndExtractResponse(prompt, "categorizeTask");

            if (isEmptyResponse(jsonResponse)) {
                log.warn("AI returned empty response for categorization");
                return existingTask;
            }

            List<CategorySuggestionDto> suggestions = parseCategorySuggestions(jsonResponse);

            if (suggestions.isEmpty()) {
                log.info("AI suggested no categories for task {} with content: '{}'", taskId, taskContent);
                return existingTask;
            }

            List<UUID> categoryIds = processCategories(suggestions, userId);

            return updateTaskWithCategories(taskId, userId, categoryIds, existingTask);

        } catch (Exception e) {
            log.error("Error categorizing task {} for user: {}", taskId, userId, e);
            return taskService.getTaskById(taskId, userId);
        }
    }

    @Override
    public String summarizeTasks(List<UUID> taskIds, UUID userId) {
        if (taskIds.isEmpty()) {
            return "No tasks to summarize.";
        }

        List<TaskResponse> tasks = retrieveValidTasks(taskIds, userId);

        if (tasks.isEmpty()) {
            return "No valid tasks found to summarize.";
        }

        return generateTasksSummary(tasks, "summarizeTasks");
    }

    @Override
    public String summarizeDailyTasks(LocalDate date, UUID userId) {
        List<TaskResponse> dailyTasks = taskService.getTasksByDate(userId, date);

        if (dailyTasks.isEmpty()) {
            return String.format("No tasks scheduled for %s.", date.format(DATE_FORMATTER));
        }

        return generateDailySummary(date, dailyTasks);
    }

    @Override
    public String summarizeAllTasks(UUID userId) {
        try {
            Page<TaskResponse> allTasksPage = taskService.getAllTasks(userId, 0, MAX_TASKS_FOR_SUMMARY);

            if (allTasksPage.isEmpty()) {
                return "You don't have any tasks yet. Start by creating your first task!";
            }

            return generateAllTasksSummary(allTasksPage.getContent());

        } catch (Exception e) {
            log.error("Error summarizing all tasks for user: {}", userId, e);
            return "Unable to generate comprehensive task summary at this time.";
        }
    }

    @Override
    public String healthCheck(UUID userId) {
        try {
            Prompt prompt = new Prompt("Say 'Hello the deepseek api is working'");
            ChatResponse response = deepSeekChatModel.call(prompt);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("DeepSeek API health check failed", e);
            return "DeepSeek API health check failed";
        }
    }

    @Override
    public UserIntent determineUserIntent(String naturalLanguageInput) {
        try {
            Map<String, Object> variables = Map.of("userInput", naturalLanguageInput);
            Prompt prompt = promptTemplates.createIntentDetectionPrompt(naturalLanguageInput).create(variables);

            ChatResponse response = deepSeekChatModel.call(prompt);
            String intentStr = response.getResult().getOutput().getText().trim().toUpperCase();

            return parseUserIntent(intentStr);

        } catch (Exception e) {
            log.error("Error detecting user intent", e);
            return UserIntent.UNKNOWN;
        }
    }

    @Override
    public Object handleUserInput(String naturalLanguageInput, UUID userId, List<UUID> taskIds, LocalDate date) {
        UserIntent intent = determineUserIntent(naturalLanguageInput);

        return switch (intent) {
            case CREATE_TASK -> parseAndCreateTask(naturalLanguageInput, userId);
            case SUMMARIZE_TASK -> handleSummarizeTask(userId, taskIds, date);
            case CATEGORIZE_TASK -> handleCategorizeTask(naturalLanguageInput, userId);
            default -> "Sorry, I couldn't understand your request.";
        };
    }

    private String getCategoriesAsJson(UUID userId) {
        List<CategoryResponse> userCategories = categoryService.getCategoriesByUserId(userId);
        return categoriesToJson(userCategories);
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    private Map<String, Object> createTaskParsingVariables(String input, String categories, String dateTime) {
        return Map.of(
                "userInput", input,
                "categories", categories,
                "currentDateTime", dateTime
        );
    }

    private Map<String, Object> createCategorizationVariables(String taskContent, String categories) {
        return Map.of(
                "taskContent", taskContent,
                "categories", categories
        );
    }

    private String callAiAndExtractResponse(Prompt prompt, String operation) {
        ChatResponse response = deepSeekChatModel.call(prompt);
        String jsonResponse = cleanJsonResponse(response.getResult().getOutput().getText());
        log.debug("AI raw response for {}: {}", operation, jsonResponse);
        return jsonResponse;
    }

    private TaskRequest parseTaskRequest(String jsonResponse) throws JsonProcessingException {
        return objectMapper.readValue(jsonResponse, TaskRequest.class);
    }

    private String buildTaskContent(TaskResponse task) {
        return task.getTitle() + " " + (task.getContent() != null ? task.getContent() : "");
    }

    private boolean isEmptyResponse(String response) {
        return response == null || response.trim().isEmpty();
    }

    private List<CategorySuggestionDto> parseCategorySuggestions(String jsonResponse) {
        try {
            return objectMapper.readValue(
                    jsonResponse,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CategorySuggestionDto.class)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI categorization response as JSON: {}", jsonResponse, e);
            return new ArrayList<>();
        }
    }

    private List<UUID> processCategories(List<CategorySuggestionDto> suggestions, UUID userId) {
        List<UUID> categoryIds = new ArrayList<>();

        for (CategorySuggestionDto suggestion : suggestions) {
            try {
                UUID categoryId = findOrCreateCategory(suggestion, userId);
                categoryIds.add(categoryId);
                log.debug("Successfully processed category suggestion: {} -> {}", suggestion.getName(), categoryId);
            } catch (Exception e) {
                log.error("Failed to process category suggestion: {}", suggestion, e);
            }
        }

        return categoryIds;
    }

    private TaskResponse updateTaskWithCategories(UUID taskId, UUID userId, List<UUID> categoryIds, TaskResponse existingTask) {
        if (!categoryIds.isEmpty()) {
            TaskResponse updatedTask = taskService.addCategories(taskId, userId, categoryIds);
            log.info("Successfully categorized task {} with {} categories for user: {}", taskId, categoryIds.size(), userId);
            return updatedTask;
        } else {
            log.info("No suitable categories could be created for task {} for user: {}", taskId, userId);
            return existingTask;
        }
    }

    private List<TaskResponse> retrieveValidTasks(List<UUID> taskIds, UUID userId) {
        List<TaskResponse> tasks = new ArrayList<>();

        for (UUID taskId : taskIds) {
            try {
                TaskResponse task = taskService.getTaskById(taskId, userId);
                tasks.add(task);
            } catch (Exception e) {
                log.warn("Failed to retrieve task {} for user {}: {}", taskId, userId, e.getMessage());
            }
        }

        return tasks;
    }

    private String generateTasksSummary(List<TaskResponse> tasks, String operation) {
        try {
            String tasksJson = tasksToJson(tasks);
            Map<String, Object> variables = Map.of("tasksData", tasksJson);
            Prompt prompt = promptTemplates.createTaskSummaryPrompt(tasksJson).create(variables);

            return callAiAndExtractResponse(prompt, operation);

        } catch (Exception e) {
            log.error("Error summarizing tasks", e);
            return "Unable to generate task summary at this time.";
        }
    }

    private String generateDailySummary(LocalDate date, List<TaskResponse> tasks) {
        try {
            String dateFormatted = date.format(DATE_FORMATTER);
            String tasksJson = tasksToJson(tasks);

            Map<String, Object> variables = Map.of(
                    "date", dateFormatted,
                    "tasksData", tasksJson
            );

            Prompt prompt = promptTemplates.createDailyTaskSummaryPrompt(dateFormatted, tasksJson).create(variables);
            return callAiAndExtractResponse(prompt, "summarizeDailyTasks");

        } catch (Exception e) {
            log.error("Error summarizing daily tasks", e);
            String dateFormatted = date.format(DATE_FORMATTER);
            return String.format("You have %d task(s) scheduled for %s.", tasks.size(), dateFormatted);
        }
    }

    private String generateAllTasksSummary(List<TaskResponse> tasks) {
        try {
            String tasksJson = tasksToJson(tasks);
            Map<String, Object> variables = Map.of("tasksData", tasksJson);
            Prompt prompt = promptTemplates.createAllTasksSummaryPrompt(tasksJson).create(variables);

            return callAiAndExtractResponse(prompt, "summarizeAllTasks");

        } catch (Exception e) {
            log.error("Error summarizing all tasks", e);
            return "Unable to generate comprehensive task summary at this time.";
        }
    }

    private UserIntent parseUserIntent(String intentStr) {
        return switch (intentStr) {
            case "CREATE_TASK" -> UserIntent.CREATE_TASK;
            case "SUMMARIZE_TASK" -> UserIntent.SUMMARIZE_TASK;
            case "CATEGORIZE_TASK" -> UserIntent.CATEGORIZE_TASK;
            default -> UserIntent.UNKNOWN;
        };
    }

    private Object handleSummarizeTask(UUID userId, List<UUID> taskIds, LocalDate date) {
        if (taskIds != null && !taskIds.isEmpty()) {
            return summarizeTasks(taskIds, userId);
        } else if (date != null) {
            return summarizeDailyTasks(date, userId);
        } else {
            return summarizeAllTasks(userId);
        }
    }

    private Object handleCategorizeTask(String naturalLanguageInput, UUID userId) {
        UUID taskId = taskService.getTaskByTitle(naturalLanguageInput, userId);
        return categorizeTask(taskId, userId);
    }

    private String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return "";
        }
        return response.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }

    private void validateTaskRequest(TaskRequest taskRequest) {
        if (taskRequest.getTitle() == null || taskRequest.getTitle().isBlank()) {
            throw new AiServiceException("AI failed to generate a valid task title", null);
        }

        if (taskRequest.getTitle().length() > MAX_TITLE_LENGTH) {
            taskRequest.setTitle(taskRequest.getTitle().substring(0, MAX_TITLE_LENGTH));
        }

        if (taskRequest.getContent() != null && taskRequest.getContent().length() > MAX_CONTENT_LENGTH) {
            taskRequest.setContent(taskRequest.getContent().substring(0, MAX_CONTENT_LENGTH));
        }

        if (taskRequest.getStatus() == null) {
            taskRequest.setStatus(TaskStatus.PENDING);
        }

        if (taskRequest.getPriority() == null) {
            taskRequest.setPriority(TaskPriority.MEDIUM);
        }

        if (taskRequest.getDueDate() != null && taskRequest.getDueDate().isBefore(LocalDateTime.now())) {
            log.warn("AI generated a past due date: {}. Setting to null.", taskRequest.getDueDate());
            taskRequest.setDueDate(null);
        }
    }

    private String categoriesToJson(List<CategoryResponse> categories) {
        try {
            return objectMapper.writeValueAsString(categories);
        } catch (JsonProcessingException e) {
            log.error("Error converting categories to JSON", e);
            return "[]";
        }
    }

    private String tasksToJson(List<TaskResponse> tasks) {
        try {
            return objectMapper.writeValueAsString(tasks);
        } catch (JsonProcessingException e) {
            log.error("Error converting tasks to JSON", e);
            return "[]";
        }
    }

    private UUID findOrCreateCategory(CategorySuggestionDto suggestion, UUID userId) {
        return categoryService.findByNameAndUserId(suggestion.getName(), userId)
                .map(CategoryResponse::getId)
                .orElseGet(() -> createNewCategory(suggestion, userId));
    }

    private UUID createNewCategory(CategorySuggestionDto suggestion, UUID userId) {
        try {
            CategoryRequest request = CategoryRequest.builder()
                    .name(suggestion.getName())
                    .icon(suggestion.getIcon())
                    .color(suggestion.getColor())
                    .build();

            CategoryResponse created = categoryService.createCategory(userId, request);
            log.info("Created new category '{}' for user {}", created.getName(), userId);
            return created.getId();

        } catch (Exception e) {
            log.error("Failed to create category '{}' for user {}", suggestion.getName(), userId, e);
            throw new AiServiceException("Failed to create new category from AI suggestion", e);
        }
    }
}