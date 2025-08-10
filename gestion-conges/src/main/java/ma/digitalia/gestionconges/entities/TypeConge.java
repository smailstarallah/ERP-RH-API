package ma.digitalia.gestionconges.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "type_conge")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TypeConge {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du type de congé est obligatoire")
    @Column(nullable = false, unique = true)
    private String nom;

    @NotNull(message = "Le nombre de jours maximum est obligatoire")
    @Min(value = 1, message = "Le nombre de jours maximum doit être supérieur à 0")
    @Column(name = "nombre_jours_max", nullable = false)
    private Integer nombreJoursMax;

    @NotNull(message = "Le statut payé est obligatoire")
    @Column(nullable = false)
    private Boolean paye;

    @Column(length = 7) // Pour stocker les couleurs hex (#FFFFFF)
    private String couleur;
}
