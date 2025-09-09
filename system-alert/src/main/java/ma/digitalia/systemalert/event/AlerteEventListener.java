package ma.digitalia.systemalert.event;

import ma.digitalia.systemalert.model.dto.AlerteDTO;
import ma.digitalia.systemalert.model.enums.TypeAlerte;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;



@Component
public class AlerteEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AlerteEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;

    public AlerteEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlerteCreated(AlerteCreatedEvent event) {
        try {
            AlerteDTO alerteDTO = event.getAlerte(); // Correction ici

            // Notification WebSocket individuelle
            messagingTemplate.convertAndSend(
                    "/topic/alertes/employe/" + alerteDTO.getUserId(),
                    alerteDTO
            );

            // Notification globale si nécessaire
            if (alerteDTO.getType() == TypeAlerte.ERROR || alerteDTO.getType() == TypeAlerte.WARNING) {
                messagingTemplate.convertAndSend("/topic/alertes/global", alerteDTO);
            }

            logger.info("📡 Notification WebSocket envoyée pour l'alerte ID: {}", alerteDTO.getId());        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de notification WebSocket", e);
        }
    }

}
