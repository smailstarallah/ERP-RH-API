package ma.digitalia.generationfichepaie.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {
    private KpisDto kpis;
    private List<SalaryStructureDto> salaryStructure;
    private List<SalaryEvolutionDto> salaryEvolution;
    private List<SalaryDistributionDto> salaryDistribution;
    private List<PayrollQualityDto> payrollQuality;
    private List<VariableElementDto> variableElements;
    private List<ComplianceActionDto> complianceActions;
    private LocalDateTime lastUpdate;
    private String status;
}