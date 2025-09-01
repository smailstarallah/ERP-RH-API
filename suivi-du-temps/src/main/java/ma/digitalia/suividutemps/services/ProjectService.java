package ma.digitalia.suividutemps.services;

import ma.digitalia.suividutemps.dto.CreateProjectRequest;
import ma.digitalia.suividutemps.dto.CreateTaskRequest;
import ma.digitalia.suividutemps.dto.ProjectWithTasksDto;
import ma.digitalia.suividutemps.entities.Projet;
import ma.digitalia.suividutemps.entities.Tache;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProjectService {
    @Transactional
    Projet createProject(CreateProjectRequest request);

    @Transactional
    Tache createTaskForProject(Long projetId, CreateTaskRequest request);

    @Transactional(readOnly = true)
    List<Projet> getAllProjectsWithTasks();

    void changeTaskStatus(Long taskId, String status);

    List<ProjectWithTasksDto> getAllProjectsWithTasksDto();
}
