package ma.digitalia.gestionutilisateur.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends Users {

    @Column(nullable = false)
    private String department;

    @JsonIgnoreProperties
    @Column(nullable = false)
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Employe> employes;

    public String toString() {
        return "Manager{" +
                "Id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", preNom='" + getPreNom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", telephone='" + getTelephone() + '\'' +
                ", dateNaissance=" + getDateNaissance() +
                ", department='" + department + '\'' +
                '}';
    }
}
