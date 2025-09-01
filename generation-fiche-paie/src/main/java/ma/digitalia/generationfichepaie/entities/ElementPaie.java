package ma.digitalia.generationfichepaie.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.generationfichepaie.Enum.ModeCalcul;
import ma.digitalia.generationfichepaie.Enum.TypeElement;
import ma.digitalia.gestionutilisateur.entities.Employe;

import java.math.BigDecimal;

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

}