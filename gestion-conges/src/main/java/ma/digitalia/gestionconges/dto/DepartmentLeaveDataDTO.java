package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentLeaveDataDTO {
    private String department;
    private Map<String, Integer> leaveTypes;
    private Map<String, String> leaveTypeColors; // Nom du type -> couleur hex
}
