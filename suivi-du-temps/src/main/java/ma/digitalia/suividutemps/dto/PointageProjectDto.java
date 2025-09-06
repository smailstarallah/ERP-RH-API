package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointageProjectDto {
    private Long id;
    private String name;
    private String color;
    private String client; // optionnel
}
