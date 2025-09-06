package ma.digitalia.generationfichepaie.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.generationfichepaie.Enum.ModeCalcul;
import ma.digitalia.generationfichepaie.Enum.TypeElement;
import ma.digitalia.generationfichepaie.dto.AjoutElementPaieDTO;
import ma.digitalia.gestionutilisateur.entities.Employe;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "element_paie")
public class ElementPaie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeElement type;
    private String sousType;

    private String libelle;

    @Enumerated(EnumType.STRING)
    private ModeCalcul modeCalcul;

    private BigDecimal montant;
    private BigDecimal taux;
    private BigDecimal base;

    private String description;

    // Flags pour le régime fiscal/social
    private boolean soumisIR = true;
    private boolean soumisCNSS = true;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "fiche_paie_id")
    private FichePaie fichePaie;

    public ElementPaie(AjoutElementPaieDTO dto, Employe employe) {
        this.type = dto.getType();
        this.sousType = dto.getSousType();
        this.libelle = dto.getLibelle();
        this.montant = dto.getMontant();
        this.taux = dto.getTaux();
        this.base = dto.getBase();
        this.description = dto.getDescription();
        this.soumisIR = dto.isSoumisIR();
        this.soumisCNSS = dto.isSoumisCNSS();
        this.employe = employe;
        if (Objects.equals(dto.getUniteCalcul(), "HEURE")) {
            this.modeCalcul = ModeCalcul.PAR_HEURE;
        } else if (Objects.equals(dto.getUniteCalcul(), "JOUR")) {
            this.modeCalcul = ModeCalcul.PAR_JOUR;
        } else {
            this.modeCalcul = dto.getModeCalcul();
        }
    }

}