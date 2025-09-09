package ma.digitalia.systemalert.model.entity;

import jakarta.persistence.*;
import ma.digitalia.systemalert.model.enums.StatusAlerte;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité JPA pour les alertes système
 */
@Entity
@Table(name = "alertes")
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAlerte status = StatusAlerte.UNREAD;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Constructeurs
    public Alerte() {}

    public Alerte(String titre, String message, TypeAlerte type, Long userId) {
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.status = StatusAlerte.UNREAD;
    }

    // Getters et Setters
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
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        this.userId = userId;
    }

    /**
     * Marque l'alerte comme lue
     */
    public void marquerCommeLue() {
        this.status = StatusAlerte.READ;
    }

    @Override
    public String toString() {
        return "Alerte{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", dateCreation=" + dateCreation +
                ", userId=" + userId +
                '}';
    }
}
