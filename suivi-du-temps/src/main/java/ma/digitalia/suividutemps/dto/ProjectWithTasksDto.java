package ma.digitalia.suividutemps.dto;

import java.util.List;

public class ProjectWithTasksDto {
    public Long id;
    public String nom;
    public String client;
    public String description;
    public List<TaskDto> taches;

    public static class TaskDto {
        public Long id;
        public String nom;
        public String priority;
        public String statut;
    }
}

