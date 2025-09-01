package ma.digitalia.suividutemps.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.suividutemps.Enum.StatutPointage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pointage")
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
    private LocalDateTime heureEntree;

    /**
     * Heure de sortie du travail
     */
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "heure_sortie")
    private LocalDateTime heureSortie;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    @JsonIgnore
    private Employe employe;

    @OneToMany(mappedBy = "pointage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("debut ASC")
    private List<Activite> activites = new ArrayList<>();

    public Pointage(Employe employe) {
        this.date = LocalDate.now();
        this.heureEntree = LocalDateTime.now();
        this.employe = employe;
    }


    public Duration calculerHeuresTravaillees() {
        if (heureEntree != null && heureSortie != null) {
            heuresTravaillees = Duration.between(heureEntree, heureSortie);
            Duration tempsPause = activites.stream()
                    .filter(activite -> activite.getType() == ma.digitalia.suividutemps.Enum.TypeActivite.PAUSE)
                    .filter(activite -> activite.getDebut() != null && activite.getFin() != null)
                    .map(activite -> Duration.between(activite.getDebut(), activite.getFin()))
                    .reduce(Duration.ZERO, Duration::plus);
            heuresTravaillees = heuresTravaillees.minus(tempsPause);
        }
        if(heureEntree != null && heureSortie == null){
            heuresTravaillees = Duration.between(heureEntree, LocalDateTime.now());
            Duration tempsPause = activites.stream()
                    .filter(activite -> activite.getType() == ma.digitalia.suividutemps.Enum.TypeActivite.PAUSE)
                    .filter(activite -> activite.getDebut() != null && activite.getFin() != null)
                    .map(activite -> Duration.between(activite.getDebut(), activite.getFin()))
                    .reduce(Duration.ZERO, Duration::plus);
            heuresTravaillees = heuresTravaillees.minus(tempsPause);
        }

        return heuresTravaillees;
    }
}
