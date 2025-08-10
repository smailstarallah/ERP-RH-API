package ma.digitalia.gestionutilisateur.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@DiscriminatorValue("EMPLOYE")
public class Employe extends Users {

    @NotNull
    private String numeroEmploye;

    private String cin;

    private LocalDate dateEmbauche;

    private BigDecimal salairBase;

    private String poste;

    private BigDecimal tauxHoraire;

    private String adresse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Manager manager;
}
