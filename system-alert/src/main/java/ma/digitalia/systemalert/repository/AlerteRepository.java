package ma.digitalia.systemalert.repository;

import ma.digitalia.systemalert.model.entity.Alerte;
import ma.digitalia.systemalert.model.enums.StatusAlerte;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository sécurisé pour la gestion des alertes
 */
@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    /**
     * Trouve les alertes par employé et statut (utilise les méthodes Spring Data)
     */
    List<Alerte> findByUserIdAndStatus(Long userId, StatusAlerte status);

    /**
     * Trouve les alertes par employé (utilise les méthodes Spring Data)
     */
    List<Alerte> findByUserIdOrderByDateCreationDesc(Long userId);

    /**
     * Trouve les alertes par type (utilise les méthodes Spring Data)
     */
    List<Alerte> findByType(TypeAlerte type);

    /**
     * Trouve les alertes par type et employé (utilise les méthodes Spring Data)
     */
    List<Alerte> findByUserIdAndType(Long userId, TypeAlerte type);

    /**
     * Compte les alertes non lues par employé (utilise les méthodes Spring Data)
     */
    long countByUserIdAndStatus(Long userId, StatusAlerte status);

    /**
     * Trouve les alertes créées après une date donnée (utilise les méthodes Spring Data)
     */
    List<Alerte> findByDateCreationAfter(LocalDateTime dateCreation);

    /**
     * Trouve les alertes récentes avec paramètres sécurisés
     */
    @Query("SELECT a FROM Alerte a WHERE a.userId = :userId AND a.dateCreation >= :depuis ORDER BY a.dateCreation DESC")
    List<Alerte> findAlertesRecentes(@Param("userId") Long userId, @Param("depuis") LocalDateTime depuis);

    /**
     * Trouve toutes les alertes par statut (utilise les méthodes Spring Data)
     */
    List<Alerte> findByStatusOrderByDateCreationDesc(StatusAlerte status);

    /**
     * Supprime les alertes anciennes de façon sécurisée
     */
    @Modifying
    @Query("DELETE FROM Alerte a WHERE a.dateCreation < :dateLimit")
    void deleteByDateCreationBefore(@Param("dateLimit") LocalDateTime dateLimit);

    /**
     * Vérifie si une alerte appartient à un utilisateur spécifique
     */
    @Query("SELECT COUNT(a) > 0 FROM Alerte a WHERE a.id = :alerteId AND a.userId = :userId")
    boolean existsByIdAndUserId(@Param("alerteId") Long alerteId, @Param("userId") Long userId);

    /**
     * Compte les alertes par type pour un utilisateur (pour les statistiques)
     */
    @Query("SELECT COUNT(a) FROM Alerte a WHERE a.userId = :userId AND a.type = :type")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") TypeAlerte type);
}
