package ma.digitalia.gestionconges.services;

import ma.digitalia.gestionconges.Enum.StatutDemande;
import ma.digitalia.gestionconges.dto.*;
import ma.digitalia.gestionconges.entities.DemandeConge;
import ma.digitalia.gestionconges.entities.SoldeConge;
import ma.digitalia.gestionconges.entities.TypeConge;
import ma.digitalia.gestionconges.repositories.DemandeCongeRepository;
import ma.digitalia.gestionconges.repositories.SoldeCongeRepository;
import ma.digitalia.gestionconges.repositories.TypeCongeRpository;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import ma.digitalia.gestionutilisateur.repositories.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaveStatisticsService {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @Autowired
    private SoldeCongeRepository soldeCongeRepository;

    @Autowired
    private TypeCongeRpository typeCongeRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    public LeaveStatisticsDTO getLeaveStatistics() {
        LeaveStatisticsDTO statistics = new LeaveStatisticsDTO();

        statistics.setLeaveData(getDepartmentLeaveData());
        statistics.setLeaveTypeData(getLeaveTypeData());
        statistics.setMonthlyTrendData(getMonthlyTrendData());
        statistics.setCongesParDepartement(getCongesParDepartement());
        statistics.setRepartitionConges(getRepartitionConges());
        statistics.setAbsencesMensuelles(getAbsencesMensuelles());
//        statistics.setHeatmap(getHeatmapData());
        statistics.setHeatmapXLabels(Arrays.asList("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"));
//        statistics.setHeatmapYLabels(getEmployeeNamesForHeatmap());
        statistics.setCongesVsAlloues(getCongesVsAlloues());
        statistics.setSoldeConges(getSoldeConges());
        statistics.setAbsencesParJourSemaine(getAbsencesParJourSemaine());

        return statistics;
    }

    private List<DepartmentLeaveDataDTO> getDepartmentLeaveData() {
        List<DepartmentLeaveDataDTO> departmentData = new ArrayList<>();

        // Récupérer tous les types de congés existants
        List<TypeConge> allLeaveTypes = typeCongeRepository.findAll();

        // Récupérer tous les départements
        List<String> departments = employeRepository.findAllDepartments();

        for (String departmentName : departments) {
            if (departmentName != null) {
                List<Employe> employees = employeRepository.findByDepartement(departmentName);

                // Initialiser une map pour compter les jours par type de congé
                Map<String, Integer> leaveTypeCount = new HashMap<>();
                Map<String, String> leaveTypeColors = new HashMap<>();

                for (TypeConge type : allLeaveTypes) {
                    leaveTypeCount.put(type.getNom(), 0);
                    // Ajouter la couleur du type (ou couleur par défaut si null)
                    String color = type.getCouleur() != null ? type.getCouleur() : getDefaultColor(type.getNom());
                    leaveTypeColors.put(type.getNom(), color);
                }

                for (Employe employe : employees) {
                    List<DemandeConge> demandes = demandeCongeRepository.findDemandeCongeByDemandeur(employe);

                    for (DemandeConge demande : demandes) {
                        if (demande.getDateDebut().getYear() == LocalDate.now().getYear()) {
                            String typeNom = demande.getTypeConge().getNom();


                            if (demande.getStatut() == StatutDemande.VALIDEE) {
                                leaveTypeCount.put(typeNom,
                                    leaveTypeCount.getOrDefault(typeNom, 0) + demande.getNombreJours());
                            }
                        }
                    }
                }

                departmentData.add(new DepartmentLeaveDataDTO(departmentName, leaveTypeCount, leaveTypeColors));
            }
        }

        return departmentData;
    }

    private List<LeaveTypeDataDTO> getLeaveTypeData() {
        List<LeaveTypeDataDTO> leaveTypeData = new ArrayList<>();
        List<TypeConge> types = typeCongeRepository.findAll();

        for (TypeConge type : types) {
            List<DemandeConge> demandes = demandeCongeRepository.findAll().stream()
                    .filter(d -> d.getTypeConge().equals(type) &&
                               d.getDateDebut().getYear() == LocalDate.now().getYear())
                    .collect(Collectors.toList());

            int totalJours = demandes.stream()
                    .mapToInt(DemandeConge::getNombreJours)
                    .sum();

            String color = type.getCouleur() != null ? type.getCouleur() : getDefaultColor(type.getNom());

            leaveTypeData.add(new LeaveTypeDataDTO(type.getNom(), totalJours, color));
        }

        return leaveTypeData;
    }

    private List<MonthlyTrendDataDTO> getMonthlyTrendData() {
        List<MonthlyTrendDataDTO> monthlyData = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        for (int month = 1; month <= 12; month++) {
            int currentYearCount = getLeaveCountForMonth(currentYear, month);
            int previousYearCount = getLeaveCountForMonth(currentYear - 1, month);

            String monthName = LocalDate.of(currentYear, month, 1)
                    .getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);

            monthlyData.add(new MonthlyTrendDataDTO(monthName, currentYearCount, previousYearCount));
        }

        return monthlyData;
    }

    private int getLeaveCountForMonth(int year, int month) {
        return demandeCongeRepository.findAll().stream()
                .filter(d -> d.getDateDebut().getYear() == year &&
                        d.getDateDebut().getMonthValue() == month&&
                        d.getStatut() == StatutDemande.VALIDEE)
                .mapToInt(DemandeConge::getNombreJours)
                .sum();
    }

    private List<CongesParDepartementDTO> getCongesParDepartement() {
        List<CongesParDepartementDTO> result = new ArrayList<>();
        List<String> departments = employeRepository.findAllDepartments();

        for (String departmentName : departments) {
            if (departmentName != null) {
                List<Employe> employees = employeRepository.findByDepartement(departmentName);

                // Utiliser les types de congés réels au lieu de catégories fixes
                Map<String, Integer> leaveTypeCount = new HashMap<>();

                for (Employe employe : employees) {
                    List<DemandeConge> demandes = demandeCongeRepository.findDemandeCongeByDemandeur(employe);

                    for (DemandeConge demande : demandes) {
                        if (demande.getDateDebut().getYear() == LocalDate.now().getYear()) {
                            String typeNom = demande.getTypeConge().getNom();
                            leaveTypeCount.put(typeNom,
                                leaveTypeCount.getOrDefault(typeNom, 0) + demande.getNombreJours());
                        }
                    }
                }

                // Pour compatibilité avec l'ancien format, on peut mapper vers les catégories génériques
                int annuel = leaveTypeCount.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains("annuel") ||
                                   entry.getKey().toLowerCase().contains("congé"))
                    .mapToInt(Map.Entry::getValue)
                    .sum();

                int maladie = leaveTypeCount.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains("maladie"))
                    .mapToInt(Map.Entry::getValue)
                    .sum();

                int exceptionnel = leaveTypeCount.entrySet().stream()
                    .filter(entry -> !entry.getKey().toLowerCase().contains("annuel") &&
                                   !entry.getKey().toLowerCase().contains("congé") &&
                                   !entry.getKey().toLowerCase().contains("maladie"))
                    .mapToInt(Map.Entry::getValue)
                    .sum();

                result.add(new CongesParDepartementDTO(departmentName, annuel, maladie, exceptionnel));
            }
        }

        return result;
    }

    private List<RepartitionCongesDTO> getRepartitionConges() {
        List<RepartitionCongesDTO> result = new ArrayList<>();
        Map<String, Integer> typeCount = new HashMap<>();

        List<DemandeConge> demandes = demandeCongeRepository.findAll().stream()
                .filter(d -> d.getDateDebut().getYear() == LocalDate.now().getYear())
                .collect(Collectors.toList());

        for (DemandeConge demande : demandes) {
            String typeNom = demande.getTypeConge().getNom();
            typeCount.put(typeNom, typeCount.getOrDefault(typeNom, 0) + demande.getNombreJours());
        }

        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            result.add(new RepartitionCongesDTO(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    private List<AbsencesMensuellesDTO> getAbsencesMensuelles() {
        List<AbsencesMensuellesDTO> result = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        for (int month = 1; month <= 12; month++) {
            int joursAbsence = getLeaveCountForMonth(currentYear, month);
            String monthName = LocalDate.of(currentYear, month, 1)
                    .getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);

            result.add(new AbsencesMensuellesDTO(monthName, joursAbsence));
        }

        return result;
    }

//    private List<List<Integer>> getHeatmapData() {
//        // Générer une heatmap simple basée sur les congés par employé et jour de la semaine
//        List<List<Integer>> heatmap = new ArrayList<>();
//        List<Employe> employees = employeRepository.findAll();
//
//        for (int i = 0; i < Math.min(employees.size(), 10); i++) {
//            List<Integer> weekData = new ArrayList<>();
//            for (int day = 0; day < 7; day++) {
//                // Simuler des données de présence/absence (0 = présent, 1 = absent)
//                weekData.add((int) (Math.random() * 2));
//            }
//            heatmap.add(weekData);
//        }
//
//        return heatmap;
//    }

//    private List<String> getEmployeeNamesForHeatmap() {
//        List<Employe> allEmployees = (List<Employe>) employeRepository.findAll();
//        return allEmployees.stream()
//                .limit(10)
//                .map(e -> e.getNom() + " " + e.getPrenom())
//                .collect(Collectors.toList());
//    }

    private List<CongesVsAllouesDTO> getCongesVsAlloues() {
        List<CongesVsAllouesDTO> result = new ArrayList<>();
        List<Employe> employees = employeRepository.findAll()
                .stream()
                .filter(e -> e instanceof Employe)
                .map(e -> (Employe) e)
                .toList();

        for (Employe employe : employees) {
            List<SoldeConge> soldes = soldeCongeRepository.findByEmploye(employe);
            List<DemandeConge> demandes = demandeCongeRepository.findDemandeCongeByDemandeur(employe);

            int totalPris = demandes.stream()
                    .filter(d -> d.getDateDebut().getYear() == LocalDate.now().getYear())
                    .mapToInt(DemandeConge::getNombreJours)
                    .sum();

            int totalAlloues = soldes.stream()
                    .filter(s -> s.getAnnee() == LocalDate.now().getYear())
                    .mapToInt(SoldeConge::getSoldeInitial)
                    .sum();

            result.add(new CongesVsAllouesDTO(employe.getNom() + " " + employe.getPreNom(), employe.getManager().getDepartment(), totalPris, totalAlloues));
        }

        return result;
    }

    private List<SoldeCongesDTO> getSoldeConges() {
        List<SoldeCongesDTO> result = new ArrayList<>();
        List<Employe> employees = employeRepository.findAll()
                .stream()
                .filter(e -> e instanceof Employe)
                .map(e -> (Employe) e)
                .toList();
        for (Employe employe : employees) {
            List<SoldeConge> soldes = soldeCongeRepository.findByEmploye(employe);

            int soldeTotal = soldes.stream()
                    .filter(s -> s.getAnnee() == LocalDate.now().getYear())
                    .mapToInt(SoldeConge::getSoldeRestant)
                    .sum();

            result.add(new SoldeCongesDTO(employe.getNom() + " " + employe.getPreNom(), employe.getManager().getDepartment(), soldeTotal));
        }

        return result;
    }

    private List<AbsencesParJourSemaineDTO> getAbsencesParJourSemaine() {
        List<AbsencesParJourSemaineDTO> result = new ArrayList<>();
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};

        int currentYear = LocalDate.now().getYear();
        List<DemandeConge> demandesValidees = demandeCongeRepository.findByStatut(StatutDemande.VALIDEE)
                .stream()
                .filter(d -> d.getDateDebut().getYear() == currentYear)
                .toList();

        for (int i = 0; i < jours.length; i++) {
            String jour = jours[i];
            DayOfWeek dayOfWeek = DayOfWeek.of(i + 1);

            long totalJoursConges = demandesValidees.stream()
                    .mapToLong(demande -> compterJoursCongesPourJourSemaine(
                            demande.getDateDebut(),
                            demande.getDateFin(),
                            dayOfWeek
                    ))
                    .sum();

            result.add(new AbsencesParJourSemaineDTO(jour, (int) totalJoursConges));
        }

        return result;
    }

    private int compterJoursCongesPourJourSemaine(LocalDate dateDebut, LocalDate dateFin, DayOfWeek jourSemaine) {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }

        int count = 0;
        LocalDate current = dateDebut;

        while (!current.isAfter(dateFin)) {

            if (current.getDayOfWeek() == jourSemaine && estJourOuvrable(current)) {
                count++;
            }
            current = current.plusDays(1);
        }

        return count;
    }

    private boolean estJourOuvrable(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Exclure les weekends
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        // logique pour les jours feries

        return true;
    }

    private String getDefaultColor(String typeName) {
        switch (typeName.toLowerCase()) {
            case "congé":
            case "congés payés":
                return "#3B82F6";
            case "rtt":
                return "#10B981";
            case "maladie":
                return "#EF4444";
            case "formation":
                return "#F59E0B";
            default:
                return "#6B7280";
        }
    }

    public List<KPILeavesDTO> getKPILeaves() {
        List<KPILeavesDTO> kpiList = new ArrayList<>();

        // 1. Solde moyen de congés restants
        KPILeavesDTO soldeMoyenKPI = calculerSoldeMoyenConges();
        kpiList.add(soldeMoyenKPI);

        // 2. Périodes de forte demande (mois avec le plus de demandes)
        KPILeavesDTO periodeForteDemandeKPI = calculerPeriodeFortedemande();
        kpiList.add(periodeForteDemandeKPI);

        // 3. Taux de chevauchement des congés par service
        KPILeavesDTO tauxChevauchementKPI = calculerTauxChevauchement();
        kpiList.add(tauxChevauchementKPI);

        // 4. Délai moyen de validation des demandes
        KPILeavesDTO delaiValidationKPI = calculerDelaiMoyenValidation();
        kpiList.add(delaiValidationKPI);

        return kpiList;
    }

    private KPILeavesDTO calculerSoldeMoyenConges() {
        int currentYear = LocalDate.now().getYear();
        List<Employe> employees = employeRepository.findAll()
                .stream()
                .filter(e -> e instanceof Employe)
                .map(e -> (Employe) e)
                .toList();

        if (employees.isEmpty()) {
            return new KPILeavesDTO("Solde moyen de congés", 0.0, " jrs", 0.0, "calendar", "warning");
        }

        double totalSolde = 0;
        int employeesAvecSolde = 0;

        for (Employe employe : employees) {
            List<SoldeConge> soldes = soldeCongeRepository.findByEmploye(employe);
            int soldeTotal = soldes.stream()
                    .filter(s -> s.getAnnee() == currentYear)
                    .mapToInt(SoldeConge::getSoldeRestant)
                    .sum();

            if (soldeTotal > 0) {
                totalSolde += soldeTotal;
                employeesAvecSolde++;
            }
        }

        double soldeMoyen = employeesAvecSolde > 0 ? totalSolde / employeesAvecSolde : 0;

        // Calculer le changement par rapport à l'année précédente
        double soldeMoyenPrecedent = calculerSoldeMoyenAnneePrecedente();
        double changement = soldeMoyenPrecedent > 0 ? ((soldeMoyen - soldeMoyenPrecedent) / soldeMoyenPrecedent) * 100 : 0;

        // Déterminer le statut basé sur le solde moyen
        String status;
        String icon = "calendar-days";
        if (soldeMoyen >= 20) {
            status = "success"; // Vert - bon solde
        } else if (soldeMoyen >= 10) {
            status = "warning"; // Orange - solde moyen
        } else {
            status = "danger"; // Rouge - solde faible
        }

        return new KPILeavesDTO("Solde moyen de congés", Math.round(soldeMoyen * 100.0) / 100.0, "jrs",
                               Math.round(changement * 100.0) / 100.0, icon, status);
    }

    private double calculerSoldeMoyenAnneePrecedente() {
        int previousYear = LocalDate.now().getYear() - 1;
        List<Employe> employees = employeRepository.findAll()
                .stream()
                .filter(e -> e instanceof Employe)
                .map(e -> (Employe) e)
                .toList();

        if (employees.isEmpty()) {
            return 0;
        }

        double totalSolde = 0;
        int employeesAvecSolde = 0;

        for (Employe employe : employees) {
            List<SoldeConge> soldes = soldeCongeRepository.findByEmploye(employe);
            int soldeTotal = soldes.stream()
                    .filter(s -> s.getAnnee() == previousYear)
                    .mapToInt(SoldeConge::getSoldeRestant)
                    .sum();

            if (soldeTotal > 0) {
                totalSolde += soldeTotal;
                employeesAvecSolde++;
            }
        }

        return employeesAvecSolde > 0 ? totalSolde / employeesAvecSolde : 0;
    }

    private KPILeavesDTO calculerPeriodeFortedemande() {
        int currentYear = LocalDate.now().getYear();
        Map<Integer, Integer> demandesParMois = new HashMap<>();

        // Initialiser tous les mois à 0
        for (int i = 1; i <= 12; i++) {
            demandesParMois.put(i, 0);
        }

        List<DemandeConge> demandes = demandeCongeRepository.findAll().stream()
                .filter(d -> d.getDateDebut().getYear() == currentYear)
                .toList();

        // Compter les demandes par mois
        for (DemandeConge demande : demandes) {
            int mois = demande.getDateDebut().getMonthValue();
            demandesParMois.put(mois, demandesParMois.get(mois) + 1);
        }

        // Trouver le mois avec le plus de demandes
        int moisMaxDemandes = demandesParMois.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1);

        int maxDemandes = demandesParMois.get(moisMaxDemandes);

        // Calculer le changement par rapport à l'année précédente
        int demandesMoisPrecedent = calculerDemandesMoisAnneePrecedente(moisMaxDemandes);
        double changement = demandesMoisPrecedent > 0 ? ((double)(maxDemandes - demandesMoisPrecedent) / demandesMoisPrecedent) * 100 : 0;

        // Déterminer le statut
        String status;
        String icon = "chart-line";
        if (maxDemandes <= 5) {
            status = "success"; // Peu de demandes
        } else if (maxDemandes <= 15) {
            status = "warning"; // Demandes modérées
        } else {
            status = "danger"; // Beaucoup de demandes
        }

        String moisNom = LocalDate.of(currentYear, moisMaxDemandes, 1)
                .getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);

        return new KPILeavesDTO("Pic de demandes (" + moisNom + ")", maxDemandes, " demandes",
                               Math.round(changement * 100.0) / 100.0, icon, status);
    }

    private int calculerDemandesMoisAnneePrecedente(int mois) {
        int previousYear = LocalDate.now().getYear() - 1;
        return (int) demandeCongeRepository.findAll().stream()
                .filter(d -> d.getDateDebut().getYear() == previousYear &&
                           d.getDateDebut().getMonthValue() == mois)
                .count();
    }

    private KPILeavesDTO calculerTauxChevauchement() {
        int currentYear = LocalDate.now().getYear();
        List<String> departments = employeRepository.findAllDepartments();

        if (departments.isEmpty()) {
            return new KPILeavesDTO("Taux de chevauchement", 0.0, "%", 0.0, "users", "success");
        }

        double totalTauxChevauchement = 0;
        int departmentsAvecDonnees = 0;

        for (String department : departments) {
            if (department != null) {
                List<Employe> employees = employeRepository.findByDepartement(department);
                List<DemandeConge> congesValidees = new ArrayList<>();

                for (Employe employe : employees) {
                    List<DemandeConge> demandesEmploye = demandeCongeRepository.findDemandeCongeByDemandeur(employe)
                            .stream()
                            .filter(d -> d.getStatut() == StatutDemande.VALIDEE &&
                                       d.getDateDebut().getYear() == currentYear)
                            .toList();
                    congesValidees.addAll(demandesEmploye);
                }

                if (!congesValidees.isEmpty()) {
                    double tauxDepartement = calculerTauxChevauchementDepartement(congesValidees, employees.size());
                    totalTauxChevauchement += tauxDepartement;
                    departmentsAvecDonnees++;
                }
            }
        }

        double tauxMoyen = departmentsAvecDonnees > 0 ? totalTauxChevauchement / departmentsAvecDonnees : 0;

        double tauxPrecedent = calculerTauxChevauchementAnneePrecedente();
        double changement = tauxPrecedent > 0 ? ((tauxMoyen - tauxPrecedent) / tauxPrecedent) * 100 : 0;

        String status;
        String icon = "users";
        if (tauxMoyen <= 10) {
            status = "success";
        } else if (tauxMoyen <= 25) {
            status = "warning";
        } else {
            status = "danger";
        }

        return new KPILeavesDTO("Taux de chevauchement", Math.round(tauxMoyen * 100.0) / 100.0, "%",
                               Math.round(changement * 100.0) / 100.0, icon, status);
    }

    private double calculerTauxChevauchementDepartement(List<DemandeConge> conges, int nombreEmployes) {
        if (conges.size() <= 1 || nombreEmployes <= 1) {
            return 0.0;
        }

        int chevauchements = 0;
        int totalPaires = 0;

        for (int i = 0; i < conges.size(); i++) {
            for (int j = i + 1; j < conges.size(); j++) {
                DemandeConge conge1 = conges.get(i);
                DemandeConge conge2 = conges.get(j);

                // Vérifier si les congés se chevauchent
                if (congesSeChevauche(conge1, conge2)) {
                    chevauchements++;
                }
                totalPaires++;
            }
        }

        return totalPaires > 0 ? (double) chevauchements / totalPaires * 100 : 0.0;
    }

    private boolean congesSeChevauche(DemandeConge conge1, DemandeConge conge2) {
        return !conge1.getDateFin().isBefore(conge2.getDateDebut()) &&
               !conge2.getDateFin().isBefore(conge1.getDateDebut());
    }

    private double calculerTauxChevauchementAnneePrecedente() {
        int previousYear = LocalDate.now().getYear() - 1;
        List<String> departments = employeRepository.findAllDepartments();

        if (departments.isEmpty()) {
            return 0.0;
        }

        double totalTauxChevauchement = 0;
        int departmentsAvecDonnees = 0;

        for (String department : departments) {
            if (department != null) {
                List<Employe> employees = employeRepository.findByDepartement(department);
                List<DemandeConge> congesValidees = new ArrayList<>();

                // Récupérer tous les congés validés du département pour l'année précédente
                for (Employe employe : employees) {
                    List<DemandeConge> demandesEmploye = demandeCongeRepository.findDemandeCongeByDemandeur(employe)
                            .stream()
                            .filter(d -> d.getStatut() == StatutDemande.VALIDEE &&
                                       d.getDateDebut().getYear() == previousYear)
                            .collect(Collectors.toList());
                    congesValidees.addAll(demandesEmploye);
                }

                if (!congesValidees.isEmpty()) {
                    double tauxDepartement = calculerTauxChevauchementDepartement(congesValidees, employees.size());
                    totalTauxChevauchement += tauxDepartement;
                    departmentsAvecDonnees++;
                }
            }
        }

        return departmentsAvecDonnees > 0 ? totalTauxChevauchement / departmentsAvecDonnees : 0.0;
    }

    private KPILeavesDTO calculerDelaiMoyenValidation() {
        int currentYear = LocalDate.now().getYear();

        List<DemandeConge> demandesValidees = demandeCongeRepository.findAll().stream()
                .filter(d -> d.getStatut() == StatutDemande.VALIDEE &&
                           d.getDateCreation() != null &&
                           d.getDateTraitement() != null &&
                           d.getDateCreation().getYear() == currentYear)
                .collect(Collectors.toList());

        if (demandesValidees.isEmpty()) {
            return new KPILeavesDTO("Délai moyen validation", 0.0, "%", 0.0, "clock", "warning");
        }

        double totalDelais = 0;
        for (DemandeConge demande : demandesValidees) {
            long delaiJours = java.time.Duration.between(
                    demande.getDateCreation(),
                    demande.getDateTraitement()
            ).toDays();
            totalDelais += delaiJours;
        }

        double delaiMoyen = totalDelais / demandesValidees.size();

        // Convertir en pourcentage de performance (plus c'est rapide, plus le pourcentage est élevé)
        // Délai idéal = 1 jour, délai maximum acceptable = 7 jours
        double pourcentagePerformance = Math.max(0, Math.min(100, (8 - delaiMoyen) / 7 * 100));

        // Calculer le changement par rapport à l'année précédente
        double delaiPrecedent = calculerDelaiMoyenAnneePrecedente();
        double changement = 0.0;

        if (delaiPrecedent > 0) {
            // Calculer le changement en pourcentage de performance
            double pourcentagePrecedent = Math.max(0, Math.min(100, (8 - delaiPrecedent) / 7 * 100));
            changement = pourcentagePerformance - pourcentagePrecedent;
        }

        // Déterminer le statut basé sur le pourcentage de performance
        String status;
        String icon = "clock";
        if (pourcentagePerformance >= 80) {
            status = "success"; // Validation rapide (≤2 jours)
        } else if (pourcentagePerformance >= 50) {
            status = "warning"; // Validation modérée (3-5 jours)
        } else {
            status = "danger"; // Validation lente (>5 jours)
        }

        return new KPILeavesDTO("Délai moyen validation", Math.round(pourcentagePerformance * 100.0) / 100.0, "%",
                               Math.round(changement * 100.0) / 100.0, icon, status);
    }

    private double calculerDelaiMoyenAnneePrecedente() {
        int previousYear = LocalDate.now().getYear() - 1;

        List<DemandeConge> demandesValidees = demandeCongeRepository.findAll().stream()
                .filter(d -> d.getStatut() == StatutDemande.VALIDEE &&
                           d.getDateCreation() != null &&
                           d.getDateTraitement() != null &&
                           d.getDateCreation().getYear() == previousYear)
                .collect(Collectors.toList());

        if (demandesValidees.isEmpty()) {
            return 0.0; // Retourner 0 s'il n'y a pas de données précédentes
        }

        double totalDelais = 0;
        for (DemandeConge demande : demandesValidees) {
            long delaiJours = java.time.Duration.between(
                    demande.getDateCreation(),
                    demande.getDateTraitement()
            ).toDays();
            totalDelais += delaiJours;
        }

        return totalDelais / demandesValidees.size();
    }
}
