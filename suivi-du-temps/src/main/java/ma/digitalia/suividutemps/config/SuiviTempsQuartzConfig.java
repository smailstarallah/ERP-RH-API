package ma.digitalia.suividutemps.config;


import ma.digitalia.suividutemps.jobs.RapportMensuelJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SuiviTempsQuartzConfig {
    @Bean
    public JobDetail pointageJobDetail() {
        return JobBuilder.newJob(RapportMensuelJob.class)
                .withIdentity("rapportMensuelJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger pointageTrigger(JobDetail pointageJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(pointageJobDetail)
                .withIdentity("rapportMensuelTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 57 19 * * ?"))
                .build();
    }
}
