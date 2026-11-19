package com.practiceHackathon.taskmanager.service;

import com.practiceHackathon.taskmanager.exception.ResourceNotFoundException;
import com.practiceHackathon.taskmanager.model.Task;
import com.practiceHackathon.taskmanager.model.TaskStatus;
import com.practiceHackathon.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service class handling business logic for Task management.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    // Constructor injection for dependency injection
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Creates a new task in the database.
     * Sets status to PENDING if not provided.
     */
    public Task createTask(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        return taskRepository.save(task);
    }

    /**
     * Retrieves all tasks from the database.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Retrieves a single task by its ID.
     * Throws ResourceNotFoundException (404) if the task does not exist.
     */
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    /**
     * Updates an existing task with new data.
     * Throws ResourceNotFoundException (404) if the task does not exist.
     */
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        if (updatedTask.getStatus() != null) {
            existingTask.setStatus(updatedTask.getStatus());
        }
        existingTask.setDueDate(updatedTask.getDueDate());
        return taskRepository.save(existingTask);
    }

    /**
     * Deletes a task by its ID.
     * Throws ResourceNotFoundException (404) if the task does not exist.
     */
    public void deleteTask(Long id) {
        Task existingTask = getTaskById(id);
        taskRepository.delete(existingTask);
    }
}
