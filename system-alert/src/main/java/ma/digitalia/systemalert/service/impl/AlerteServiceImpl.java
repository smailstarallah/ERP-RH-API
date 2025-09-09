package ma.digitalia.systemalert.service.impl;

import ma.digitalia.systemalert.event.AlerteCreatedEvent;
import ma.digitalia.systemalert.event.AlerteDeletedEvent;
import ma.digitalia.systemalert.event.AlerteUpdatedEvent;
import ma.digitalia.systemalert.exception.AlerteNotFoundException;
import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.entity.Alerte;
import ma.digitalia.systemalert.model.enums.StatusAlerte;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import ma.digitalia.systemalert.model.mapper.AlerteMapper;
import ma.digitalia.systemalert.repository.AlerteRepository;
import ma.digitalia.systemalert.service.AlerteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de gestion des alertes avec événements automatiques
 */
@Service
public class AlerteServiceImpl implements AlerteService {

    private static final Logger logger = LoggerFactory.getLogger(AlerteServiceImpl.class);

    private final AlerteRepository alerteRepository;
    private final AlerteMapper alerteMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public AlerteServiceImpl(AlerteRepository alerteRepository,
                           AlerteMapper alerteMapper,
                           SimpMessagingTemplate messagingTemplate,
                           ApplicationEventPublisher eventPublisher) {
        this.alerteRepository = alerteRepository;
        this.alerteMapper = alerteMapper;
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public AlerteDTO creerAlerte(AlerteDTO alerteDTO) {
        logger.info("💾 Création d'une nouvelle alerte pour l'utilisateur {}", alerteDTO.getUserId());

        // Conversion DTO vers entité
        Alerte alerte = alerteMapper.toEntity(alerteDTO);
        alerte.setStatus(StatusAlerte.UNREAD);

        // Sauvegarde en base de données
        Alerte alerteSauvegardee = alerteRepository.save(alerte);
        AlerteDTO alerteDTOSauvegardee = alerteMapper.toDTO(alerteSauvegardee);

        logger.info("✅ Alerte sauvegardée en DB avec l'ID {}", alerteSauvegardee.getId());

        // 🚨 DÉCLENCHEMENT AUTOMATIQUE DE L'ÉVÉNEMENT
        // Ceci va automatiquement notifier tous les clients WebSocket connectés
        try {
            AlerteCreatedEvent event = new AlerteCreatedEvent(alerteDTOSauvegardee, "SERVICE");
            eventPublisher.publishEvent(event);
            logger.info("📡 Événement AlerteCreatedEvent publié pour l'alerte ID: {}", alerteDTOSauvegardee.getId());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la publication de l'événement:", e);
        }

        return alerteDTOSauvegardee;
    }

    /**
     * Vérifie les permissions avant de récupérer les alertes
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlerteDTO> getAlertesParEmploye(Long employeId) {
        logger.debug("Récupération des alertes pour l'employé {}", employeId);
        List<Alerte> alertes = alerteRepository.findByUserIdOrderByDateCreationDesc(employeId);
        return alerteMapper.toDTOList(alertes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteDTO> getAlertesParEmployeEtStatut(Long employeId, StatusAlerte status) {
        logger.debug("Récupération des alertes {} pour l'employé {}", status, employeId);
        List<Alerte> alertes = alerteRepository.findByUserIdAndStatus(employeId, status);
        return alerteMapper.toDTOList(alertes);
    }

    @Override
    public AlerteDTO marquerCommeLue(Long alerteId) {
        logger.info("Marquage de l'alerte {} comme lue", alerteId);

        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new AlerteNotFoundException(alerteId));

        alerte.marquerCommeLue();
        Alerte alerteMiseAJour = alerteRepository.save(alerte);
        AlerteDTO alerteDTO = alerteMapper.toDTO(alerteMiseAJour);
        eventPublisher.publishEvent(new AlerteUpdatedEvent(this, alerteDTO));

        logger.info("Alerte {} marquée comme lue avec succès", alerteId);
        return alerteMapper.toDTO(alerteMiseAJour);
    }

    /**
     * Sécurisation de la suppression d'alertes
     */
    @Override
    public void supprimerAlerte(Long alerteId) {
        logger.info("Suppression de l'alerte {}", alerteId);

        Alerte alerte = alerteRepository.findById(alerteId).get();
        Long userId = alerte.getUserId();

        if (!alerteRepository.existsById(alerteId)) {
            throw new AlerteNotFoundException(alerteId);
        }

        alerteRepository.deleteById(alerteId);
        logger.info("Alerte {} supprimée avec succès", alerteId);
        eventPublisher.publishEvent(new AlerteDeletedEvent(this, alerteId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public AlerteDTO getAlerteParId(Long alerteId) {
        logger.debug("Récupération de l'alerte {}", alerteId);

        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new AlerteNotFoundException(alerteId));

        return alerteMapper.toDTO(alerte);
    }

    /**
     * Filtrage sécurisé des alertes par type
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlerteDTO> getAlertesParType(TypeAlerte type) {
        logger.debug("Récupération des alertes de type {}", type);
        List<Alerte> alertes = alerteRepository.findByType(type);
        return alerteMapper.toDTOList(alertes);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterAlertesNonLues(Long employeId) {
        logger.debug("Comptage des alertes non lues pour l'employé {}", employeId);
        return alerteRepository.countByUserIdAndStatus(employeId, StatusAlerte.UNREAD);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteDTO> getAlertesRecentes(Long employeId) {
        logger.debug("Récupération des alertes récentes pour l'employé {}", employeId);
        LocalDateTime depuis = LocalDateTime.now().minusHours(24);
        List<Alerte> alertes = alerteRepository.findAlertesRecentes(employeId, depuis);
        return alerteMapper.toDTOList(alertes);
    }

    @Override
    public void supprimerAlertesAnciennes(LocalDateTime dateLimit) {
        logger.info("Suppression des alertes anciennes avant {}", dateLimit);
        alerteRepository.deleteByDateCreationBefore(dateLimit);
        logger.info("Alertes anciennes supprimées avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteDTO> getToutesAlertesNonLues() {
        logger.debug("Récupération de toutes les alertes non lues");
        List<Alerte> alertes = alerteRepository.findByStatusOrderByDateCreationDesc(StatusAlerte.UNREAD);
        return alerteMapper.toDTOList(alertes);
    }

    /**
     * Envoie une notification WebSocket pour une nouvelle alerte
     */
    private void envoyerNotificationWebSocket(AlerteDTO alerteDTO) {
        try {
            // Notification individuelle à l'employé
            messagingTemplate.convertAndSend(
                "/topic/alertes/employe/" + alerteDTO.getUserId(),
                alerteDTO
            );

            // Notification globale pour les managers/RH selon le type
            if (alerteDTO.getType() == TypeAlerte.ERROR || alerteDTO.getType() == TypeAlerte.WARNING) {
                messagingTemplate.convertAndSend("/topic/alertes/global", alerteDTO);
            }

            logger.debug("Notification WebSocket envoyée pour l'alerte {}", alerteDTO.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification WebSocket", e);
        }
    }
}
