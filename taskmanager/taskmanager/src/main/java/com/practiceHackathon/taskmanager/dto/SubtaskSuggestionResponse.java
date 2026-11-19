package com.practiceHackathon.taskmanager.dto;

import java.util.List;

/**
 * DTO for sending AI-generated subtasks back to the client.
 */
public class SubtaskSuggestionResponse {

    private String originalTask;
    private List<String> subtasks;

    public SubtaskSuggestionResponse() {
    }

    public SubtaskSuggestionResponse(String originalTask, List<String> subtasks) {
        this.originalTask = originalTask;
        this.subtasks = subtasks;
    }

    public String getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(String originalTask) {
        this.originalTask = originalTask;
    }

    public List<String> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<String> subtasks) {
        this.subtasks = subtasks;
    }
}
