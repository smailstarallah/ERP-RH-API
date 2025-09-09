package ma.digitalia.suividutemps.jobs;

import lombok.extern.slf4j.Slf4j;
import ma.digitalia.generationfichepaie.services.GenerationFichePaieService;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.services.EmployeService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
public class FichePaieJob implements Job {

    private final GenerationFichePaieService generationFichePaieService;
    private final EmployeService employeService;

    public FichePaieJob(GenerationFichePaieService generationFichePaieService, EmployeService employeService) {
        this.generationFichePaieService = generationFichePaieService;
        this.employeService = employeService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        try {
            List<Employe> employes = employeService.findAll();
            YearMonth month = YearMonth.now();
            for (Employe employe: employes) {
                generationFichePaieService.genererFichePaie(employe.getId(), month);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution du job RapportMensuelJob", e);
        }
    }
}
