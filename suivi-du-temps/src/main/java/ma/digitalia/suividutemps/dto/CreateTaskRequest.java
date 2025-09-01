package ma.digitalia.suividutemps.dto;

public record CreateTaskRequest(String nom, Double estimationHeures, String priority, String description, Long userId) {}