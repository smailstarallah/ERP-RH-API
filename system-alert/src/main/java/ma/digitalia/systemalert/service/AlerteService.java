package ma.digitalia.systemalert.service;

import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.enums.StatusAlerte;
import ma.digitalia.systemalert.model.enums.TypeAlerte;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface pour le service de gestion des alertes
 */
public interface AlerteService {

    /**
     * Crée une nouvelle alerte
     */
    AlerteDTO creerAlerte(AlerteDTO alerteDTO);

    /**
     * Récupère toutes les alertes d'un employé
     */
    List<AlerteDTO> getAlertesParEmploye(Long employeId);

    /**
     * Récupère les alertes d'un employé par statut
     */
    List<AlerteDTO> getAlertesParEmployeEtStatut(Long employeId, StatusAlerte status);

    /**
     * Marque une alerte comme lue
     */
    AlerteDTO marquerCommeLue(Long alerteId);

    /**
     * Supprime une alerte
     */
    void supprimerAlerte(Long alerteId);

    /**
     * Récupère une alerte par son ID
     */
    AlerteDTO getAlerteParId(Long alerteId);

    /**
     * Récupère les alertes par type
     */
    List<AlerteDTO> getAlertesParType(TypeAlerte type);

    /**
     * Compte les alertes non lues d'un employé
     */
    long compterAlertesNonLues(Long employeId);

    /**
     * Récupère les alertes récentes d'un employé (dernières 24h)
     */
    List<AlerteDTO> getAlertesRecentes(Long employeId);

    /**
     * Supprime les alertes anciennes
     */
    void supprimerAlertesAnciennes(LocalDateTime dateLimit);

    /**
     * Récupère toutes les alertes non lues du système
     */
    List<AlerteDTO> getToutesAlertesNonLues();
}
