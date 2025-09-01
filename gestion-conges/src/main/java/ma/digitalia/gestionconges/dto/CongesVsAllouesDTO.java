package ma.digitalia.gestionconges.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongesVsAllouesDTO {
    private String employe;
    private String departement;
    private Integer Pris;
    private Integer Alloues;
}
