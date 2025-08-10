package ma.digitalia.gestionconges.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionconges.Enum.StatutDemande;
import ma.digitalia.gestionconges.dto.CreateDemandeCongeRequest;
import ma.digitalia.gestionconges.dto.SoldeCongeDTO;
import ma.digitalia.gestionconges.dto.ValidationDemandeConge;
import ma.digitalia.gestionconges.entities.DemandeConge;
import ma.digitalia.gestionconges.entities.TypeConge;
import ma.digitalia.gestionconges.repositories.DemandeCongeRepository;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.services.EmployeService;
import ma.digitalia.gestionutilisateur.services.ManagerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DemandeCongeServiceImpl implements DemandeCongeService {

    DemandeCongeRepository demandeCongeRepository;
    EmployeService employeService;
    ManagerService managerService;
    TypeCongeService typeCongeService;

    public DemandeCongeServiceImpl(DemandeCongeRepository demandeCongeRepository, EmployeService employeService, ManagerService managerService, TypeCongeService typeCongeService) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.employeService = employeService;
        this.typeCongeService = typeCongeService;
        this.managerService = managerService;
    }

    @Override
    public boolean envoyerDemandeConge(Long employeId, CreateDemandeCongeRequest demandeCongeRequest) {

        try {
            Employe employe = employeService.findById(employeId);
            log.info("Employé trouvé : {}", employe);
            if (employe == null) {
                throw new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId);
            }

            TypeConge typeConge = typeCongeService.findById(demandeCongeRequest.getTypeCongeId());
            log.info("Type de congé trouvé : {}", typeConge);
            if (typeConge == null) {
                throw new EntityNotFoundException("Type de congé non trouvé avec l'ID : " + demandeCongeRequest.getTypeCongeId());
            }

            DemandeConge demandeConge = new DemandeConge(
                    demandeCongeRequest.getDateDebut(),
                    demandeCongeRequest.getDateFin(),
                    demandeCongeRequest.getMotif(),
                    typeConge, employe, employe.getManager());

            demandeCongeRepository.save(demandeConge);
            log.info("Demande de congé envoyée avec succès : {}", demandeConge);
            return true;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de la demande de congé", e);
        }
    }

    @Override
    public boolean annulerDemandeConge(Long demandeCongeId) {
        try {
            if(demandeCongeRepository.existsById(demandeCongeId)){
                demandeCongeRepository.updateStatutConge(demandeCongeId, StatutDemande.ANNULEE);
            }
            return  true;
        } catch (EntityExistsException e) {
            throw new EntityExistsException("La demande de congé n'existe pas avec l'ID : " + demandeCongeId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean validerDemandeConge(Long demandeCongeId, Long managerId, String commentaire) {
        try {
            if(demandeCongeRepository.existsById(demandeCongeId) && managerService.existsById(managerId)
                    && managerId == demandeCongeRepository.findById(demandeCongeId).get().getValidateur().getId()
            ) {
                demandeCongeRepository.updateStatutConge(demandeCongeId, StatutDemande.VALIDEE);
                if (commentaire != null && !commentaire.isEmpty()) {
                    demandeCongeRepository.updateCommentaireDemandeConge(demandeCongeId, commentaire);
                }
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean refuserDemandeConge(Long demandeCongeId, Long managerId, String commentaire) {
        try {
            if(demandeCongeRepository.existsById(demandeCongeId) && managerService.existsById(managerId)
                    && managerId == demandeCongeRepository.findById(demandeCongeId).get().getValidateur().getId()
            ) {
                demandeCongeRepository.updateStatutConge(demandeCongeId, StatutDemande.REJETEE);
                if (commentaire != null && !commentaire.isEmpty()) {
                    demandeCongeRepository.updateCommentaireDemandeConge(demandeCongeId, commentaire);
                }
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public List<DemandeConge> getDemandeCongeByEmploye(Long employeId) {

        if (employeService.existsById(employeId)) {
            return demandeCongeRepository.findDemandeCongeByDemandeur(employeService.findById(employeId));
        }
        throw new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId);
    }

    @Override
    public List<ValidationDemandeConge> getDemandeCongeByManager(Long managerId) {
        log.info("Recherche des demandes de congé pour le manager avec l'ID : " + managerId);
        Manager manager = managerService.findById(managerId);
        System.out.println("Manager trouvé: " + manager);
        if (manager != null) {
            List<DemandeConge> demandes = demandeCongeRepository.findDemandeCongeByValidateur(manager);
            System.out.println("Nombre de demandes de congé pour le manager " + managerId + ": " + demandes.size());
            return demandes.stream()
                    .map(ValidationDemandeConge::new)
                    .collect(Collectors.toList());
        } else {
            throw new EntityNotFoundException("Manager non trouvé avec l'ID : " + managerId);
        }
    }
}
