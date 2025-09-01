package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongesPrisDTO {
    private Long id;
    private String employeNom;
    private String employePrenom;
    private String departement;
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreJours;
    private String motif;
    private LocalDate dateValidation;
    private String couleurType;
}
