package ma.digitalia.suividutemps.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import ma.digitalia.gestionutilisateur.entities.Employe;

@Data
@AllArgsConstructor
@Entity
@Table(name = "rapport_temps")
public class RapportTemps {

    /**
     * Identifiant unique du rapport
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Période du rapport (ex: "2024-01", "2024-Q1", "2024")
     */
    @NotBlank(message = "La période ne peut pas être vide")
    @Size(max = 50, message = "La période ne peut pas dépasser 50 caractères")
    @Column(name = "periode", nullable = false, length = 50)
    private String periode;

    /**
     * Total des heures travaillées durant la période
     * Stocké en base sous forme de nombre de secondes
     */
    @NotNull(message = "Le total des heures travaillées est obligatoire")
    @Column(name = "total_heures_travaillees", nullable = false)
    private Duration totalHeuresTravaillees;

    /**
     * Total des heures supplémentaires effectuées
     * Stocké en base sous forme de nombre de secondes
     */
    @Column(name = "total_heures_supplementaires")
    private Duration totalHeuresSupplementaires;

    /**
     * Nombre de retards enregistrés durant la période
     */
    @Min(value = 0, message = "Le nombre de retards ne peut pas être négatif")
    @Column(name = "nombre_retards")
    private Integer nombreRetards;

    /**
     * Taux de présence en pourcentage (0.00 à 100.00)
     */
    @DecimalMin(value = "0.00", message = "Le taux de présence ne peut pas être négatif")
    @DecimalMax(value = "100.00", message = "Le taux de présence ne peut pas dépasser 100%")
    @Digits(integer = 3, fraction = 2, message = "Le taux de présence doit avoir au maximum 3 chiffres entiers et 2 décimales")
    @Column(name = "taux_presence", precision = 5, scale = 2)
    private BigDecimal tauxPresence;

    @Column(name = "nombre_jours_absence", precision = 5, scale = 2)
    private Integer nombreJoursAbsence;

    /**
     * Identifiant de l'employé concerné par ce rapport
     * Utilisé pour les relations avec l'entité Employe
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    @JsonIgnore
    private Employe employe;

    public RapportTemps() {
        this.totalHeuresTravaillees = Duration.ZERO;
        this.totalHeuresSupplementaires = Duration.ZERO;
        this.nombreRetards = 0;
        this.tauxPresence = BigDecimal.ZERO;
        this.nombreJoursAbsence = 0;
    }
    public String toString() {
        return "RapportTemps{" +
                "id=" + id +
                ", periode='" + periode + '\'' +
                ", totalHeuresTravaillees=" + totalHeuresTravaillees.toHours() +
                ", totalHeuresSupplementaires=" + totalHeuresSupplementaires.toHours() +
                ", nombreRetards=" + nombreRetards +
                ", tauxPresence=" + tauxPresence +
                ", nombreJoursAbsence=" + nombreJoursAbsence +
                '}';
    }
}