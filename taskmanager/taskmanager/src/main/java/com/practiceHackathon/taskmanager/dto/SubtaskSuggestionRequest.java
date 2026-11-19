package com.practiceHackathon.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for receiving the user's task description for AI subtask generation.
 */
public class SubtaskSuggestionRequest {

    @NotBlank(message = "Task description is required")
    private String taskDescription;

    public SubtaskSuggestionRequest() {
    }

    public SubtaskSuggestionRequest(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
}
