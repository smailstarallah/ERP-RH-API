package ma.digitalia.gestionconges.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDemande {
    @NotNull
    private Long idDemande;
    private String commentaire;
    @NotNull
    private String decision;
}
