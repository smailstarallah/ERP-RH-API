package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointageResponseDto {
    private List<PointageActivityDto> activities;
    private List<PointagePlannedDto> planned;
}
