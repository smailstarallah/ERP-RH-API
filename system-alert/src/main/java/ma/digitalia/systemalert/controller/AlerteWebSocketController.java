package ma.digitalia.systemalert.controller;

import ma.digitalia.systemalert.event.AlerteCreatedEvent;
import ma.digitalia.systemalert.event.AlerteDeletedEvent;
import ma.digitalia.systemalert.event.AlerteUpdatedEvent;
import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * Contrôleur WebSocket pour la diffusion des notifications en temps réel.
 * Ce contrôleur écoute les événements de l'application (création, mise à jour, suppression)
 * et envoie des messages ciblés aux clients WebSocket.
 */
@Controller
public class AlerteWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(AlerteWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public AlerteWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Écoute les événements de création d'alerte et notifie les clients.
     */
    @EventListener
    public void handleAlerteCreated(AlerteCreatedEvent event) {
        AlerteDTO alerte = event.getAlerte();
        logger.info("🚨 [CRÉATION] Événement reçu - Envoi notification pour alerte ID: {}", alerte.getId());

        try {
            // Notification personnelle à l'utilisateur concerné
            String userTopic = "/topic/alertes/employe/" + alerte.getUserId();
            messagingTemplate.convertAndSend(userTopic, alerte);
            logger.info("✅ Notification personnelle envoyée à l'utilisateur {} sur {}", alerte.getUserId(), userTopic);

            // Notification globale pour les managers
            if (alerte.getType() == TypeAlerte.WARNING || alerte.getType() == TypeAlerte.ERROR) {
                String globalTopic = "/topic/alertes/global";
                messagingTemplate.convertAndSend(globalTopic, alerte);
                logger.info("✅ Notification globale envoyée sur {} pour alerte de type {}", globalTopic, alerte.getType());
            }

            // Mise à jour des statistiques pour les dashboards
            sendStatsUpdate(alerte.getUserId());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi des notifications de CRÉATION pour l'alerte ID: {}", alerte.getId(), e);
        }
    }

    /**
     * Écoute les événements de mise à jour d'alerte (ex: marquer comme lue) et notifie les clients.
     */
    @EventListener
    public void handleAlerteUpdated(AlerteUpdatedEvent event) {
        AlerteDTO alerte = event.getAlerte();
        logger.info("🔄 [MISE À JOUR] Événement reçu - Envoi notification pour alerte ID: {}", alerte.getId());

        try {
            // Notifier l'utilisateur que son alerte a été mise à jour
            String userTopic = "/topic/alertes/employe/" + alerte.getUserId();
            messagingTemplate.convertAndSend(userTopic, alerte);
            logger.info("✅ Notification de mise à jour envoyée à l'utilisateur {} sur {}", alerte.getUserId(), userTopic);

            // Notifier également les dashboards globaux pour qu'ils se rafraîchissent
            String globalTopic = "/topic/alertes/global";
            messagingTemplate.convertAndSend(globalTopic, alerte);
            logger.info("✅ Notification de mise à jour globale envoyée sur {}", globalTopic);

            // Mise à jour des statistiques (le nombre d'alertes non lues a changé)
            sendStatsUpdate(alerte.getUserId());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi des notifications de MISE À JOUR pour l'alerte ID: {}", alerte.getId(), e);
        }
    }

    /**
     * Écoute les événements de suppression d'alerte et notifie les clients pour qu'ils retirent l'alerte de l'UI.
     */
    @EventListener
    public void handleAlerteDeleted(AlerteDeletedEvent event) {
        Long alerteId = event.getAlerteId();
        Long userId = event.getUserId();
        logger.info("🗑️ [SUPPRESSION] Événement reçu - Envoi notification pour alerte ID: {}", alerteId);

        try {
            // Créer un payload simple pour l'action de suppression
            Map<String, Object> deletePayload = Map.of("action", "DELETE", "alerteId", alerteId);

            // Notifier l'utilisateur pour qu'il retire l'alerte de sa liste
            String userTopic = "/topic/alertes/employe/" + userId;
            messagingTemplate.convertAndSend(userTopic, deletePayload);
            logger.info("✅ Notification de suppression envoyée à l'utilisateur {} sur {}", userId, userTopic);

            // Notifier les dashboards globaux
            String globalTopic = "/topic/alertes/global";
            messagingTemplate.convertAndSend(globalTopic, deletePayload);
            logger.info("✅ Notification de suppression globale envoyée sur {}", globalTopic);

            // Mise à jour des statistiques
            sendStatsUpdate(userId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi des notifications de SUPPRESSION pour l'alerte ID: {}", alerteId, e);
        }
    }

    /**
     * Envoie une mise à jour des statistiques pour les dashboards.
     */
    private void sendStatsUpdate(Long userId) {
        try {
            var statsUpdate = Map.of(
                    "type", "STATS_UPDATE",
                    "userId", userId, // Peut être utilisé pour des stats par utilisateur ou "global"
                    "timestamp", java.time.LocalDateTime.now().toString()
            );
            messagingTemplate.convertAndSend("/topic/alertes/stats", statsUpdate);
            logger.info("📊 Mise à jour des statistiques envoyée pour l'utilisateur {}", userId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi des statistiques:", e);
        }
    }
}