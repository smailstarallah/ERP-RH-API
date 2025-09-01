package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyPresenceReportDto {
    private MonthlySummaryDto summary;
    private List<PunchDto> dailyPunches;
}

