package ma.digitalia.suividutemps.dto;

public record WeeklyProductivityDto(
        String jour,
        int productivite,
        double heuresActives,
        int pauses
) {}
