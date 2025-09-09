package ma.digitalia.gestionconges.services;

import ma.digitalia.gestionconges.dto.CongesPrisDTO;
import ma.digitalia.gestionutilisateur.entities.Employe;

import java.time.LocalDate;
import java.util.List;

public interface CongesPrisService {

    /**
     * Récupérer tous les congés pris (validés) pour l'année courante
     */
    List<CongesPrisDTO> getAllCongesPris(Long userId);

    /**
     * Récupérer les congés pris pour une année spécifique
     */
    List<CongesPrisDTO> getCongesPrisByAnnee(int annee);

    /**
     * Récupérer les congés pris par un employé spécifique
     */
    List<CongesPrisDTO> getCongesPrisByEmploye(Long employeId);

    /**
     * Récupérer les congés pris par département
     */
    List<CongesPrisDTO> getCongesPrisByDepartement(String departement);

    /**
     * Récupérer les congés pris dans une période donnée
     */
    List<CongesPrisDTO> getCongesPrisByPeriode(LocalDate dateDebut, LocalDate dateFin);

    /**
     * Récupérer les congés pris par type
     */
    List<CongesPrisDTO> getCongesPrisByType(String typeConge);

    /**
     * Calculer le total des jours de congés pris par employé
     */
    int getTotalJoursCongesPris(Long employeId, int annee);

    /**
     * Calculer le total des jours de congés pris par département
     */
    int getTotalJoursCongesPrisByDepartement(String departement, int annee);
}
