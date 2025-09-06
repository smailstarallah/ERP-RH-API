package ma.digitalia.generationfichepaie.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableElementDto {
    private String element;
    private double budget;
    private double consomme;
    private double taux;
    private String impact;
}