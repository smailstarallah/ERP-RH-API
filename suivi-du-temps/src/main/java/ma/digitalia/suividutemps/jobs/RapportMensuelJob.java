package ma.digitalia.suividutemps.jobs;

import lombok.extern.slf4j.Slf4j;
import ma.digitalia.suividutemps.services.RapportTempsService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.Month;

@Slf4j
@Component
public class RapportMensuelJob implements Job {

    private final RapportTempsService rapportTempsService;

    public RapportMensuelJob(RapportTempsService rapportTempsService) {
        this.rapportTempsService = rapportTempsService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        rapportTempsService.generateMonthlyReport(2L, Month.AUGUST);
    }
}
