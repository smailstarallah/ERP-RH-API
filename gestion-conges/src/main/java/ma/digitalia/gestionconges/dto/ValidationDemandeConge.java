package ma.digitalia.gestionconges.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.gestionconges.Enum.StatutDemande;
import ma.digitalia.gestionconges.entities.DemandeConge;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDemandeConge {
    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreJours;
    private String typeConge;
    private String motif;
    String employeNom;
    StatutDemande statut;

    public ValidationDemandeConge(DemandeConge demandeConge) {
        this.id = demandeConge.getId();
        this.dateDebut = demandeConge.getDateDebut();
        this.dateFin = demandeConge.getDateFin();
        this.nombreJours = demandeConge.getNombreJours();
        this.typeConge = demandeConge.getTypeConge().getNom();
        this.motif = demandeConge.getMotif();
        this.statut = demandeConge.getStatut();
        this.employeNom = demandeConge.getDemandeur().getNom() + " " + demandeConge.getDemandeur().getPreNom();
    }
}
