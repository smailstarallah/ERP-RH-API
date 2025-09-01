package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoldeCongesDTO {
    private String employe;
    private String departement;
    private Integer solde;
}
