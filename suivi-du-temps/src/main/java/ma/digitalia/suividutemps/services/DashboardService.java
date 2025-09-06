package ma.digitalia.suividutemps.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.suividutemps.dto.*;
import ma.digitalia.suividutemps.entities.Activite;
import ma.digitalia.suividutemps.entities.Pointage;
import ma.digitalia.suividutemps.entities.Projet;
import ma.digitalia.suividutemps.Enum.TypeActivite;
import ma.digitalia.suividutemps.entities.Tache;
import ma.digitalia.suividutemps.repositories.ActiviteRepository;
import ma.digitalia.suividutemps.repositories.PointageRepository;
import ma.digitalia.suividutemps.repositories.ProjetRepository;
import ma.digitalia.suividutemps.repositories.TacheRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ActiviteRepository activiteRepository;
    private final PointageRepository pointageRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;

    /**
     * Génère toutes les données nécessaires pour le tableau de bord
     */
    public DashboardResponseDto getDashboardData() {
        log.info("Génération des données du tableau de bord");

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            List<Pointage> pointages = pointageRepository.findByDateBetween(startDate, endDate);
            List<Activite> activites = activiteRepository.findByPointageDateBetween(startDate, endDate);
            List<Projet> projets = projetRepository.findAll();
            List<Tache> taches = tacheRepository.findAll();

            DashboardKpisDto kpis = calculateKpis(pointages, activites, taches, startDate);
            List<WorkloadDataDto> workloadData = calculateWorkloadData(pointages, activites);
            List<ProductivityDataDto> productivityData = calculateProductivityData(pointages, activites, taches);
            List<ProjectDataDto> projectData = calculateProjectData(projets, activites, taches);
            List<WeeklyProductivityDto> weeklyProductivity = calculateWeeklyProductivity(pointages, activites);
            List<TimeDistributionDto> timeDistribution = calculateTimeDistribution(activites);
            List<WellnessDataDto> wellnessData = calculateWellnessData(pointages, activites);
            List<TeamEfficiencyDto> teamEfficiency = calculateTeamEfficiency(pointages, activites, projets);

            log.info("Données du tableau de bord générées avec succès");

            return new DashboardResponseDto(
                    kpis,
                    workloadData,
                    productivityData,
                    projectData,
                    weeklyProductivity,
                    timeDistribution,
                    wellnessData,
                    teamEfficiency
            );

        } catch (Exception e) {
            log.error("Erreur lors de la génération des données du tableau de bord: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération des données du tableau de bord", e);
        }
    }

    /**
     * Calcule les KPIs principaux basés sur les données réelles
     */
    private DashboardKpisDto calculateKpis(List<Pointage> pointages, List<Activite> activites, List<Tache> taches, LocalDate startDate) {
        log.debug("Calcul des KPIs");

        // Calcul du taux d'utilisation réel basé sur les heures travaillées vs heures pointées
        long totalMinutesTravaillees = activites.stream()
                .filter(a -> a.getType() == TypeActivite.TRAVAIL)
                .filter(a -> a.getDebut() != null && a.getFin() != null)
                .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes())
                .sum();

        long totalMinutesPointees = pointages.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .mapToLong(p -> p.getHeuresTravaillees().toMinutes())
                .sum();

        int utilisationRate = totalMinutesPointees > 0 ?
                (int) ((totalMinutesTravaillees * 100) / totalMinutesPointees) : 0;

        // Calcul des heures supplémentaires (>8h par jour)
        long totalHeuresSupp = pointages.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .mapToLong(p -> {
                    long heuresTravaillees = p.getHeuresTravaillees().toHours();
                    return Math.max(0, heuresTravaillees - 8);
                })
                .sum();

        long totalHeures = totalMinutesTravaillees / 60;
        int overtimeRate = totalHeures > 0 ? (int) ((totalHeuresSupp * 100) / totalHeures) : 0;

        // Productivité basée sur les tâches terminées
        long tachesTerminees = taches.stream()
                .filter(Tache::isTerminee)
                .count();
        long totalTaches = taches.size();
        int averageProductivity = totalTaches > 0 ? (int) ((tachesTerminees * 100) / totalTaches) : 0;

        // Équipes en surcharge (>8h/jour en moyenne par équipe)
        Map<Employe, List<Pointage>> pointagesParEmploye = pointages.stream()
                .filter(p -> p.getEmploye() != null)
                .collect(Collectors.groupingBy(Pointage::getEmploye));

        // Grouper les pointages par manager
        Map<Manager, List<Pointage>> pointagesParManager = new HashMap<>();

        for (Map.Entry<Employe, List<Pointage>> entry : pointagesParEmploye.entrySet()) {
            Employe employe = entry.getKey();
            List<Pointage> employePointages = entry.getValue();

            if (employe.getManager() != null) {
                Manager manager = employe.getManager();
                if (!pointagesParManager.containsKey(manager)) {
                    pointagesParManager.put(manager, new ArrayList<>());
                }
                pointagesParManager.get(manager).addAll(employePointages);
            }
        }

        int overloadedTeams = (int) pointagesParManager.entrySet().stream()
                .filter(entry -> {
                    double avgHours = entry.getValue().stream()
                            .filter(p -> p.getHeuresTravaillees() != null)
                            .mapToDouble(p -> p.getHeuresTravaillees().toHours())
                            .average()
                            .orElse(0);
                    return avgHours > 8;
                })
                .count();

        // Calcul des changements (comparaison avec période précédente)
        int utilisationChange = calculatePeriodChange(utilisationRate, startDate.minusDays(30), startDate, "utilisation");
        int overtimeChange = calculatePeriodChange(overtimeRate, startDate.minusDays(30), startDate, "overtime");
        int productivityChange = calculatePeriodChange(averageProductivity, startDate.minusDays(30), startDate, "productivity");
        int overloadedChange = calculatePeriodChange(overloadedTeams, startDate.minusDays(30), startDate, "overloaded");

        return new DashboardKpisDto(
                utilisationRate,
                utilisationChange,
                overtimeRate,
                overtimeChange,
                averageProductivity,
                productivityChange,
                overloadedTeams,
                overloadedChange
        );
    }

    /**
     * Calcule les données de charge de travail par employé
     */
    private List<WorkloadDataDto> calculateWorkloadData(List<Pointage> pointages, List<Activite> activites) {
        log.debug("Calcul des données de charge de travail");

        Map<Employe, List<Pointage>> pointagesByEmployee = pointages.stream()
                .filter(p -> p.getEmploye() != null)
                .collect(Collectors.groupingBy(Pointage::getEmploye));

        return pointagesByEmployee.entrySet().stream()
                .map(entry -> {
                    Employe employe = entry.getKey();
                    List<Pointage> empPointages = entry.getValue();

                    String employeeName = employe.getPreNom() + " " +
                            employe.getNom().substring(0, Math.min(employe.getNom().length(), 1)) + ".";

                    // Heures standard (≤8h) et supplémentaires (>8h)
                    int heuresStandard = 0;
                    int heuresSupp = 0;

                    for (Pointage p : empPointages) {
                        if (p.getHeuresTravaillees() != null) {
                            long heures = p.getHeuresTravaillees().toHours();
                            heuresStandard += (int) Math.min(heures, 8);
                            heuresSupp += (int) Math.max(0, heures - 8);
                        }
                    }

                    // Projet principal basé sur les activités
                    String projet = activites.stream()
                            .filter(a -> a.getPointage() != null &&
                                    a.getPointage().getEmploye() != null &&
                                    a.getPointage().getEmploye().getId().equals(employe.getId()))
                            .filter(a -> a.getProjet() != null)
                            .collect(Collectors.groupingBy(a -> a.getProjet().getNom(), Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("Sans projet");

                    // Productivité basée sur les activités de travail vs total
                    long minutesTravail = activites.stream()
                            .filter(a -> a.getPointage() != null &&
                                    a.getPointage().getEmploye() != null &&
                                    a.getPointage().getEmploye().getId().equals(employe.getId()))
                            .filter(a -> a.getType() == TypeActivite.TRAVAIL)
                            .filter(a -> a.getDebut() != null && a.getFin() != null)
                            .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes())
                            .sum();

                    long minutesTotal = empPointages.stream()
                            .filter(p -> p.getHeuresTravaillees() != null)
                            .mapToLong(p -> p.getHeuresTravaillees().toMinutes())
                            .sum();

                    int productivite = minutesTotal > 0 ? (int) ((minutesTravail * 100) / minutesTotal) : 0;

                    String departement = employe.getManager() != null ?
                            employe.getManager().getDepartment() : "Non assigné";

                    return new WorkloadDataDto(employeeName, heuresStandard, heuresSupp,
                            projet, productivite, departement);
                })
                .sorted((a, b) -> Integer.compare(b.heuresSupp(), a.heuresSupp()))
                .collect(Collectors.toList());
    }

    /**
     * Calcule les données de productivité
     */
    private List<ProductivityDataDto> calculateProductivityData(List<Pointage> pointages,
                                                                List<Activite> activites,
                                                                List<Tache> taches) {
        log.debug("Calcul des données de productivité");

        Map<Employe, List<Pointage>> pointagesByEmployee = pointages.stream()
                .filter(p -> p.getEmploye() != null)
                .collect(Collectors.groupingBy(Pointage::getEmploye));

        return pointagesByEmployee.entrySet().stream()
                .map(entry -> {
                    Employe employe = entry.getKey();
                    List<Pointage> empPointages = entry.getValue();

                    String employeeName = employe.getPreNom() + " " +
                            employe.getNom().substring(0, Math.min(employe.getNom().length(), 1)) + ".";

                    // Calcul des heures supplémentaires
                    int heuresSupp = empPointages.stream()
                            .filter(p -> p.getHeuresTravaillees() != null)
                            .mapToInt(p -> {
                                long heures = p.getHeuresTravaillees().toHours();
                                return (int) Math.max(0, heures - 8);
                            })
                            .sum();

                    // Productivité basée sur tâches terminées et activités productives
                    long tachesTermineesEmploye = taches.stream()
                            .filter(t -> t.getAjoutePar() != null &&
                                    t.getAjoutePar().getId().equals(employe.getId()))
                            .filter(Tache::isTerminee)
                            .count();

                    long activitesProductives = activites.stream()
                            .filter(a -> a.getPointage() != null &&
                                    a.getPointage().getEmploye() != null &&
                                    a.getPointage().getEmploye().getId().equals(employe.getId()))
                            .filter(a -> a.getType() == TypeActivite.TRAVAIL ||
                                    a.getType() == TypeActivite.REUNION)
                            .count();

                    long totalActivites = activites.stream()
                            .filter(a -> a.getPointage() != null &&
                                    a.getPointage().getEmploye() != null &&
                                    a.getPointage().getEmploye().getId().equals(employe.getId()))
                            .count();

                    int productivite = totalActivites > 0 ?
                            (int) ((activitesProductives * 100) / totalActivites) : 0;

                    // Ajustement basé sur les tâches terminées
                    if (tachesTermineesEmploye > 0) {
                        productivite = Math.min(100, productivite + (int) (tachesTermineesEmploye * 5));
                    }

                    return new ProductivityDataDto(employeeName, heuresSupp, productivite);
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcule les données des projets
     */
    private List<ProjectDataDto> calculateProjectData(List<Projet> projets,
                                                      List<Activite> activites,
                                                      List<Tache> taches) {
        log.debug("Calcul des données des projets");

        return projets.stream()
                .map(projet -> {
                    // Heures réelles travaillées sur le projet
                    int heures = (int) activites.stream()
                            .filter(a -> a.getProjet() != null &&
                                    a.getProjet().getId().equals(projet.getId()))
                            .filter(a -> a.getDebut() != null && a.getFin() != null)
                            .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toHours())
                            .sum();

                    // Budget du projet
                    int budget = projet.getBudget() != null ? projet.getBudget().intValue() : 0;

                    // Efficacité basée sur tâches terminées vs total
                    long tachesTerminees = taches.stream()
                            .filter(t -> t.getProjet() != null &&
                                    t.getProjet().getId().equals(projet.getId()))
                            .filter(Tache::isTerminee)
                            .count();

                    long totalTachesProjet = taches.stream()
                            .filter(t -> t.getProjet() != null &&
                                    t.getProjet().getId().equals(projet.getId()))
                            .count();

                    // Calcul de l'efficacité combinant progression et respect budget
                    int progressionTaches = totalTachesProjet > 0 ?
                            (int) ((tachesTerminees * 100) / totalTachesProjet) : 0;

                    double estimationTotale = taches.stream()
                            .filter(t -> t.getProjet() != null &&
                                    t.getProjet().getId().equals(projet.getId()))
                            .mapToDouble(t -> t.getEstimationHeures() != null ?
                                    t.getEstimationHeures() : 0)
                            .sum();

                    int efficaciteBudget = estimationTotale > 0 && heures > 0 ?
                            (int) ((estimationTotale * 100) / heures) : 100;

                    int efficacite = (progressionTaches + efficaciteBudget) / 2;

                    return new ProjectDataDto(projet.getNom(), heures, budget, efficacite);
                })
                .filter(p -> p.heures() > 0) // Ne montrer que les projets avec activité
                .collect(Collectors.toList());
    }

    /**
     * Calcule la productivité hebdomadaire
     */
    private List<WeeklyProductivityDto> calculateWeeklyProductivity(List<Pointage> pointages,
                                                                    List<Activite> activites) {
        log.debug("Calcul de la productivité hebdomadaire");

        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        List<WeeklyProductivityDto> weeklyData = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            String dayName = currentDay.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRENCH);

            List<Pointage> dayPointages = pointages.stream()
                    .filter(p -> p.getDate().equals(currentDay))
                    .toList();

            List<Activite> dayActivites = activites.stream()
                    .filter(a -> a.getPointage() != null &&
                            a.getPointage().getDate().equals(currentDay))
                    .toList();

            if (!dayPointages.isEmpty()) {
                // Productivité = temps de travail effectif / temps total pointé
                long minutesTravail = dayActivites.stream()
                        .filter(a -> a.getType() == TypeActivite.TRAVAIL ||
                                a.getType() == TypeActivite.REUNION)
                        .filter(a -> a.getDebut() != null && a.getFin() != null)
                        .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes())
                        .sum();

                long minutesTotal = dayPointages.stream()
                        .filter(p -> p.getHeuresTravaillees() != null)
                        .mapToLong(p -> p.getHeuresTravaillees().toMinutes())
                        .sum();

                int productivite = minutesTotal > 0 ? (int) ((minutesTravail * 100) / minutesTotal) : 0;

                // Heures actives (travail + réunion)
                double heuresActives = minutesTravail / 60.0;

                // Minutes de pause
                int pauses = dayPointages.stream()
                        .mapToInt(Pointage::getMinutesPause)
                        .sum();

                weeklyData.add(new WeeklyProductivityDto(dayName, productivite, heuresActives, pauses));
            } else if (currentDay.isAfter(LocalDate.now())) {
                // Jours futurs - pas de données
                continue;
            } else {
                // Jour passé sans pointage
                weeklyData.add(new WeeklyProductivityDto(dayName, 0, 0.0, 0));
            }
        }

        return weeklyData;
    }

    /**
     * Calcule la distribution du temps par type d'activité
     */
    private List<TimeDistributionDto> calculateTimeDistribution(List<Activite> activites) {
        log.debug("Calcul de la distribution du temps");

        Map<TypeActivite, Long> minutesPerType = activites.stream()
                .filter(a -> a.getDebut() != null && a.getFin() != null)
                .collect(Collectors.groupingBy(
                        Activite::getType,
                        Collectors.summingLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes())
                ));

        long totalMinutes = minutesPerType.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return Arrays.stream(TypeActivite.values())
                .map(type -> {
                    Long minutes = minutesPerType.getOrDefault(type, 0L);
                    String displayName = getTypeActiviteDisplayName(type);
                    int heures = (int) (minutes / 60);
                    int pourcentage = totalMinutes > 0 ? (int) ((minutes * 100) / totalMinutes) : 0;
                    return new TimeDistributionDto(displayName, heures, pourcentage);
                })
                .filter(dto -> dto.heures() > 0) // Ne montrer que les types avec du temps
                .sorted((a, b) -> Integer.compare(b.pourcentage(), a.pourcentage()))
                .collect(Collectors.toList());
    }

    /**
     * Calcule les données de bien-être basées sur les données réelles
     */
    private List<WellnessDataDto> calculateWellnessData(List<Pointage> pointages,
                                                        List<Activite> activites) {
        log.debug("Calcul des données de bien-être");

        List<WellnessDataDto> wellnessData = new ArrayList<>();

        // Respect pause déjeuner (12h-14h)
        long joursAvecPauseDejeuner = activites.stream()
                .filter(a -> a.getType() == TypeActivite.PAUSE)
                .filter(a -> a.getDebut() != null)
                .filter(a -> {
                    int heure = a.getDebut().getHour();
                    return heure >= 12 && heure <= 14;
                })
                .map(a -> a.getPointage().getDate())
                .distinct()
                .count();

        long joursTravailes = pointages.stream()
                .map(Pointage::getDate)
                .distinct()
                .count();

        int pauseDejeuerRespect = joursTravailes > 0 ?
                (int) ((joursAvecPauseDejeuner * 100) / joursTravailes) : 0;

        wellnessData.add(new WellnessDataDto("Respect pause déjeuner", pauseDejeuerRespect, 95,
                pauseDejeuerRespect >= 95 ? "success" : pauseDejeuerRespect >= 80 ? "warning" : "danger"));

        // Déconnexion 20h
        long joursDeconnexionOk = pointages.stream()
                .filter(p -> p.getHeureSortie() != null)
                .filter(p -> p.getHeureSortie().getHour() <= 20)
                .count();

        int deconnexion20h = joursTravailes > 0 ?
                (int) ((joursDeconnexionOk * 100) / joursTravailes) : 0;

        wellnessData.add(new WellnessDataDto("Déconnexion avant 20h", deconnexion20h, 90,
                deconnexion20h >= 90 ? "success" : deconnexion20h >= 75 ? "warning" : "danger"));

        // Charge de travail équilibrée (<9h/jour)
        long joursChargeOk = pointages.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .filter(p -> p.getHeuresTravaillees().toHours() <= 9)
                .count();

        int chargeEquilibree = joursTravailes > 0 ?
                (int) ((joursChargeOk * 100) / joursTravailes) : 0;

        wellnessData.add(new WellnessDataDto("Charge de travail équilibrée", chargeEquilibree, 85,
                chargeEquilibree >= 85 ? "success" : chargeEquilibree >= 70 ? "warning" : "danger"));

        // Temps de pause suffisant (>30min/jour)
        long joursAvecPauseSuffisante = pointages.stream()
                .filter(p -> p.getMinutesPause() >= 30)
                .count();

        int pausesSuffisantes = joursTravailes > 0 ?
                (int) ((joursAvecPauseSuffisante * 100) / joursTravailes) : 0;

        wellnessData.add(new WellnessDataDto("Pauses suffisantes", pausesSuffisantes, 80,
                pausesSuffisantes >= 80 ? "success" : pausesSuffisantes >= 60 ? "warning" : "danger"));

        return wellnessData;
    }

    /**
     * Calcule l'efficacité des équipes basée sur les départements
     */
    private List<TeamEfficiencyDto> calculateTeamEfficiency(List<Pointage> pointages,
                                                            List<Activite> activites,
                                                            List<Projet> projets) {
        log.debug("Calcul de l'efficacité des équipes");

        // Regrouper par département via le manager
        Map<String, List<Pointage>> pointagesParDepartement = pointages.stream()
                .filter(p -> p.getEmploye() != null &&
                        p.getEmploye().getManager() != null &&
                        p.getEmploye().getManager().getDepartment() != null)
                .collect(Collectors.groupingBy(p -> p.getEmploye().getManager().getDepartment()));

        return pointagesParDepartement.entrySet().stream()
                .map(entry -> {
                    String departement = entry.getKey();
                    List<Pointage> deptPointages = entry.getValue();

                    // Efficacité = temps productif / temps total
                    Set<Long> employeIds = deptPointages.stream()
                            .map(p -> p.getEmploye().getId())
                            .collect(Collectors.toSet());

                    long minutesProductives = activites.stream()
                            .filter(a -> a.getPointage() != null &&
                                    a.getPointage().getEmploye() != null &&
                                    employeIds.contains(a.getPointage().getEmploye().getId()))
                            .filter(a -> a.getType() == TypeActivite.TRAVAIL ||
                                    a.getType() == TypeActivite.REUNION)
                            .filter(a -> a.getDebut() != null && a.getFin() != null)
                            .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes())
                            .sum();

                    long minutesTotal = deptPointages.stream()
                            .filter(p -> p.getHeuresTravaillees() != null)
                            .mapToLong(p -> p.getHeuresTravaillees().toMinutes())
                            .sum();

                    int efficacite = minutesTotal > 0 ?
                            (int) ((minutesProductives * 100) / minutesTotal) : 0;

                    // Objectif basé sur le nombre de projets du département
                    int objectif = Math.min(95, 80 + projets.size() * 2);

                    // Heures supplémentaires moyennes
                    int heuresSupp = deptPointages.stream()
                            .filter(p -> p.getHeuresTravaillees() != null)
                            .mapToInt(p -> {
                                long heures = p.getHeuresTravaillees().toHours();
                                return (int) Math.max(0, heures - 8);
                            })
                            .sum() / Math.max(1, deptPointages.size());

                    return new TeamEfficiencyDto(departement, efficacite, objectif, heuresSupp);
                })
                .collect(Collectors.toList());
    }

    // Méthodes utilitaires

    private String getTypeActiviteDisplayName(TypeActivite type) {
        return switch (type) {
            case TRAVAIL -> "Travail";
            case REUNION -> "Réunion";
            case PAUSE -> "Pause";
        };
    }

    private int calculatePeriodChange(int currentValue, LocalDate startDate, LocalDate endDate, String kpiType) {
        log.debug("Calcul du changement par rapport à la période précédente pour {}", kpiType);

        try {
            // Récupérer les données de la période précédente
            List<Pointage> previousPointages = pointageRepository.findByDateBetween(startDate, endDate);
            List<Activite> previousActivites = activiteRepository.findByPointageDateBetween(startDate, endDate);
            List<Tache> previousTaches = tacheRepository.findAll(); // Les tâches peuvent être filtrées par date si nécessaire

            if (previousPointages.isEmpty()) {
                return 0; // Pas de données pour comparaison
            }

            // Calculer la valeur précédente en fonction du type de KPI
            int previousValue;

            switch (kpiType) {
                case "utilisation":
                    // Taux d'utilisation
                    long totalMinutesTravaillees = previousActivites.stream()
                            .filter(a -> a.getType() == TypeActivite.TRAVAIL)
                            .filter(a -> a.getDebut() != null && a.getFin() != null)
                            .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes())
                            .sum();

                    long totalMinutesPointees = previousPointages.stream()
                            .filter(p -> p.getHeuresTravaillees() != null)
                            .mapToLong(p -> p.getHeuresTravaillees().toMinutes())
                            .sum();

                    previousValue = totalMinutesPointees > 0 ?
                            (int) ((totalMinutesTravaillees * 100) / totalMinutesPointees) : 0;
                    break;

                case "overtime":
                    // Heures supplémentaires
                    long totalHeuresSupp = previousPointages.stream()
                            .filter(p -> p.getHeuresTravaillees() != null)
                            .mapToLong(p -> {
                                long heuresTravaillees = p.getHeuresTravaillees().toHours();
                                return Math.max(0, heuresTravaillees - 8);
                            })
                            .sum();

                    long totalHeures = previousActivites.stream()
                            .filter(a -> a.getType() == TypeActivite.TRAVAIL)
                            .filter(a -> a.getDebut() != null && a.getFin() != null)
                            .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes() / 60)
                            .sum();

                    previousValue = totalHeures > 0 ? (int) ((totalHeuresSupp * 100) / totalHeures) : 0;
                    break;

                case "productivity":
                    // Productivité
                    long tachesTerminees = previousTaches.stream()
                            .filter(Tache::isTerminee)
                            .count();
                    long totalTaches = previousTaches.size();
                    previousValue = totalTaches > 0 ? (int) ((tachesTerminees * 100) / totalTaches) : 0;
                    break;

                case "overloaded":
                    // Équipes en surcharge
                    Map<Employe, List<Pointage>> pointagesParEmploye = previousPointages.stream()
                            .filter(p -> p.getEmploye() != null)
                            .collect(Collectors.groupingBy(Pointage::getEmploye));

                    previousValue = (int) pointagesParEmploye.entrySet().stream()
                            .filter(entry -> {
                                double avgHours = entry.getValue().stream()
                                        .filter(p -> p.getHeuresTravaillees() != null)
                                        .mapToDouble(p -> p.getHeuresTravaillees().toHours())
                                        .average()
                                        .orElse(0);
                                return avgHours > 8;
                            })
                            .count();
                    break;

                default:
                    // Utiliser la méthode générique par défaut
                    previousValue = calculateKpiForPeriod(previousPointages, previousActivites, previousTaches);
            }

            // Calculer le pourcentage de changement
            if (previousValue == 0) {
                return currentValue > 0 ? 100 : 0;
            }

            return ((currentValue - previousValue) * 100) / previousValue;

        } catch (Exception e) {
            log.warn("Erreur lors du calcul du changement de période pour {}: {}", kpiType, e.getMessage());
            return 0;
        }
    }

    /**
     * Calcule un KPI spécifique pour une période donnée (utilisé pour les comparaisons)
     */
    private int calculateKpiForPeriod(List<Pointage> pointages, List<Activite> activites, List<Tache> taches) {
        // Taux d'utilisation
        long totalMinutesTravaillees = activites.stream()
                .filter(a -> a.getType() == TypeActivite.TRAVAIL)
                .filter(a -> a.getDebut() != null && a.getFin() != null)
                .mapToLong(a -> Duration.between(a.getDebut(), a.getFin()).toMinutes())
                .sum();

        long totalMinutesPointees = pointages.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .mapToLong(p -> p.getHeuresTravaillees().toMinutes())
                .sum();

        return totalMinutesPointees > 0 ?
                (int) ((totalMinutesTravaillees * 100) / totalMinutesPointees) : 0;
    }
}
