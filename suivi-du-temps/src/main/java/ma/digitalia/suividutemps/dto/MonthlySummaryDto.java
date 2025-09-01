package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummaryDto {
    private int monthlyWorkObjectiveMinutes;
    private int totalEffectiveWorkMinutes;
    private int totalOvertimeMinutes;
    private int totalAnomalies;
    private int presenceDays;
    private int remoteDays;
}

