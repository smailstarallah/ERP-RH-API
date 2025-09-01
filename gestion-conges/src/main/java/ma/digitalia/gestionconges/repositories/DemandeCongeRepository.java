package ma.digitalia.gestionconges.repositories;


import ma.digitalia.gestionconges.Enum.StatutDemande;
import ma.digitalia.gestionconges.entities.DemandeConge;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

    /**
     * Update the status of a leave request.
     * @param id the ID of the leave request to update
     * @param statut the new status to set
     */
    @Modifying
    @Query("UPDATE DemandeConge d SET d.statut = :statut WHERE d.id = :id")
    void updateStatutConge(@Param("id") Long id, @Param("statut") StatutDemande statut);

    /**
     * Update the comment of a leave request.
     * @param id
     * @param commentaire
     */
    @Modifying
    @Query("UPDATE DemandeConge d SET d.commentaire = :commentaire WHERE d.id = :id")
    void updateCommentaireDemandeConge(@Param("id") Long id, @Param("commentaire") String commentaire);

    List<DemandeConge> findDemandeCongeByDemandeur(Employe demandeur);


    List<DemandeConge> findDemandeCongeByValidateur(Manager validateur);

    @Query("SELECT d FROM DemandeConge d WHERE d.validateur = :validateur AND d.statut <> :statut")
    List<DemandeConge> findDemandeCongeByValidateurAndStatut(Manager validateur, StatutDemande statut);

    List<DemandeConge> findByStatut(StatutDemande statut);
}
