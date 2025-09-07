package com.salah.taskmate.ai;

import com.salah.taskmate.task.dto.TaskResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AiService {
    TaskResponse parseAndCreateTask(String naturalLanguageInput, UUID userId);
    TaskResponse categorizeTask(UUID taskId, UUID userId);
    String summarizeTasks(List<UUID> taskIds, UUID userId);
    String summarizeDailyTasks(LocalDate date, UUID userId);
    String summarizeAllTasks(UUID userId);
    String healthCheck(UUID userId);
    UserIntent determineUserIntent(String naturalLanguageInput);
    Object handleUserInput(String naturalLanguageInput, UUID userId, List<UUID> taskIds, LocalDate date);
}
