package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDataDTO {
    private String month;
    private Integer current;
    private Integer previous;
}
