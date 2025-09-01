package ma.digitalia.generationfichepaie.entities;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.generationfichepaie.Enum.StatutPaie;
import ma.digitalia.gestionutilisateur.entities.Employe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@Table(name = "fiche_paie")
public class FichePaie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private YearMonth periode;

    private BigDecimal salaireBrut;
    private BigDecimal salaireBrutImposable;
    private BigDecimal cotisationsSalariales;
    private BigDecimal salaireNetImposable;
    private BigDecimal salaireNet;
    private BigDecimal impotSurLeRevenu;

    @Timestamp
    private LocalDateTime dateGeneration;

    @Enumerated(EnumType.STRING)
    private StatutPaie statut;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Lob
    @Column(name = "pdf_file")
    private byte[] pdfFile;

    @OneToMany(mappedBy = "fichePaie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ElementPaie> elements;

    public String toString() {
        return "FichePaie{" +
                "id=" + id +
                ", periode=" + periode +
                ", salaireBrut=" + salaireBrut +
                ", salaireBrutImposable=" + salaireBrutImposable +
                ", cotisationsSalariales=" + cotisationsSalariales +
                ", salaireNetImposable=" + salaireNetImposable +
                ", salaireNet=" + salaireNet +
                ", impotSurLeRevenu=" + impotSurLeRevenu +
                ", dateGeneration=" + dateGeneration +
                ", statut=" + statut +
                ", employe=" + employe.getId() +
                '}';
    }
}