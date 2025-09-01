package ma.digitalia.gestionutilisateur.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import ma.digitalia.gestionutilisateur.Enum.UserType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@lombok.Data
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@lombok.NoArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false)
    private String preNom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Pattern(
            regexp = "^(\\+212|0)([5-6-7])\\d{8}$",
            message = "Le numéro de téléphone doit être un numéro marocain valide"
    )
    @Column(unique = true, nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String dateNaissance;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime dateCreation;

    private LocalDateTime dernierConnexion;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    UserType userType;
}
