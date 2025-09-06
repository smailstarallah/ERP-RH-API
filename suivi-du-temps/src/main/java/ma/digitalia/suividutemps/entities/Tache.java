package ma.digitalia.suividutemps.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ma.digitalia.gestionutilisateur.entities.Users;
import ma.digitalia.suividutemps.Enum.Priority;
import ma.digitalia.suividutemps.Enum.TacheStatut;

@Entity
@Data
@NoArgsConstructor
@ToString(exclude = {"ajoutePar", "projet"})
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private Double estimationHeures;
    private String description;
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private TacheStatut statut;

    @ManyToOne
    @JoinColumn(name = "ajoute_par_id")
    @JsonIgnore
    private Users ajoutePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "projet_id")
    private Projet projet;

    public boolean isTerminee() {
        return this.statut == TacheStatut.TERMINEE;
    }
}