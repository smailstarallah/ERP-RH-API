package ma.digitalia.suividutemps.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.suividutemps.Enum.StatutPointage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "demande_conge")
public class Pointage {

    /**
     * Identifiant unique du pointage
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Date du pointage
     */
    @NotNull(message = "La date de pointage est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Heure d'entrée au travail
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "heure_entree")
    private LocalTime heureEntree;

    /**
     * Heure de sortie du travail
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "heure_sortie")
    private LocalTime heureSortie;

    /**
     * Heure de début de pause
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "pause_debutee")
    private LocalTime pauseDebutee;

    /**
     * Heure de fin de pause
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "pause_terminee")
    private LocalTime pauseTerminee;

    /**
     * Durée totale des heures travaillées
     * Calculée automatiquement ou saisie manuellement
     */
    @Column(name = "heures_travaillees")
    private Duration heuresTravaillees;

    /**
     * Statut du pointage
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20)
    private StatutPointage statut;

    /**
     * Commentaire ou remarque sur le pointage
     */
    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    @Column(name = "commentaire", length = 500)
    private String commentaire;
}
