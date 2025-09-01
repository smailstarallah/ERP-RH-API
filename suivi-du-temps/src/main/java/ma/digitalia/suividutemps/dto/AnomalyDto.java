package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnomalyDto {
    private String id;
    private String type;
    private String message;
    private String severity;
}

