package ma.digitalia.gestionutilisateur.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "manager")
@NoArgsConstructor
@Entity
@DiscriminatorValue("EMPLOYE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    @JsonIgnore
    private Manager manager;
}
