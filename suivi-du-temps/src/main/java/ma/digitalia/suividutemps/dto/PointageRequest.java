package ma.digitalia.suividutemps.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PointageRequest {
    @NotNull(message = "L'ID de l'employé ne peut pas être nul")
    Long empId;
    String commentaire;
    @NotNull(message = "L'état du pointage ne peut pas être nul")
    String typePointage;
}
