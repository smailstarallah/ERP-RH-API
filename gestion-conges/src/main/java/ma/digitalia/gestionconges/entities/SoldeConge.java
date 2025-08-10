package ma.digitalia.gestionconges.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Users;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solde_conge", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "type_conge_id", "annee"})
})
public class SoldeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'année est obligatoire")
    @Min(value = 2020, message = "L'année doit être valide")
    @Column(nullable = false)
    private Integer annee;

    @NotNull(message = "Le solde initial est obligatoire")
    @Min(value = 0, message = "Le solde initial ne peut pas être négatif")
    @Column(name = "solde_initial", nullable = false)
    private Integer soldeInitial;

    @NotNull(message = "Le solde pris est obligatoire")
    @Min(value = 0, message = "Le solde pris ne peut pas être négatif")
    @Column(name = "solde_pris", nullable = false)
    private Integer soldePris = 0;

    @NotNull(message = "Le solde restant est obligatoire")
    @Min(value = 0, message = "Le solde restant ne peut pas être négatif")
    @Column(name = "solde_restant", nullable = false)
    private Integer soldeRestant;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge type;
}