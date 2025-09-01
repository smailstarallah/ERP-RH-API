package ma.digitalia.gestionutilisateur.dto;


import lombok.Data;
import ma.digitalia.gestionutilisateur.entities.Employe;
import java.util.List;

@Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class DepartmentEmployeesDTO {
    private String departement;
    private List<Employe> employes;

}