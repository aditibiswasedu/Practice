package com.practiceHackathon.taskmanager.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.practiceHackathon.taskmanager.dto.SubtaskSuggestionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to communicate with Google Gemini API for task breakdowns.
 */
@Service
public class GeminiAiService {

    private final String apiKey;
    private final String apiUrl;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiAiService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a list of actionable subtasks for a given task description using Gemini API.
     *
     * @param taskDescription The description of the task to break down.
     * @return SubtaskSuggestionResponse containing original task and generated subtasks.
     */
    public SubtaskSuggestionResponse generateSubtasks(String taskDescription) {
        // Validate API key presence
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured. Please set the GEMINI_API_KEY environment variable.");
        }

        // Formulate prompt
        String prompt = "You are an expert productivity assistant. Break down the following task into 3 to 6 concise, actionable subtasks. "
                + "Return ONLY the subtasks, one per line. Do NOT include numbers, bullet points, headers, or any introductory or concluding text.\n\n"
                + "Task: " + taskDescription;

        // Construct request payload required by Gemini generateContent API
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        try {
            // Send POST request with x-goog-api-key HTTP header
            String rawResponse = restClient.post()
                    .uri(apiUrl)
                    .header("x-goog-api-key", apiKey.trim())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // Parse response JSON: candidates[0].content.parts[0].text
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                return new SubtaskSuggestionResponse(taskDescription, Collections.emptyList());
            }

            // Split into clean subtask lines
            List<String> subtasks = Arrays.stream(textNode.asText().split("\\r?\\n"))
                    .map(String::trim)
                    .map(line -> line.replaceFirst("^[0-9]+[.)]\\s*", "")) // Clean "1. " or "1) "
                    .map(line -> line.replaceFirst("^[-*•]\\s*", ""))      // Clean "- ", "* ", "• "
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());

            return new SubtaskSuggestionResponse(taskDescription, subtasks);

        } catch (RestClientResponseException ex) {
            // Re-throw to be captured by the controller exception handler with error details
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to process Gemini API response: " + ex.getMessage(), ex);
        }
    }
}
