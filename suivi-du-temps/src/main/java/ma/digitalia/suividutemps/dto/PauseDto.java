package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PauseDto {
    private String id;
    private String type;
    private String start;
    private String end;
    private int duration;
}

