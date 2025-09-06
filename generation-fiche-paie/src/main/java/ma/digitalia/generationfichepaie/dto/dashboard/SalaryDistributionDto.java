package ma.digitalia.generationfichepaie.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDistributionDto {
    private String departement;
    private int nombreEmployes;
    private double masseSalariale;
    private double salaireMoyen;
    private SalaryRangeDto tranches;
}
