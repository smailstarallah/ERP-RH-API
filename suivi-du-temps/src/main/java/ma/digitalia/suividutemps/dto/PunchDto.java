package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PunchDto {
    private String id;
    private String date;
    private String start;
    private String end;
    private String status;
    private int effectiveWorkMinutes;
    private int totalPauseMinutes;
    private Integer overtime;
    private String location;
    private List<PauseDto> pauses;
    private List<AnomalyDto> anomalies;
    private int dailyWorkObjectiveMinutes;
}
