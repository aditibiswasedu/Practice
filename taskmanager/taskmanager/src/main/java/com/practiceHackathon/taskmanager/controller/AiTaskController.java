package com.practiceHackathon.taskmanager.controller;

import com.practiceHackathon.taskmanager.dto.SubtaskSuggestionRequest;
import com.practiceHackathon.taskmanager.dto.SubtaskSuggestionResponse;
import com.practiceHackathon.taskmanager.service.GeminiAiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import java.util.Map;

/**
 * REST Controller for AI-assisted task operations under /api/ai.
 */
@RestController
@RequestMapping("/api/ai")
public class AiTaskController {

    private final GeminiAiService geminiAiService;

    public AiTaskController(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    /**
     * POST /api/ai/suggest-subtasks
     * Accepts a task description and returns AI-generated subtasks using Gemini.
     */
    @PostMapping("/suggest-subtasks")
    public ResponseEntity<SubtaskSuggestionResponse> suggestSubtasks(
            @Valid @RequestBody SubtaskSuggestionRequest request) {
        SubtaskSuggestionResponse response = geminiAiService.generateSubtasks(request.getTaskDescription());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles missing configuration (e.g. GEMINI_API_KEY not set).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleMissingConfiguration(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Configuration Error",
                        "message", ex.getMessage()
                ));
    }

    /**
     * Handles errors returned by the external Gemini API (e.g. invalid key, quota exceeded).
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleGeminiApiError(RestClientResponseException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of(
                        "error", "Gemini API Error",
                        "statusCode", ex.getStatusCode().value(),
                        "details", ex.getResponseBodyAsString()
                ));
    }
}
