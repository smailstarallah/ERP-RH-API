package ma.digitalia.suividutemps.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String client;

    @Column(length = 2000)
    private String description;

    // Dates
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateFinReelle;

    private Double budget;


    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tache> taches;

    private LocalDateTime dateCreation;
    private LocalDateTime derniereModification;

    @PrePersist
    public void prePersist() {
        dateCreation = LocalDateTime.now();
        derniereModification = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        derniereModification = LocalDateTime.now();
    }
}
