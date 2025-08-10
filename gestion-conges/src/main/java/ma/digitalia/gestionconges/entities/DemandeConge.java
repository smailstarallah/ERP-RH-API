package ma.digitalia.gestionconges.entities;

import lombok.Data;
import ma.digitalia.gestionconges.Enum.StatutDemande;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.entities.Users;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "demande_conge")
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @NotNull(message = "Le nombre de jours est obligatoire")
    @Min(value = 1, message = "Le nombre de jours doit être supérieur à 0")
    @Column(name = "nombre_jours", nullable = false)
    private Integer nombreJours;

    @Column(length = 1000)
    private String motif;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @Column(length = 500)
    private String commentaire;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Employe demandeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id")
    private Manager validateur;

    public DemandeConge(LocalDate dateDebut, LocalDate dateFin, String motif, TypeConge typeConge
            , Employe demandeur, Manager validateur) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.motif = motif;
        this.typeConge = typeConge;
        this.demandeur = demandeur;
        this.validateur = validateur;
        this.statut = StatutDemande.EN_ATTENTE;
        this.nombreJours = calculeJoursOuvrables(dateDebut, dateFin);
    }

    private int calculeJoursOuvrables(LocalDate startDate, LocalDate endDate) {
        int nombreJours = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                nombreJours++;
            }
            currentDate = currentDate.plusDays(1);
        }
        return nombreJours;
    }
}