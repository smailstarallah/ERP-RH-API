package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.gestionconges.entities.SoldeConge;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoldeCongeDTO {

    private Integer annee;
    private Integer soldeInitial;
    private Integer soldePris;
    private Integer soldeRestant;
    private Long employeId;
    private String typeCongeLibelle;

    public SoldeCongeDTO(SoldeConge soldeConge) {
        this.annee = soldeConge.getAnnee();
        this.soldeInitial = soldeConge.getSoldeInitial();
        this.soldePris = soldeConge.getSoldePris();
        this.soldeRestant = soldeConge.getSoldeRestant();
        this.employeId = soldeConge.getEmploye().getId();
        this.typeCongeLibelle = soldeConge.getType() != null ? soldeConge.getType().getNom() : null;
    }
}