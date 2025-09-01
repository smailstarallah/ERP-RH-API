package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KPILeavesDTO {
    private String title;
    private double value;
    private String unit;
    private double change;
    private String icon;
    private String status;
}
