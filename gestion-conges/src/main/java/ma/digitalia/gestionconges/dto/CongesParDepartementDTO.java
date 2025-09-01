package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongesParDepartementDTO {
    private String departement;
    private Integer Annuel;
    private Integer Maladie;
    private Integer Exceptionnel;
}
