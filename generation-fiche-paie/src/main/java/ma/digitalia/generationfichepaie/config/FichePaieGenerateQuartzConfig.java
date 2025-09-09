package ma.digitalia.generationfichepaie.config;


import ma.digitalia.suividutemps.jobs.FichePaieJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FichePaieGenerateQuartzConfig {
    @Bean
    public JobDetail FichePaieGenerateJobDetail() {
        return JobBuilder.newJob(FichePaieJob.class)
                .withIdentity("fichePaieJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger FichePaieGenerateTrigger(JobDetail FichePaieGenerateJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(FichePaieGenerateJobDetail)
                .withIdentity("fichePaieTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 19 23 * ?"))
                .build();
    }
}
