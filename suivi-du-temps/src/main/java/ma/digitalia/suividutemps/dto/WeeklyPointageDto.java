package ma.digitalia.suividutemps.dto;

import java.util.List;

public class WeeklyPointageDto {
    private WeekStatsDto weekStats;
    private List<WeekRowDto> weekRows;

    public WeeklyPointageDto(WeekStatsDto weekStats, List<WeekRowDto> weekRows) {
        this.weekStats = weekStats;
        this.weekRows = weekRows;
    }

    public WeekStatsDto getWeekStats() {
        return weekStats;
    }

    public void setWeekStats(WeekStatsDto weekStats) {
        this.weekStats = weekStats;
    }

    public List<WeekRowDto> getWeekRows() {
        return weekRows;
    }

    public void setWeekRows(List<WeekRowDto> weekRows) {
        this.weekRows = weekRows;
    }
}

