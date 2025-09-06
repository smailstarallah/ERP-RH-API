package ma.digitalia.suividutemps.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.suividutemps.Enum.TypeActivite;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Activite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime debut;
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    private TypeActivite type; // Utilisation de l'enum

    @Column(length = 500)
    private String description; // Champ crucial pour décrire ce que l'employé fait

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "pointage_id")
    private Pointage pointage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_id")
    private Tache tache;
}