package ma.digitalia.gestionconges.controllers;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionconges.dto.CreateDemandeCongeRequest;
import ma.digitalia.gestionconges.dto.ValidationDemande;
import ma.digitalia.gestionconges.entities.TypeConge;
import ma.digitalia.gestionconges.services.DemandeCongeService;
import ma.digitalia.gestionconges.services.DemandeCongeServiceImpl;
import ma.digitalia.gestionconges.services.SoldeCongeService;
import ma.digitalia.gestionconges.services.TypeCongeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/gestion-conge")
public class DemandeCongeControllers {

    private final DemandeCongeService demandeCongeService;
    private final TypeCongeService typeCongeService;
    private final SoldeCongeService soldeCongeService;

    public DemandeCongeControllers(DemandeCongeServiceImpl demandeCongeService, TypeCongeService typeCongeService,
                                   SoldeCongeService soldeCongeService) {
        this.demandeCongeService = demandeCongeService;
        this.typeCongeService = typeCongeService;
        this.soldeCongeService = soldeCongeService;
    }

    /**
     * Endpoint to send a leave request for an employee.
     * @param employeId The ID of the employee for whom the leave request is being made.
     * @param demandeCongeRequest The request body containing the details of the leave request.
     * @return A response entity indicating the success of the operation.
     */
    @Transactional
    @PostMapping("/demande/{employeId}")
    @PreAuthorize("hasAnyRole('EMPLOYE')")
    public ResponseEntity<String> envoyerDemandeConge(
            @PathVariable Long employeId,
            @RequestBody @Valid CreateDemandeCongeRequest demandeCongeRequest) {

        try {
            demandeCongeService.envoyerDemandeConge(employeId, demandeCongeRequest);
            return ResponseEntity.ok("Demande de congé envoyée avec succès");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Pour voir l'erreur complète
            return ResponseEntity.internalServerError().body("Erreur interne du serveur: " + e.getMessage());
        }
    }

    @GetMapping("/list-type-conge")
    public ResponseEntity<List<Map<Long, String>>> getTypeCongeListMap() {
        try {
            List<Map<Long, String>> result = typeCongeService.getTypeConges();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list-all-type-conge")
    public ResponseEntity<List<TypeConge>> getTypeConge() {
        try {
            List<TypeConge> result = typeCongeService.getAllTypeConges();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint to get the leave request of an employee.
     * @param employeId The ID of the employee whose leave request is being retrieved.
     * @return A response entity containing the leave request details.
     */
    @GetMapping("/{employeId}")
    public ResponseEntity<?> getDemandeConge(@PathVariable Long employeId) {
        try {
            return ResponseEntity.ok(demandeCongeService.getDemandeCongeByEmploye(employeId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }

    @GetMapping("/list-sold-conge/{employeId}")
    @PreAuthorize("hasAnyRole('EMPLOYE')")
    public ResponseEntity<?> getSoldCongeList(@PathVariable Long employeId) {
        try {
            return ResponseEntity.ok(soldeCongeService.recupererSoldeCongesRestant(employeId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }

    @GetMapping("/list-demande-conge/{managerId}")
    public ResponseEntity<?> getDemandeByManager(@PathVariable Long managerId) {
        try {
            return ResponseEntity.ok(demandeCongeService.getDemandeCongeByManager(managerId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }

    @PutMapping("/validation-demande/{managerId}")
    @Transactional
    @PreAuthorize(value = "hasAnyRole('MANAGER', 'RH')")
    public ResponseEntity<String> validationDemandeConge(
            @PathVariable Long managerId,
            @RequestBody @Valid ValidationDemande validationDemande
            ) {
        try {
            log.info("Validation de la demande de congé: {}", validationDemande);
            if (Objects.equals(validationDemande.getDecision(), "APPROUVE")) {
                demandeCongeService.validerDemandeConge(validationDemande.getIdDemande(), managerId, validationDemande.getCommentaire());
            } else if (Objects.equals(validationDemande.getDecision(), "REJETE")) {
                demandeCongeService.refuserDemandeConge(validationDemande.getIdDemande(), managerId, validationDemande.getCommentaire());
            } else {
                log.error("La décision doit être 'Accepter' ou 'Refuser'");
                return ResponseEntity.badRequest().body("La décision doit être 'Accepter' ou 'Refuser'");
            }
            return ResponseEntity.ok("Demande de congé traitée avec succès");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur interne du serveur: " + e.getMessage());
        }
    }

    @PostMapping("/save-type-conge")
    @PreAuthorize("hasRole('RH')")
    public ResponseEntity<?> saveTypeConges(
            @RequestBody @Valid TypeConge typeConge
    ) {
        try {
            log.info("Type de cong<UNK>: {}", typeConge);
            typeCongeService.save(typeConge);
            log.info("TypeConge sauvegardé avec succées: {}", typeConge);
            return ResponseEntity.ok("save with success");
        } catch (Exception e) {
            log.error("Erreur interne du serveur: " + e.getMessage());
            return  ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }

    @DeleteMapping("/delete-type-conge/{id}")
    @PreAuthorize("hasRole('RH')")
    public ResponseEntity<?> deleteTypeConge(@PathVariable Long id) {
        try {
            log.info("Suppression du type de congé avec l'ID: {}", id);
            typeCongeService.deleteById(id);
            log.info("Type de congé supprimé avec succès");
            return ResponseEntity.ok("Type de congé supprimé avec succès");
        } catch (EntityNotFoundException e) {
            log.error("Type de congé non trouvé: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erreur interne du serveur: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }
}
