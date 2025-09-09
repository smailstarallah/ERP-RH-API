package ma.digitalia.systemalert.controller;

import jakarta.validation.Valid;
import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.enums.StatusAlerte;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import ma.digitalia.systemalert.service.AlerteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des alertes
 */
@RestController
@RequestMapping("/api/alertes")
public class AlerteController {

    private static final Logger logger = LoggerFactory.getLogger(AlerteController.class);

    private final AlerteService alerteService;

    @Autowired
    public AlerteController(AlerteService alerteService) {
        this.alerteService = alerteService;
    }

    /**
     * Crée une nouvelle alerte.
     * Le service se chargera de publier un AlerteCreatedEvent.
     */
    @PostMapping
    public ResponseEntity<AlerteDTO> creerAlerte(@Valid @RequestBody AlerteDTO alerteDTO) {
        logger.info("Demande de création d'alerte pour l'utilisateur {}", alerteDTO.getUserId());
        AlerteDTO nouvelleAlerte = alerteService.creerAlerte(alerteDTO);
        return new ResponseEntity<>(nouvelleAlerte, HttpStatus.CREATED);
    }

    /**
     * Marque une alerte comme lue.
     * Le service se chargera de publier un AlerteUpdatedEvent.
     */
    @PatchMapping("/{alerteId}/lu")
    public ResponseEntity<AlerteDTO> marquerCommeLue(@PathVariable Long alerteId) {
        logger.info("Marquage de l'alerte {} comme lue", alerteId);
        AlerteDTO alerte = alerteService.marquerCommeLue(alerteId);
        return ResponseEntity.ok(alerte);
    }

    /**
     * Supprime une alerte.
     * Le service se chargera de publier un AlerteDeletedEvent.
     */
    @DeleteMapping("/{alerteId}")
    public ResponseEntity<Void> supprimerAlerte(@PathVariable Long alerteId) {
        logger.info("Suppression de l'alerte {}", alerteId);
        alerteService.supprimerAlerte(alerteId);
        return ResponseEntity.noContent().build();
    }

    // --- Les autres méthodes GET restent inchangées ---

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<AlerteDTO>> getAlertesParEmploye(@PathVariable Long employeId) {
        logger.debug("Récupération des alertes pour l'employé {}", employeId);
        List<AlerteDTO> alertes = alerteService.getAlertesParEmploye(employeId);
        return ResponseEntity.ok(alertes);
    }

    @GetMapping("/employe/{employeId}/statut/{status}")
    public ResponseEntity<List<AlerteDTO>> getAlertesParEmployeEtStatut(
            @PathVariable Long employeId,
            @PathVariable StatusAlerte status) {
        logger.debug("Récupération des alertes {} pour l'employé {}", status, employeId);
        List<AlerteDTO> alertes = alerteService.getAlertesParEmployeEtStatut(employeId, status);
        return ResponseEntity.ok(alertes);
    }


    /**
     * Récupère une alerte par son ID
     */
    @GetMapping("/{alerteId}")
    public ResponseEntity<AlerteDTO> getAlerteParId(@PathVariable Long alerteId) {
        logger.debug("Récupération de l'alerte {}", alerteId);
        AlerteDTO alerte = alerteService.getAlerteParId(alerteId);
        return ResponseEntity.ok(alerte);
    }

    /**
     * Récupère les alertes par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AlerteDTO>> getAlertesParType(@PathVariable TypeAlerte type) {
        logger.debug("Récupération des alertes de type {}", type);
        List<AlerteDTO> alertes = alerteService.getAlertesParType(type);
        return ResponseEntity.ok(alertes);
    }

    /**
     * Compte les alertes non lues d'un employé
     */
    @GetMapping("/employe/{employeId}/non-lues/count")
    public ResponseEntity<Long> compterAlertesNonLues(@PathVariable Long employeId) {
        logger.debug("Comptage des alertes non lues pour l'employé {}", employeId);
        long count = alerteService.compterAlertesNonLues(employeId);
        return ResponseEntity.ok(count);
    }

    /**
     * Récupère les alertes récentes d'un employé (dernières 24h)
     */
    @GetMapping("/employe/{employeId}/recentes")
    public ResponseEntity<List<AlerteDTO>> getAlertesRecentes(@PathVariable Long employeId) {
        logger.debug("Récupération des alertes récentes pour l'employé {}", employeId);
        List<AlerteDTO> alertes = alerteService.getAlertesRecentes(employeId);
        return ResponseEntity.ok(alertes);
    }

    /**
     * Récupère toutes les alertes non lues
     */
    @GetMapping("/non-lues")
    public ResponseEntity<List<AlerteDTO>> getToutesAlertesNonLues() {
        logger.debug("Récupération de toutes les alertes non lues");
        List<AlerteDTO> alertes = alerteService.getToutesAlertesNonLues();
        return ResponseEntity.ok(alertes);
    }

    // ========== WebSocket Message Mapping ==========

    /**
     * Traite les messages WebSocket pour créer une nouvelle alerte
     */
    @MessageMapping("/nouvelle-alerte")
    @SendTo("/topic/alertes/global")
    public AlerteDTO nouvelleAlerteWebSocket(AlerteDTO alerteDTO) {
        logger.info("Création d'alerte via WebSocket pour l'utilisateur {}", alerteDTO.getUserId());
        return alerteService.creerAlerte(alerteDTO);
    }

    /**
     * Traite les messages WebSocket pour marquer une alerte comme lue
     */
    @MessageMapping("/marquer-lu")
    public void marquerCommeLueWebSocket(Long alerteId) {
        logger.info("Marquage d'alerte {} comme lue via WebSocket", alerteId);
        alerteService.marquerCommeLue(alerteId);
    }
}
