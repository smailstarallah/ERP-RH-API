package ma.digitalia.suividutemps.dto;

public record DashboardKpisDto(
        int utilisationRate,
        int utilisationChange,
        int overtimeRate,
        int overtimeChange,
        int averageProductivity,
        int productivityChange,
        int overloadedTeams,
        int overloadChange
) {}
