package ma.digitalia.suividutemps.services;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.services.EmployeService;
import ma.digitalia.gestionutilisateur.services.ManagerService;
import ma.digitalia.suividutemps.Enum.Priority;
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
    public WeeklyPointageDto getPointagesSemaineByEmploye(Long employeId, LocalDate date) {
        Employe employe = employeService.findById(employeId);
        LocalDate lundi = date.with(previousOrSame(DayOfWeek.MONDAY));
        LocalDate dimanche = date.with(nextOrSame(DayOfWeek.SUNDAY));
        List<Pointage> pointages = pointageRepository.findByEmployeAndDateBetween(employe, lundi, dimanche);
        Map<LocalDate, Pointage> pointagesParDate = pointages.stream()
                .collect(Collectors.toMap(Pointage::getDate, p -> p));
        List<WeekRowDto> weekRows = new ArrayList<>();
        double totalHours = 0;
        int absences = 0;
        int lateArrivals = 0;
        int totalArrivalMinutes = 0;
        int arrivalCount = 0;
        double overtimeHours = 0;
        LocalDate jourCourant = lundi;
        while (!jourCourant.isAfter(dimanche)) {
            Pointage p = pointagesParDate.get(jourCourant);
            String arrivee = "—";
            String sortie = "—";
            int effective = 0;
            int pauses = 0;
            String retard = "—";
            // Correction: calculer les pauses à partir des activités de type PAUSE
            if (p != null && p.getActivites() != null) {
                for (Activite activite : p.getActivites()) {
                    if (activite.getType() == TypeActivite.PAUSE && activite.getDebut() != null && activite.getFin() != null) {
                        pauses += (int) java.time.Duration.between(activite.getDebut(), activite.getFin()).toMinutes();
                    }
                }
            }
            if (p != null) {
                if (p.getHeureEntree() != null) {
                    arrivee = String.format("%02d:%02d", p.getHeureEntree().getHour(), p.getHeureEntree().getMinute());
                    arrivalCount++;
                    totalArrivalMinutes += p.getHeureEntree().getHour() * 60 + p.getHeureEntree().getMinute();
                    // Supposons que l'heure normale d'arrivée est 08:45
                    int retardMinutes = (p.getHeureEntree().getHour() * 60 + p.getHeureEntree().getMinute()) - (8 * 60 + 45);
                    if (retardMinutes > 0) {
                        retard = retardMinutes + "m";
                        lateArrivals++;
                    }
                }
                if (p.getHeureSortie() != null) {
                    sortie = String.format("%02d:%02d", p.getHeureSortie().getHour(), p.getHeureSortie().getMinute());
                }
                if (p.getHeuresTravaillees() != null) {
                    effective = (int) p.getHeuresTravaillees().toMinutes();
                    totalHours += effective / 60.0;
                }
            } else {
                absences++;
            }
            weekRows.add(new WeekRowDto(jourCourant.toString(), arrivee, sortie, effective, pauses, retard));
            jourCourant = jourCourant.plusDays(1);
        }
        String averageArrival = arrivalCount > 0 ? String.format("%02d:%02d", totalArrivalMinutes / arrivalCount / 60, totalArrivalMinutes / arrivalCount % 60) : "—";
        WeekStatsDto weekStats = new WeekStatsDto(totalHours, averageArrival, overtimeHours, absences, lateArrivals);
        return new WeeklyPointageDto(weekStats, weekRows);
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
        log.info("Récupération du manager avec ID: {}", managerId);
        Manager manager = managerService.findById(managerId);
        String department = manager.getDepartment();
        log.info("Récupération des employés pour le manager ID: {} dans le département: {}", managerId, department);
        List<Employe> equipe = manager.getEmployes();
        if (equipe == null || equipe.isEmpty()) {
            log.warn("Le manager ID: {} n'a pas d'employés dans son équipe.", managerId);
            return List.of();
        }
        log.info("Récupération du statut de l'équipe pour le manager ID: {} avec {} employés.", managerId, equipe.size());
        List<TeamStatusDto> teamStatusList = new ArrayList<>();
        for (Employe employe : equipe) {
            Pointage pointage = pointageRepository.findByEmployeAndDate(employe, date);
            boolean isConnected = pointage != null && pointage.getHeureEntree() != null && pointage.getHeureSortie() == null;
            int todayHours = 0;
            String avatar = "";
            String managerName = manager.getNom() + " " + manager.getPreNom();
            // Dernière activité du jour
            String lastActivity = null;
            String availabilityStatus = "indisponible";
            log.info("Traitement de l'employé ID: {} - {}", employe.getId(), employe.getNom());
            if (pointage != null && pointage.getActivites() != null && !pointage.getActivites().isEmpty()) {
                Activite last = pointage.getActivites().stream()
                    .max(Comparator.comparing(Activite::getDebut)).orElse(null);
                if (last != null) {
                    lastActivity = last.getDebut().toString();
                    switch (last.getType()) {
                        case TRAVAIL: availabilityStatus = "disponible"; break;
                        case PAUSE: availabilityStatus = "pause"; break;
                        case REUNION: availabilityStatus = "reunion"; break;
                        default: availabilityStatus = "occupe";
                    }
                }
            }
            log.info("Dernière activité pour l'employé ID: {} - {}", employe.getId(), lastActivity);
            // Calcul des heures de la semaine, retards, heures supp, objectif
            LocalDate lundi = date.with(previousOrSame(DayOfWeek.MONDAY));
            LocalDate dimanche = date.with(nextOrSame(DayOfWeek.SUNDAY));
            List<Pointage> weekPointages = pointageRepository.findByEmployeAndDateBetween(employe, lundi, dimanche);
            double weeklyHours = 0;
            int lateArrivalCount = 0;
            double overTimeHours = 0;
            double targetHours = 0;
            log.info("Calcul des statistiques hebdomadaires pour l'employé ID: {} entre {} et {}", employe.getId(), lundi, dimanche);
            for (Pointage p : weekPointages) {
                if (p.getHeuresTravaillees() != null) {
                    weeklyHours += p.getHeuresTravaillees().toMinutes() / 60.0;
                }
                if (p.getHeureEntree() != null) {
                    int retardMinutes = (p.getHeureEntree().getHour() * 60 + p.getHeureEntree().getMinute()) - (8 * 60 + 45);
                    if (retardMinutes > 0) lateArrivalCount++;
                }
                // Heures supp
                PlanningTravail planning = planningTravailRepository.findByJourSemaine(p.getDate().getDayOfWeek());
                if (planning != null && p.getHeuresTravaillees() != null && planning.getHeuresParJour() != null) {
                    double planned = planning.getHeuresParJour().toMinutes() / 60.0;
                    double worked = p.getHeuresTravaillees().toMinutes() / 60.0;
                    if (worked > planned) overTimeHours += (worked - planned);
                    targetHours += planned;
                }
            }
            log.info("Statistiques pour l'employé ID: {} - Heures hebdo: {}, Retards: {}, Heures supp: {}, Objectif: {}",
                    employe.getId(), weeklyHours, lateArrivalCount, overTimeHours, targetHours);
            // Performance simple (à affiner selon vos critères)
            String performance = "moyen";
            if (weeklyHours >= targetHours && lateArrivalCount == 0) performance = "excellent";
            else if (weeklyHours >= targetHours * 0.8) performance = "bon";
            else if (weeklyHours >= targetHours * 0.5) performance = "moyen";
            else performance = "faible";
            // Tâches urgentes
            int urgentTasks = 0;

            List<Tache> taches = weekPointages.stream()
                .filter(p -> p.getActivites() != null)
                .flatMap(p -> p.getActivites().stream())
                .map(Activite::getTache)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            log.info("Nombre de tâches associées aux pointages de la semaine pour l'employé ID: {} - {}", employe.getId(), taches.size());

            urgentTasks = (int) taches.stream()
                .filter(t -> t.getPriority() != null && (t.getPriority() == Priority.CRITICAL || t.getPriority() == Priority.HIGH )&& !t.isTerminee())
                .count();

            if (pointage != null && pointage.calculerHeuresTravaillees() != null) {
                todayHours = (int) pointage.getHeuresTravaillees().toMinutes();
            }
            TeamStatusDto dto = TeamStatusDto.builder()
                    .employeeId(String.valueOf(employe.getId()))
                    .employeeName(employe.getNom() + " " + employe.getPreNom())
                    .role(employe.getPoste() != null ? employe.getPoste() : "")
                    .isConnected(isConnected)
                    .todayHours(todayHours)
                    .avatar(avatar)
                    .lastActivity(lastActivity)
                    .performance(performance)
                    .weeklyHours(weeklyHours)
                    .targetHours(targetHours)
                    .lateArrivalCount(lateArrivalCount)
                    .overTimeHours(overTimeHours)
                    .department(department)
                    .manager(managerName)
                    .urgentTasks(urgentTasks)
                    .availabilityStatus(availabilityStatus)
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

    @Override
    public List<ActivityOfDayDto> getActivitiesOfDay(Long empId) {
        Employe employe = employeService.findById(empId);
        LocalDate today = LocalDate.now();
        Pointage pointageToday = pointageRepository.findByEmployeAndDate(employe, today);

        if (pointageToday == null) {
            return Collections.emptyList();
        }

        return pointageToday.getActivites().stream()
                .map(activite -> {
                    ActivityOfDayDto dto = new ActivityOfDayDto();
                    dto.setType(activite.getType().name());
                    dto.setStartTime(activite.getDebut() != null ? activite.getDebut().toString() : null);
                    dto.setEndTime(activite.getFin() != null ? activite.getFin().toString() : null);
                    dto.setDescription(activite.getDescription());
                    dto.setProjectName(activite.getProjet() != null ? activite.getProjet().getNom() : null);

                    if (activite.getTache() != null) {
                        ActivityOfDayDto.TaskDto taskDto = new ActivityOfDayDto.TaskDto();
                        taskDto.id = String.valueOf(activite.getTache().getId());
                        taskDto.name = activite.getTache().getNom();
                        taskDto.priority = activite.getTache().getPriority() != null ? activite.getTache().getPriority().name() : "NORMAL";
                        dto.setTask(taskDto);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PointageResponseDto getPointagesForWeek(LocalDate weekStart, Long employeId) {
        LocalDate weekEnd = weekStart.plusDays(6);

        Employe employe = employeService.findById(employeId);

        // Récupérer toutes les activités de la semaine
        List<Activite> activites = activiteRepo.findByEmployeAndPointageDateBetweenOrderByDebutAsc(employe, weekStart, weekEnd);

        // Mapper les activités vers le DTO
        List<PointageActivityDto> activities = activites.stream()
                .map(this::mapActiviteToDto)
                .collect(Collectors.toList());

        // Récupérer la planification de la semaine
        List<PointagePlannedDto> planned = getPlannedHoursForWeek(weekStart);

        return new PointageResponseDto(activities, planned);
    }

    private PointageActivityDto mapActiviteToDto(Activite activite) {
        String type;
        if (activite.getType() == TypeActivite.TRAVAIL) {
            type = "WORK";
        } else if (activite.getType() == TypeActivite.PAUSE) {
            type = "BREAK";
        } else if (activite.getType() == TypeActivite.REUNION) {
            type = "MEETING";
        } else {
            type = activite.getType().name(); // fallback
        }

        PointageProjectDto projectDto = null;
        if (activite.getProjet() != null) {
            projectDto = new PointageProjectDto(
                    activite.getProjet().getId(),
                    activite.getProjet().getNom(),
                    "#3498db", // couleur par défaut - à adapter selon vos besoins
                    activite.getProjet().getClient()
            );
        }

        return new PointageActivityDto(
                String.valueOf(activite.getId()),
                type,
                activite.getDescription(),
                activite.getDebut(),
                activite.getFin(),
                projectDto
        );
    }

    private List<PointagePlannedDto> getPlannedHoursForWeek(LocalDate weekStart) {
        List<PointagePlannedDto> planned = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = weekStart.plusDays(i);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Convertir DayOfWeek en format attendu (0 = Dimanche, 1 = Lundi, etc.)
            int dayOfWeekValue = (dayOfWeek.getValue() % 7);

            PlanningTravail planning = planningTravailRepository.findByJourSemaine(dayOfWeek);

            if (planning != null && planning.getHeuresParJour() != null) {
                // Pour simplifier, on suppose un seul projet par jour
                PointageProjectDto projectDto = getDefaultProjectForDay();

                if (projectDto != null) {
                    planned.add(new PointagePlannedDto(
                            dayOfWeekValue,
                            projectDto,
                            (int) planning.getHeuresParJour().toHours()
                    ));
                }
            }
        }

        return planned;
    }

    private PointageProjectDto getDefaultProjectForDay() {
        // Utiliser une requête plus simple pour éviter les erreurs
        List<Projet> projets = projetRepo.findAll();

        if (!projets.isEmpty()) {
            Projet projet = projets.get(0);
            return new PointageProjectDto(
                    projet.getId(),
                    projet.getNom(),
                    "#2ecc71",
                    projet.getClient()
            );
        }

        return null;
    }

    @Override
    public void startReunionTime(PointageRequest pointageRequest) {
        demarrerOuChangerActivite(pointageRequest.getEmpId(), TypeActivite.REUNION, null, null, pointageRequest.getCommentaire());
    }
}
