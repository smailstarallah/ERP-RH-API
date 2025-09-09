package ma.digitalia.systemalert.service;

import ma.digitalia.systemalert.service.AlerteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service de nettoyage automatique des alertes anciennes
 */
@Service
@EnableScheduling
public class AlerteCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(AlerteCleanupService.class);

    private final AlerteService alerteService;

    @Autowired
    public AlerteCleanupService(AlerteService alerteService) {
        this.alerteService = alerteService;
    }

    /**
     * Nettoie automatiquement les alertes de plus de 30 jours
     * Exécuté tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void nettoyerAlertesAnciennes() {
        logger.info("Début du nettoyage automatique des alertes anciennes");

        try {
            LocalDateTime dateLimit = LocalDateTime.now().minusDays(30);
            alerteService.supprimerAlertesAnciennes(dateLimit);
            logger.info("Nettoyage automatique des alertes terminé avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage automatique des alertes", e);
        }
    }

    /**
     * Nettoie manuellement les alertes anciennes
     */
    public void nettoyerManuellement(int nombreDeJours) {
        logger.info("Début du nettoyage manuel des alertes de plus de {} jours", nombreDeJours);

        LocalDateTime dateLimit = LocalDateTime.now().minusDays(nombreDeJours);
        alerteService.supprimerAlertesAnciennes(dateLimit);

        logger.info("Nettoyage manuel terminé avec succès");
    }
}
