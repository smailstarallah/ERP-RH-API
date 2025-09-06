package ma.digitalia.suividutemps.Controllers;


import jakarta.validation.Valid;
import ma.digitalia.suividutemps.Enum.TypeActivite;
import ma.digitalia.suividutemps.dto.*;
import ma.digitalia.suividutemps.entities.Activite;
import ma.digitalia.suividutemps.entities.PlanningTravail;
import ma.digitalia.suividutemps.entities.Projet;
import ma.digitalia.suividutemps.entities.Tache;
import ma.digitalia.suividutemps.services.DashboardService;
import ma.digitalia.suividutemps.services.PlanningTravailService;
import ma.digitalia.suividutemps.services.PointageService;
import ma.digitalia.suividutemps.services.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pointages")
public class PointageController {

    private static final Logger log = LoggerFactory.getLogger(PointageController.class);
    private final PointageService pointageService;
    private final ProjectService projectService;
    private final PlanningTravailService planningTravailService;
    private final DashboardService dashboardService;


    public PointageController(PointageService pointageService, ProjectService projectService,
                             PlanningTravailService planningTravailService, DashboardService dashboardService) {
        this.pointageService = pointageService;
        this.projectService = projectService;
        this.planningTravailService = planningTravailService;
        this.dashboardService = dashboardService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPointage(@RequestBody PointageRequest pointageRequest) {
        log.info("Pointage Request: {}", pointageRequest);
        switch (pointageRequest.getTypePointage()) {
            case "ENTREE":
                pointageService.startTimeTracking(pointageRequest);
                break;
            case "SORTIE":
                pointageService.stopTimeTracking(pointageRequest);
                break;
            case "PAUSE":
                pointageService.startPauseTime(pointageRequest);
                break;
            case "FIN_PAUSE":
                pointageService.stopPauseTime(pointageRequest);
                break;
            case "REUNION":
                pointageService.startReunionTime(pointageRequest);
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid pointage type");
        }
        return ResponseEntity.ok("Pointage created successfully");
    }

    @PostMapping("/{employeId}/start-task")
    public ResponseEntity<Activite> startTask(@PathVariable Long employeId, @RequestBody StartTaskRequest request) {

        Activite activite = pointageService.demarrerOuChangerActivite(
                employeId,
                TypeActivite.TRAVAIL,
                null,
                request.tacheId(),
                request.description()
        );
        return ResponseEntity.ok(activite);
    }

    @GetMapping("/get/{employeId}/jour")
    public ResponseEntity<?> getPointage(
            @PathVariable Long employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("Recuperation de pointage journalier de employe id {} de la date {} ", employeId, date);
            return ResponseEntity.ok(pointageService.getPointagesSemaineByEmploye(employeId, date));
        } catch (Exception e) {
            log.error("Error retrieving pointage: {}", e.getMessage());
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get/{employeId}/semaine")
    public ResponseEntity<?> getPointageSemaine(
            @PathVariable Long employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("Recuperation hebdo de pointage de employe id {} de la date {} pour", employeId, date);
            return ResponseEntity.ok(pointageService.getPointagesSemaineByEmploye(employeId, date));
        } catch (Exception e) {
            log.error("Error retrieving pointage: {}", e.getMessage());
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{empId}/week")
    public ResponseEntity<?> getEfficaciteParSemaine(
            @PathVariable Long empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start) {
        try {
            log.info("Recuperation de pointage de employe id {} de la semaine commancant le {}", empId, start);
            return ResponseEntity.ok(pointageService.getEfficaciteParSemaine(empId, start));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/equipe/status/{managerId}")
    public ResponseEntity<?> getTeamStatus(
            @PathVariable Long managerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("Recuperation du status de l'equipe du manager id {} pour la date {}", managerId, date);
            return ResponseEntity.ok(pointageService.getTeamStatus(managerId, date));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/presence/{employeId}")
    public ResponseEntity<?> getPresence(
            @PathVariable Long employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("Recuperation de la présence de l'employé id {} du {} au {}", employeId, startDate, endDate);
            return ResponseEntity.ok(pointageService.getPunchsForEmployee(employeId, startDate, endDate));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/monthly-report")
    public ResponseEntity<?> getMonthlyPresenceReport(
            @RequestParam Long employeId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            log.info("Récupération du rapport mensuel de présence pour employ�� {} année {} mois {}", employeId, year, month);
            return ResponseEntity.ok(pointageService.getMonthlyPresenceReport(employeId, year, month));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/ajout/projects")
    public ResponseEntity<Projet> createProject(@RequestBody CreateProjectRequest request) {
        Projet nouveauProjet = projectService.createProject(request);
        return new ResponseEntity<>(nouveauProjet, HttpStatus.CREATED);
    }

    @PostMapping("/{projetId}/tasks")
    public ResponseEntity<Tache> createTask(@PathVariable Long projetId, @RequestBody CreateTaskRequest request) {
        Tache nouvelleTache = projectService.createTaskForProject(projetId, request);
        System.out.println("Nouvelle Tâche: " + nouvelleTache);
        return new ResponseEntity<>(nouvelleTache, HttpStatus.CREATED);
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectWithTasksDto>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjectsWithTasksDto());
    }

    @PostMapping("/change-tasks/{employeId}/{tacheId}")
    public ResponseEntity<Activite> changeCurrentTask(
            @PathVariable Long employeId,
            @PathVariable Long tacheId,
            @RequestParam(required = false) String description) {
        log.info("Changement de tâche pour l'employé id {} vers la tâche id {} : {}", employeId, tacheId, description);
        Activite activite = pointageService.demarrerOuChangerActivite(
                employeId,
                TypeActivite.TRAVAIL,
                null,
                tacheId,
                description
        );
        log.info("Activité actuelle après changement de tâche : {}", activite.getId());
        return ResponseEntity.ok(activite);
    }

    @GetMapping("/week-pointages/{employeId}")
    public ResponseEntity<PointageResponseDto> getPointagesForWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart, @PathVariable Long employeId) {
        try {
            log.info("Récupération des pointages pour la semaine commençant le {}", weekStart);
            PointageResponseDto response = pointageService.getPointagesForWeek(weekStart, employeId);
            log.info("Pointages récupérés avec succès pour la semaine du {}: {}", weekStart, response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des pointages pour la semaine: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create-planning")
    public ResponseEntity<String> createPlanning(@RequestBody @Valid List<PlanningTravail> planningRequest) {
        log.info("planning Request: {}", planningRequest);
        planningTravailService.createPlanning(planningRequest);
        return ResponseEntity.ok("Planning created successfully");
    }

    @PostMapping("tasks/{Taskid}/state")
    public ResponseEntity<String> changeTaskStatus(@PathVariable Long Taskid, @RequestBody String status) {
        try {
            projectService.changeTaskStatus(Taskid, status);
            return ResponseEntity.ok("Task status updated successfully");
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        } catch (Exception e) {
            log.error("Error updating task status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating task status");
        }
    }

    @GetMapping("/today/{empId}")
    public ResponseEntity<List<ActivityOfDayDto>> getActivitiesOfDay(@PathVariable Long empId) {
        log.info("Récupération des activités du jour pour l'employé id {}", empId);
        List<ActivityOfDayDto> activities = pointageService.getActivitiesOfDay(empId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            log.info("Récupération des données du tableau de bord");
            DashboardResponseDto dashboardData = dashboardService.getDashboardData();
            log.info("Données du tableau de bord récupérées avec succès");
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données du tableau de bord: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la récupération des données du tableau de bord: " + e.getMessage());
        }
    }

}
