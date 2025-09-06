package ma.digitalia.suividutemps.dto;

import java.util.List;

public record DashboardResponseDto(
        DashboardKpisDto kpis,
        List<WorkloadDataDto> workloadData,
        List<ProductivityDataDto> productivityData,
        List<ProjectDataDto> projectData,
        List<WeeklyProductivityDto> weeklyProductivity,
        List<TimeDistributionDto> timeDistribution,
        List<WellnessDataDto> wellnessData,
        List<TeamEfficiencyDto> teamEfficiency
) {}
