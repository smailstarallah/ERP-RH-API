package ma.digitalia.gestionconges.controllers;

import ma.digitalia.gestionconges.dto.KPILeavesDTO;
import ma.digitalia.gestionconges.dto.LeaveStatisticsDTO;
import ma.digitalia.gestionconges.services.LeaveStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-statistics")
@CrossOrigin(origins = "*")
public class LeaveStatisticsController {

    @Autowired
    private LeaveStatisticsService leaveStatisticsService;

    @GetMapping
    public ResponseEntity<LeaveStatisticsDTO> getLeaveStatistics() {
        try {
            LeaveStatisticsDTO statistics = leaveStatisticsService.getLeaveStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/department/{managerId}")
    public ResponseEntity<LeaveStatisticsDTO> getLeaveStatisticsByManager(@PathVariable Long managerId) {
        try {
            // Cette méthode pourrait être étendue pour filtrer par manager spécifique
            LeaveStatisticsDTO statistics = leaveStatisticsService.getLeaveStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/kpi")
    public ResponseEntity<List<KPILeavesDTO>> getKPILeaves() {
        try {
            List<KPILeavesDTO> kpiList = leaveStatisticsService.getKPILeaves();
            return ResponseEntity.ok(kpiList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
