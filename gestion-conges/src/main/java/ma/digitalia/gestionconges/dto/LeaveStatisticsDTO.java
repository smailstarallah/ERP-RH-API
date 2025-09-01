package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveStatisticsDTO {
    private List<DepartmentLeaveDataDTO> leaveData;
    private List<LeaveTypeDataDTO> leaveTypeData;
    private List<MonthlyTrendDataDTO> monthlyTrendData;
    private List<CongesParDepartementDTO> congesParDepartement;
    private List<RepartitionCongesDTO> repartitionConges;
    private List<AbsencesMensuellesDTO> absencesMensuelles;
    private List<List<Integer>> heatmap;
    private List<String> heatmapXLabels;
    private List<String> heatmapYLabels;
    private List<CongesVsAllouesDTO> congesVsAlloues;
    private List<SoldeCongesDTO> soldeConges;
    private List<AbsencesParJourSemaineDTO> absencesParJourSemaine;
}
