package ma.digitalia.generationfichepaie.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpisDto {
    private KpiValueDto masseSalariale;
    private KpiValueDto pourcentageCA;
    private KpiValueDto tauxErreur;
    private KpiValueDto coutBulletin;
}