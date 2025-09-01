package ma.digitalia.suividutemps.services;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.services.EmployeService;
import ma.digitalia.gestionutilisateur.services.ManagerService;
import ma.digitalia.suividutemps.Enum.StatutPointage;
import ma.digitalia.suividutemps.Enum.TypeActivite;
import ma.digitalia.suividutemps.dto.*;
import ma.digitalia.suividutemps.entities.*;
import ma.digitalia.suividutemps.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Slf4j
@Service
@Transactional
public class PointageServiceImpl implements PointageService {

    private final PointageRepository pointageRepository;
    private final EmployeService employeService;
    private final ManagerService managerService;
    private final PlanningTravailRepository planningTravailRepository;
    private final ActiviteRepository activiteRepo;
    private final ProjetRepository projetRepo;
    private final TacheRepository tacheRepo;


    public PointageServiceImpl(PointageRepository pointageRepository, EmployeService employeService, ManagerService managerService, PlanningTravailRepository planningTravailRepository
    , ActiviteRepository activiteRepo, ProjetRepository projetRepo, TacheRepository tacheRepo) {
        this.pointageRepository = pointageRepository;
        this.employeService = employeService;
        this.managerService = managerService;
        this.planningTravailRepository = planningTravailRepository;
        this.activiteRepo = activiteRepo;
        this.projetRepo = projetRepo;
        this.tacheRepo = tacheRepo;
    }

    @Override
    @Transactional
    public void startTimeTracking(PointageRequest pointageRequest) {
        if (pointageRepository.findByEmployeIdAndStatut(pointageRequest.getEmpId(), StatutPointage.EN_COURS).isPresent()) {
            throw new IllegalStateException("Une journée est déjà en cours pour cet employé.");
        }
        Employe employe = employeService.findById(pointageRequest.getEmpId());

        Pointage pointage = new Pointage();
        pointage.setEmploye(employe);
        pointage.setDate(LocalDate.now());
        pointage.setHeureEntree(LocalDateTime.now());
        pointage.setStatut(StatutPointage.EN_COURS);

        pointageRepository.save(pointage);

    }

    @Override
    @Transactional
    public void stopTimeTracking(PointageRequest pointageRequest) {

        terminerActiviteCourante(pointageRequest.getEmpId());

        Pointage pointage = pointageRepository.findByEmployeIdAndStatut(pointageRequest.getEmpId(), StatutPointage.EN_COURS)
                .orElseThrow(() -> new IllegalStateException("Aucune journée de travail à terminer pour cet employé."));

        pointage.calculerHeuresTravaillees();
        pointage.setHeureSortie(LocalDateTime.now());
        pointage.setStatut(StatutPointage.TERMINE);

        pointageRepository.save(pointage);
    }

    private void terminerActiviteCourante(Long employeId) {
        Optional<Activite> activiteEnCoursOpt = activiteRepo.findByPointageEmployeIdAndFinIsNull(employeId);

        activiteEnCoursOpt.ifPresent(activite -> {
            activite.setFin(LocalDateTime.now());
            // Si c'était une tâche, on pourrait vouloir changer son statut (ex: EN_PAUSE)
            // Pour l'instant, on la laisse EN_COURS
            activiteRepo.save(activite);
        });
    }

    @Override
    public void startPauseTime(PointageRequest pointageRequest) {
        demarrerOuChangerActivite(pointageRequest.getEmpId(), TypeActivite.PAUSE, null, null, pointageRequest.getCommentaire());
    }

    @Override
    public void stopPauseTime(PointageRequest pointageRequest) {
        terminerActiviteCourante(pointageRequest.getEmpId());
    }

