package ma.digitalia.generationfichepaie.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.digitalia.generationfichepaie.Enum.ModeCalcul;
import ma.digitalia.generationfichepaie.Enum.TypeElement;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjoutElementPaieDTO {
    @NotNull
    private TypeElement type;
    @Size(max = 50)
    private String sousType;
    @Size(max = 100)
    private String libelle;
    @NotNull
    private ModeCalcul modeCalcul;
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal montant;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal taux;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal base;
    @Size(max = 255)
    private String description;
    private boolean soumisIR = true;
    private boolean soumisCNSS = true;
    private String uniteCalcul;
}
