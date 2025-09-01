package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // AJOUTS pour résoudre le problème STANDBY
        factory.setAutoStartup(true);           // Démarrage automatique
        factory.setStartupDelay(5);             // Délai de 5 secondes
        factory.setOverwriteExistingJobs(true); // Écraser les jobs existants
        factory.setWaitForJobsToCompleteOnShutdown(true);

        return factory;
    }
}