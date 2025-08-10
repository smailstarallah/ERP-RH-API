package ma.digitalia.gestionconges.services;

import ma.digitalia.gestionconges.dto.SoldeCongeDTO;
import ma.digitalia.gestionconges.entities.SoldeConge;

import java.util.List;

public interface SoldeCongeService {

    /**
     * Met à jour le solde de congés pour un utilisateur donné.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param nouveauSolde le nouveau solde de congés
     */
    void mettreAJourSoldeConges(Long userId, double nouveauSolde);

    /**
     * Récupère le solde de congés restant pour un utilisateur donné pour chaque type.
     *
     * @param userId l'identifiant de l'utilisateur
     * @return le solde de congés restant
     */
    List<SoldeCongeDTO> recupererSoldeCongesRestant(Long userId);
}
