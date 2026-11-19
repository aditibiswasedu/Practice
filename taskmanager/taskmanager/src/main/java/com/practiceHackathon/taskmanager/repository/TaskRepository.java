package com.practiceHackathon.taskmanager.repository;

import com.practiceHackathon.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Task entities.
 * Automatically provides CRUD methods (save, findById, findAll, deleteById, etc.).
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
