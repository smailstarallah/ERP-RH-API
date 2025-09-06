package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointagePlannedDto {
    private Integer dayOfWeek; // 0 = Dimanche, 1 = Lundi, etc.
    private PointageProjectDto project;
    private Integer plannedHours;
}
