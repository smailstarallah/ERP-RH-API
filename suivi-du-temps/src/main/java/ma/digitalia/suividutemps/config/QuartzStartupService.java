package ma.digitalia.suividutemps.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuartzStartupService implements CommandLineRunner {

    @Autowired
    private Scheduler scheduler;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("🔍 Vérification du statut du scheduler Quartz...");

            if (scheduler.isInStandbyMode()) {
                log.warn("⚠️ Scheduler en mode STANDBY, démarrage en cours...");
                scheduler.start();
                log.info("✅ Scheduler Quartz démarré avec succès");
            } else if (scheduler.isStarted()) {
                log.info("ℹ️ Scheduler Quartz déjà démarré");
            } else {
                log.warn("⚠️ Scheduler dans un état inconnu, tentative de démarrage...");
                scheduler.start();
                log.info("✅ Scheduler Quartz démarré");
            }

            // Affichage des informations de debug
            logSchedulerInfo();

        } catch (SchedulerException e) {
            log.error("❌ Erreur lors du démarrage du scheduler Quartz", e);
            throw new RuntimeException("Impossible de démarrer le scheduler Quartz", e);
        }
    }

    private void logSchedulerInfo() {
        try {
            log.info("📊 Informations du scheduler:");
            log.info("   - Nom: {}", scheduler.getSchedulerName());
            log.info("   - Instance ID: {}", scheduler.getSchedulerInstanceId());
            log.info("   - Statut: {}", getSchedulerStatus());
            log.info("   - Nombre de jobs: {}", scheduler.getJobKeys(null).size());
            log.info("   - Nombre de triggers: {}", scheduler.getTriggerKeys(null).size());
        } catch (SchedulerException e) {
            log.warn("⚠️ Impossible d'obtenir les informations du scheduler", e);
        }
    }

    private String getSchedulerStatus() {
        try {
            if (scheduler.isStarted()) return "STARTED";
            if (scheduler.isShutdown()) return "SHUTDOWN";
            if (scheduler.isInStandbyMode()) return "STANDBY";
            return "UNKNOWN";
        } catch (SchedulerException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}