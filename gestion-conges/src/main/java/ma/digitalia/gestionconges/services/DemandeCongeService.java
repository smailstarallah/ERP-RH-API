package ma.digitalia.gestionconges.services;

import ma.digitalia.gestionconges.dto.CreateDemandeCongeRequest;
import ma.digitalia.gestionconges.dto.ValidationDemandeConge;
import ma.digitalia.gestionconges.entities.DemandeConge;

import java.util.List;

public interface DemandeCongeService {
    /**
     * Envoie une demande de congé.
     *
     * @param employeId l'identifiant de l'employé
     * @param demandeCongeRequest les détails de la demande de congé
     * @return true si la demande a été envoyée avec succès, false sinon
     */
    boolean envoyerDemandeConge(Long employeId, CreateDemandeCongeRequest demandeCongeRequest);

    /**
     * Annule une demande de congé.
     *
     * @param demandeCongeId l'identifiant de la demande de congé
     * @return true si la demande a été annulée avec succès, false sinon
     */
    boolean annulerDemandeConge(Long demandeCongeId);


    /**
     * Valide une demande de congé.
     *
     * @param demandeCongeId l'identifiant de la demande de congé
     * @param managerId l'identifiant du manager qui valide la demande
     * @return true si la demande a été validée avec succès, false sinon
     */
    boolean validerDemandeConge(Long demandeCongeId, Long managerId, String commentaire);

    /**
     * Refuse une demande de congé.
     *
     * @param demandeCongeId l'identifiant de la demande de congé
     * @param managerId l'identifiant du manager qui refuse la demande
     * @return true si la demande a été refusée avec succès, false sinon
     */
    boolean refuserDemandeConge(Long demandeCongeId, Long managerId, String commentaire);

    List<DemandeConge> getDemandeCongeByEmploye(Long employeId);

    /**
     * Récupère les demandes de congé d'un manager.
     *
     * @param managerId l'identifiant du manager
     * @return la liste des demandes de congé associées au manager
     */
    List<ValidationDemandeConge> getDemandeCongeByManager(Long managerId);
}
