package ma.digitalia.gestionconges.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDemandeCongeRequest {

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @NotNull(message = "Le type de congé est obligatoire")
    private Long typeCongeId;

    @Size(max = 1000, message = "Le motif ne peut pas dépasser 1000 caractères")
    private String motif;

    // Validation personnalisée pour s'assurer que dateFin >= dateDebut
    @AssertTrue(message = "La date de fin doit être postérieure ou égale à la date de début")
    public boolean isDateFinValid() {
        return !dateFin.isBefore(dateDebut);
    }
}