    @Override
    public Activite demarrerOuChangerActivite(Long employeId, TypeActivite type, Long projetId, Long tacheId, String description) {
        terminerActiviteCourante(employeId);

        Pointage pointageEnCours = pointageRepository.findByEmployeIdAndStatut(employeId, StatutPointage.EN_COURS)
                .orElseThrow(() -> new IllegalStateException("L'employé doit d'abord démarrer sa journée avant de commencer une activité."));

        Activite nouvelleActivite = new Activite();
        nouvelleActivite.setDebut(LocalDateTime.now());
        nouvelleActivite.setFin(null); // 'null' signifie "en cours"
        nouvelleActivite.setType(type);
        nouvelleActivite.setDescription(description);
        nouvelleActivite.setPointage(pointageEnCours);

        if (projetId != null) {
            Projet projet = projetRepo.findById(projetId).orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));
            nouvelleActivite.setProjet(projet);
        }
        if (tacheId != null) {
            Tache tache = tacheRepo.findById(tacheId).orElseThrow(() -> new EntityNotFoundException("Tâche non trouvée"));
            nouvelleActivite.setTache(tache);
            nouvelleActivite.setProjet(tache.getProjet());
        }

        return activiteRepo.save(nouvelleActivite);
    }

    @Override
    public Pointage getPointageByEmployeAndDate(Long employeId, LocalDate date) {
        try {
            Employe employe = employeService.findById(employeId);
            Pointage pointage = pointageRepository.findByEmployeAndDate(employe, date);
            if (pointage == null) {
                log.warn("Aucun pointage trouvé pour l'employé ID: {} et la date: {}", employeId, date);
                throw new RuntimeException("Aucun pointage trouvé pour l'employé et la date spécifiés.");
            }
            log.info("Pointage récupéré pour l'employé ID: {} et la date: {}", employeId, date);
            return pointage;
        } catch (Exception e) {
            log.error("Erreur lors de la recuperation du pointage by employe id : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération du pointage", e);
        }
    }

    @Override
    public List<Pointage> getPointagesSemaineByEmploye(Long employeId, LocalDate date) {
        try {
            Employe employe = employeService.findById(employeId);
            LocalDate lundi = date.with(previousOrSame(DayOfWeek.MONDAY));
            LocalDate dimanche = date.with(nextOrSame(DayOfWeek.SUNDAY));
            List<Pointage> pointages = pointageRepository.findByEmployeAndDateBetween(employe, lundi, dimanche);
            if (pointages == null) {
                log.warn("Aucun pointage trouvé pour l'employé ID: {} et la date: {}", employeId, date);
                throw new RuntimeException("Aucun pointage trouvé pour l'employé et la date spécifiés.");
            }
            log.info("Pointage récupéré pour l'employé ID: {} et la date: {}", employeId, date);
            return pointages;
        } catch (Exception e) {
            log.error("Erreur lors de la recuperation du pointage by employe id : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération du pointage", e);
        }
    }

    @Override
    public List<EfficaciteParSemaineDTO> getEfficaciteParSemaine(Long employeId, LocalDate date) {
        Employe employe = employeService.findById(employeId);
        LocalDate lundi = date.with(previousOrSame(DayOfWeek.MONDAY));
        LocalDate dimanche = date.with(nextOrSame(DayOfWeek.SUNDAY));
        List<Pointage> pointages = pointageRepository.findByEmployeAndDateBetween(employe, lundi, dimanche);

        // Créer une map des pointages par date pour un accès rapide
        Map<LocalDate, Pointage> pointagesParDate = pointages.stream()
                .collect(Collectors.toMap(Pointage::getDate, pointage -> pointage));

        List<EfficaciteParSemaineDTO> efficaciteSemaine = new ArrayList<>();

        // Parcourir chaque jour de la semaine (du lundi au dimanche)
        LocalDate jourCourant = lundi;
        while (!jourCourant.isAfter(dimanche)) {
            Pointage pointage = pointagesParDate.get(jourCourant);

            // Nom du jour en français (abrégé)
            String nomJour = jourCourant.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.FRENCH);

            int totalMinutes = 0;
            double totalHours = 0.0;

            if (pointage != null && pointage.getHeuresTravaillees() != null) {
                totalMinutes = (int) pointage.getHeuresTravaillees().toMinutes();
                totalHours = totalMinutes / 60.0;
            }

            EfficaciteParSemaineDTO dto = new EfficaciteParSemaineDTO(
                    jourCourant,
                    totalMinutes,
                    totalHours,
                    nomJour
            );

            efficaciteSemaine.add(dto);
            jourCourant = jourCourant.plusDays(1);
        }
        return efficaciteSemaine;
    }

    @Override
    public List<TeamStatusDto> getTeamStatus(Long managerId, LocalDate date) {
        if(!managerService.existsById(managerId)){
            throw new EntityNotFoundException("Manager with ID " + managerId + " not found.");
        }
        Manager manager = managerService.findById(managerId);
        List<Employe> equipe = manager.getEmployes();
        if (equipe == null || equipe.isEmpty()) {
            log.warn("Le manager ID: {} n'a pas d'employés dans son équipe.", managerId);
            return List.of();
        }
        List<TeamStatusDto> teamStatusList = new ArrayList<>();
        for (Employe employe : equipe) {
            Pointage pointage = pointageRepository.findByEmployeAndDate(employe, date);
            boolean isConnected = pointage != null && pointage.getHeureEntree() != null && pointage.getHeureSortie() == null;
            int todayHours = 0;
            if (pointage != null && pointage.calculerHeuresTravaillees() != null) {
                todayHours = (int) pointage.getHeuresTravaillees().toMinutes();
            }
            TeamStatusDto dto = TeamStatusDto.builder()
                    .employeeId(String.valueOf(employe.getId()))
                    .employeeName(employe.getNom() + " " + employe.getPreNom())
                    .role(employe.getPoste() != null ? employe.getPoste() : "")
                    .isConnected(isConnected)
                    .todayHours(todayHours)
                    .build();
            teamStatusList.add(dto);
        }
        log.info("TeamStatusList: {}", teamStatusList.get(1));
        return teamStatusList;
    }

    @Override
    public List<PunchDto> getPunchsForEmployee(Long employeId, LocalDate startDate, LocalDate endDate) {
        Employe employe = employeService.findById(employeId);
        List<Pointage> pointages = pointageRepository.findByEmployeAndDateBetween(employe, startDate, endDate);
        List<PunchDto> punchDtos = new ArrayList<>();
        for (Pointage pointage : pointages) {
            int dailyObjective = 0;
            PlanningTravail planning = planningTravailRepository.findByJourSemaine(pointage.getDate().getDayOfWeek());
            if (planning != null && planning.getHeuresParJour() != null) {
                dailyObjective = (int) planning.getHeuresParJour().toMinutes();
            } else if (planning != null && planning.getHeureDebutMatin() != null && planning.getHeureFinApresMidi() != null) {
                dailyObjective = (int) java.time.Duration.between(planning.getHeureDebutMatin(), planning.getHeureFinApresMidi()).toMinutes();
            }
            PunchDto punchDto = mapPointageToPunchDto(pointage, dailyObjective);
            punchDtos.add(punchDto);
        }
        return punchDtos;
    }

    Integer calculateOvertime(Pointage pointage) {
        DayOfWeek dayOfWeek = pointage.getDate().getDayOfWeek();
        PlanningTravail planning = planningTravailRepository.findByJourSemaine(dayOfWeek);
        if (pointage.getHeuresTravaillees() == null && planning.getHeuresParJour() == null) {
            return 0;
        }else if (planning.getHeuresParJour() == null) {
            return (int) pointage.getHeuresTravaillees().toMinutes();
        }
        int workedMinutes = (int) pointage.getHeuresTravaillees().toMinutes();
        int plannedMinutes = (int) planning.getHeuresParJour().toMinutes();
        return Math.max(0, workedMinutes - plannedMinutes);
    }

    @Override
    public MonthlyPresenceReportDto getMonthlyPresenceReport(Long employeId, int year, int month) {
        Employe employe = employeService.findById(employeId);
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        List<PunchDto> dailyPunches = new ArrayList<>();
        int monthlyWorkObjectiveMinutes = 0;
        int totalEffectiveWorkMinutes = 0;
        int totalOvertimeMinutes = 0;
        int totalAnomalies = 0;
        int presenceDays = 0;
        int remoteDays = 0;
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            // Exclure samedi et dimanche du calcul
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }
            PlanningTravail planning = planningTravailRepository.findByJourSemaine(dayOfWeek);
            int dailyObjective = 0;
            if (planning != null && planning.getHeuresParJour() != null) {
                dailyObjective = (int) planning.getHeuresParJour().toMinutes();
            } else if (planning != null && planning.getHeureDebutMatin() != null && planning.getHeureFinApresMidi() != null) {
                dailyObjective = (int) java.time.Duration.between(planning.getHeureDebutMatin(), planning.getHeureFinApresMidi()).toMinutes();
            }
            monthlyWorkObjectiveMinutes += dailyObjective;
            Pointage pointage = pointageRepository.findByEmployeAndDate(employe, date);
            if (pointage != null) {
                PunchDto punch = mapPointageToPunchDto(pointage, dailyObjective);
                dailyPunches.add(punch);
                totalEffectiveWorkMinutes += punch.getEffectiveWorkMinutes();
                if (punch.getOvertime() != null) totalOvertimeMinutes += punch.getOvertime();
                if (punch.getAnomalies() != null) totalAnomalies += punch.getAnomalies().size();
                if ("PRESENT".equals(punch.getStatus())) presenceDays++;
                if ("Télétravail".equalsIgnoreCase(punch.getLocation())) remoteDays++;
            }
        }
        MonthlySummaryDto summary = new MonthlySummaryDto(
                monthlyWorkObjectiveMinutes,
                totalEffectiveWorkMinutes,
                totalOvertimeMinutes,
                totalAnomalies,
                presenceDays,
                remoteDays
        );
        return new MonthlyPresenceReportDto(summary, dailyPunches);
    }

    @Override
    public List<ActivityOfDayDto> getActivitiesOfDay(Long empId) {
        List<ActivityOfDayDto> result = new ArrayList<>();
        Employe employe = employeService.findById(empId);
        Pointage pointage = pointageRepository.findByEmployeAndDate(employe, java.time.LocalDate.now());
        if (pointage == null || pointage.getActivites() == null) return result;
        for (Activite activite : pointage.getActivites()) {
            ActivityOfDayDto dto = new ActivityOfDayDto();
            dto.type = activite.getType().name();
            dto.startTime = activite.getDebut() != null ? activite.getDebut().toString() : null;
            dto.endTime = activite.getFin() != null ? activite.getFin().toString() : null;
            dto.description = activite.getDescription();
            if (activite.getTache() != null) {
                ActivityOfDayDto.TaskDto taskDto = new ActivityOfDayDto.TaskDto();
                taskDto.id = activite.getTache().getId() != null ? activite.getTache().getId().toString() : null;
                taskDto.name = activite.getTache().getNom();
                taskDto.priority = activite.getTache().getPriority() != null ? activite.getTache().getPriority().name() : null;
                dto.task = taskDto;
            }
            if (activite.getProjet() != null) {
                dto.projectName = activite.getProjet().getNom();
            }
            result.add(dto);
        }
        return result;
    }

    // pour calendar à ajuster ulterieurement
    private PunchDto mapPointageToPunchDto(Pointage pointage, int dailyObjective) {
        String id = pointage.getId() != null ? "punch_" + pointage.getId() : null;
        String date = pointage.getDate() != null ? pointage.getDate().toString() : null;
        String start = (pointage.getHeureEntree() != null)
                ? pointage.getHeureEntree().atZone(java.time.ZoneId.systemDefault()).toInstant().toString()
                : null;
        String end = (pointage.getHeureSortie() != null)
                ? pointage.getHeureSortie().atZone(java.time.ZoneId.systemDefault()).toInstant().toString()
                : null;
        String status = pointage.getStatut() != null ? pointage.getStatut().name() : "IN_PROGRESS";
        int effectiveWorkMinutes = 0;
        if (pointage.calculerHeuresTravaillees() != null) {
            effectiveWorkMinutes = (int) pointage.calculerHeuresTravaillees().toMinutes();
        }
        Integer overtime = calculateOvertime(pointage);
        String location = "Bureau"; // À adapter si le champ existe
        List<PauseDto> pauses = new ArrayList<>();
        int totalPauseMinutes = 0;
        // Utilisation des Activite de type PAUSE
        if (pointage.getActivites() != null) {
            for (Activite activite : pointage.getActivites()) {
                if (activite.getType() == TypeActivite.PAUSE && activite.getDebut() != null && activite.getFin() != null) {
                    PauseDto pause = new PauseDto();
                    pause.setId("pause_" + activite.getId());
                    pause.setType("LUNCH");
                    pause.setStart(activite.getDebut().atZone(java.time.ZoneId.systemDefault()).toInstant().toString());
                    pause.setEnd(activite.getFin().atZone(java.time.ZoneId.systemDefault()).toInstant().toString());
                    int pauseDuration = (int) java.time.Duration.between(activite.getDebut(), activite.getFin()).toMinutes();
                    pause.setDuration(pauseDuration);
                    pauses.add(pause);
                    totalPauseMinutes += pauseDuration;
                }
            }
        }
        List<AnomalyDto> anomalies = new ArrayList<>();
        PlanningTravail planning = planningTravailRepository.findByJourSemaine(pointage.getDate().getDayOfWeek());
        if (planning != null) {
            if (pointage.getHeureEntree() != null && planning.getHeureDebutMatin() != null && pointage.getHeureEntree().toLocalTime().isAfter(planning.getHeureDebutMatin().plusMinutes(10))) {
                anomalies.add(new AnomalyDto(
                        "anomaly_" + id + "_lateness",
                        "LATENESS",
                        "Arrivée en retard de " + java.time.Duration.between(planning.getHeureDebutMatin(), pointage.getHeureEntree().toLocalTime()).toMinutes() + " minutes (prévu " + planning.getHeureDebutMatin() + ")",
                        "warning"
                ));
            }
            if (pointage.getHeureSortie() != null && planning.getHeureFinApresMidi() != null && pointage.getHeureSortie().toLocalTime().isBefore(planning.getHeureFinApresMidi())) {
                anomalies.add(new AnomalyDto(
                        "anomaly_" + id + "_earlyleave",
                        "EARLY_LEAVE",
                        "Départ anticipé de " + java.time.Duration.between(pointage.getHeureSortie().toLocalTime(), planning.getHeureFinApresMidi()).toMinutes() + " minutes (prévu " + planning.getHeureFinApresMidi() + ")",
                        "warning"
                ));
            }
            for (PauseDto pause : pauses) {
                int dureePrevue = 0;
                if (planning.getHeureFinMatin() != null && planning.getHeureDebutApresMidi() != null) {
                    dureePrevue = (int) java.time.Duration.between(planning.getHeureFinMatin(), planning.getHeureDebutApresMidi()).toMinutes();
                }
                if (dureePrevue > 0) {
                    if (pause.getDuration() > dureePrevue) {
                        anomalies.add(new AnomalyDto(
                                "anomaly_" + id + "_longbreak",
                                "LONG_BREAK",
                                "Pause plus longue que prévu (" + pause.getDuration() + " min au lieu de " + dureePrevue + " min)",
                                "info"
                        ));
                    } else if (pause.getDuration() < dureePrevue) {
                        anomalies.add(new AnomalyDto(
                                "anomaly_" + id + "_shortbreak",
                                "SHORT_BREAK",
                                "Pause plus courte que prévu (" + pause.getDuration() + " min au lieu de " + dureePrevue + " min)",
                                "info"
                        ));
                    }
                }
            }
        }
        return new PunchDto(id, date, start, end, status, effectiveWorkMinutes, totalPauseMinutes, overtime, location, pauses, anomalies, dailyObjective);
    }
}
