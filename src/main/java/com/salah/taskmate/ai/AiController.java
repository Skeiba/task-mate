package com.salah.taskmate.ai;

import com.salah.taskmate.security.CustomUserDetails;
import com.salah.taskmate.shared.annotation.StandardApiResponse;
import com.salah.taskmate.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/parse-task")
    @StandardApiResponse(message = "Task parsed and created successfully")
    public ResponseEntity<TaskResponse> parseAndCreateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody String naturalLanguageInput) {

        TaskResponse response = aiService.parseAndCreateTask(naturalLanguageInput, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/chat")
    @StandardApiResponse(message = "Chat successful")
    public ResponseEntity<?> aiChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody String naturalLanguageInput,
            @RequestParam(required = false) List<UUID> taskIds,
            @RequestParam(required = false)LocalDate date) {

        Object response = aiService.handleUserInput(naturalLanguageInput, userDetails.getId(), taskIds, date);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/categorize/{taskId}")
    @StandardApiResponse(message = "Task categorized successfully")
    public ResponseEntity<TaskResponse> categorizeTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID taskId) {

        TaskResponse response = aiService.categorizeTask(taskId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/summarize")
    @StandardApiResponse(message = "Tasks summarized successfully")
    public ResponseEntity<String> summarizeTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<UUID> taskIds) {

        String summary = aiService.summarizeTasks(taskIds, userDetails.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summarize/daily")
    @StandardApiResponse(message = "Daily tasks summarized successfully")
    public ResponseEntity<String> summarizeDailyTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate date) {

        String summary = aiService.summarizeDailyTasks(date, userDetails.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summarize/all")
    @StandardApiResponse(message = "All tasks summarized successfully")
    public ResponseEntity<String> summarizeAllTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String summary = aiService.summarizeAllTasks(userDetails.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/health-check")
    @StandardApiResponse(message = "AI health check was successful")
    public ResponseEntity<String> aiHealthCheck(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String result = aiService.healthCheck(userDetails.getId());
        return ResponseEntity.ok(result);
    }

}

