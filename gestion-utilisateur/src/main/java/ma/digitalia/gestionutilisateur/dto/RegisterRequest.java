package ma.digitalia.gestionutilisateur.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ma.digitalia.gestionutilisateur.Enum.UserType;

@Data
public class RegisterRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String preNom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @Pattern(
            regexp = "^(\\+212|0)([5-6-7])\\d{8}$",
            message = "Le numéro de téléphone doit être un numéro marocain valide"
    )
    private String telephone;

    @NotBlank(message = "La date de naissance est obligatoire")
    private String dateNaissance;

    @NotNull(message = "Le type d'utilisateur est obligatoire")
    private UserType userType;

    // Champs spécifiques selon le type
    private String numeroEmploye; // Pour EMPLOYE
    private String cin;
    private String poste;

    private String departement; // Pour MANAGER
    private String niveau; // Pour RH
}
