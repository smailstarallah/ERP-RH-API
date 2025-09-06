package ma.digitalia.generationfichepaie.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceActionDto {
    private String id;
    private String title;
    private String description;
    private String priority;
    private String deadline;
    private Integer progress;
    private List<String> actions;
}