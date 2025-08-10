package ma.digitalia.suividutemps.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Duration;

/**
 * Entité représentant le planning de travail hebdomadaire
 * définissant les horaires pour chaque jour de la semaine.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "planning_travail")
public class PlanningTravail {

    /**
     * Identifiant unique du planning
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Jour de la semaine concerné par ce planning
     */
    @NotNull(message = "Le jour de la semaine est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", nullable = false, length = 10)
    private DayOfWeek jourSemaine;

    /**
     * Heure de début de travail le matin
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "heure_debut_matin")
    private LocalTime heureDebutMatin;

    /**
     * Heure de fin de travail le matin
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "heure_fin_matin")
    private LocalTime heureFinMatin;

    /**
     * Heure de début de travail l'après-midi
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "heure_debut_apres_midi")
    private LocalTime heureDebutApresMidi;

    /**
     * Heure de fin de travail l'après-midi
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "heure_fin_apres_midi")
    private LocalTime heureFinApresMidi;

    /**
     * Nombre total d'heures de travail par jour
     */
    @Column(name = "heures_par_jour")
    private Duration heuresParJour;
}
