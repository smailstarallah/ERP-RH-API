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
import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import ma.digitalia.systemalert.service.AlerteService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DemandeCongeServiceImpl implements DemandeCongeService {

    DemandeCongeRepository demandeCongeRepository;
    EmployeService employeService;
    ManagerService managerService;
    TypeCongeService typeCongeService;
    private final AlerteService alerteService;

    public DemandeCongeServiceImpl(DemandeCongeRepository demandeCongeRepository, EmployeService employeService, ManagerService managerService
            , TypeCongeService typeCongeService, AlerteService alerteService) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.employeService = employeService;
        this.typeCongeService = typeCongeService;
        this.managerService = managerService;
        this.alerteService = alerteService;
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
            String message = String.format("Nouvelle demande de congé de %s %s.",
                    employe.getPreNom(), employe.getNom());
            creerEtEnvoyerAlerte(employe.getManager().getId(), message, TypeAlerte.INFO);
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
    @Transactional
    public boolean validerDemandeConge(Long demandeCongeId, Long managerId, String commentaire) {
        try {
            DemandeConge demande = demandeCongeRepository.findById(demandeCongeId)
                    .orElseThrow(() -> new EntityNotFoundException("Demande non trouvée: " + demandeCongeId));

            if (!demande.getValidateur().getId().equals(managerId)) {
                log.warn("Tentative de validation non autorisée par le manager {} pour la demande {}", managerId, demandeCongeId);
                // Optionnel : Alerte de sécurité pour l'admin
//                creerEtEnvoyerAlerte(ADMIN_USER_ID, "Tentative de validation non autorisée par le manager " + managerId, TypeAlerte.WARNING);
                return false;
            }

            demande.setStatut(StatutDemande.VALIDEE);
            if (commentaire != null && !commentaire.isEmpty()) {
                demande.setCommentaire(commentaire);
            }
            demandeCongeRepository.save(demande);

            String message = String.format("Votre demande de congé du %s a été VALIDÉE.",
                    demande.getDateDebut().format(DateTimeFormatter.ISO_LOCAL_DATE));
            creerEtEnvoyerAlerte(demande.getDemandeur().getId(), message, TypeAlerte.INFO);

            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la validation de la demande {}", demandeCongeId, e);
//            creerEtEnvoyerAlerte(ADMIN_USER_ID, "Erreur inattendue dans validerDemandeConge: " + e.getMessage(), TypeAlerte.ERROR);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean refuserDemandeConge(Long demandeCongeId, Long managerId, String commentaire) {
        try {
            DemandeConge demande = demandeCongeRepository.findById(demandeCongeId)
                    .orElseThrow(() -> new EntityNotFoundException("Demande non trouvée: " + demandeCongeId));

            if (!demande.getValidateur().getId().equals(managerId)) {
                log.warn("Tentative de refus non autorisée par le manager {} pour la demande {}", managerId, demandeCongeId);
                return false;
            }

            demande.setStatut(StatutDemande.REJETEE);
            if (commentaire != null && !commentaire.isEmpty()) {
                demande.setCommentaire(commentaire);
            }
            demandeCongeRepository.save(demande);

            // --- NOTIFICATION À L'EMPLOYÉ ---
            String message = String.format("Votre demande de congé du %s a été REJETÉE.",
                    demande.getDateDebut().format(DateTimeFormatter.ISO_LOCAL_DATE));
            creerEtEnvoyerAlerte(demande.getDemandeur().getId(), message, TypeAlerte.WARNING); // WARNING car action négative

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    private void creerEtEnvoyerAlerte(Long userId, String message, TypeAlerte type) {
    try {
        AlerteDTO alerteDTO = new AlerteDTO();
        alerteDTO.setUserId(userId);
        alerteDTO.setMessage(message);
        alerteDTO.setType(type);
        alerteDTO.setTitre("Notification de congé");
        alerteService.creerAlerte(alerteDTO);
    } catch (Exception e) {
        log.error("Impossible de créer et d'envoyer l'alerte pour l'utilisateur {} : {}", userId, e.getMessage());
    }
}
}