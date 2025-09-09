package ma.digitalia.systemalert.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ma.digitalia.systemalert.model.enums.StatusAlerte;
import ma.digitalia.systemalert.model.enums.TypeAlerte;

import java.time.LocalDateTime;

/**
 * DTO pour les alertes avec validation renforcée
 */
@AllArgsConstructor
@Data
public class AlerteDTO {

    private Long id;

    @NotBlank(message = "Le titre ne peut pas être vide")
    @Size(min = 3, max = 255, message = "Le titre doit contenir entre 3 et 255 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s\\-_.,!?]+$", message = "Le titre contient des caractères non autorisés")
    private String titre;

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(min = 10, max = 1000, message = "Le message doit contenir entre 10 et 1000 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s\\-_.,!?\\n\\r]+$", message = "Le message contient des caractères non autorisés")
    private String message;

    @NotNull(message = "Le type d'alerte est obligatoire")
    private TypeAlerte type;

    private StatusAlerte status;

    private LocalDateTime dateCreation;

    @NotNull(message = "L'ID utilisateur est obligatoire")
    @Positive(message = "L'ID utilisateur doit être positif")
    private Long userId;

    @NotNull(message = "L'ID utilisateur est obligatoire")
    @Positive(message = "L'ID utilisateur doit être positif")
    private Long employeId;

    // Constructeurs
    public AlerteDTO() {}

    public AlerteDTO(String titre, String message, TypeAlerte type, Long userId) {
        this.titre = sanitizeInput(titre);
        this.message = sanitizeInput(message);
        this.type = type;
        this.userId = userId;
        this.status = StatusAlerte.UNREAD;
    }

    // Méthode de nettoyage des données d'entrée
    private String sanitizeInput(String input) {
        if (input == null) return null;

        // Supprime les caractères potentiellement dangereux
        return input.trim()
                .replaceAll("<script[^>]*>.*?</script>", "") // Supprime les scripts
                .replaceAll("<[^>]+>", "") // Supprime les balises HTML
                .replaceAll("[\\x00-\\x1F\\x7F]", ""); // Supprime les caractères de contrôle
    }

    // Getters et Setters avec validation
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = sanitizeInput(titre);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = sanitizeInput(message);
    }

    public TypeAlerte getType() {
        return type;
    }

    public void setType(TypeAlerte type) {
        this.type = type;
    }

    public StatusAlerte getStatus() {
        return status;
    }

    public void setStatus(StatusAlerte status) {
        this.status = status;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("L'ID utilisateur doit être positif");
        }
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "AlerteDTO{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", dateCreation=" + dateCreation +
                ", userId=" + userId +
                '}';
    }
}
