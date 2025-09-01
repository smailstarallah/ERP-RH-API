package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicDepartmentLeaveDataDTO {
    private String department;
    private Map<String, Integer> leaveTypeCount; // Nom du type de congé -> nombre de jours
}
