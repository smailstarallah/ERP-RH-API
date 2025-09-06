package ma.digitalia.generationfichepaie.services;

import lombok.extern.slf4j.Slf4j;
import ma.digitalia.generationfichepaie.dto.dashboard.*;
import ma.digitalia.generationfichepaie.repositories.ElementPaieRepository;
import ma.digitalia.generationfichepaie.repositories.FichePaieRepository;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    private final ElementPaieRepository elementPaieRepository;
    private final FichePaieRepository fichePaieRepository;
    private final EmployeRepository employeRepository;
    private final DashboardCalculationService calculationService;

    public DashboardServiceImpl(ElementPaieRepository elementPaieRepository,
                                FichePaieRepository fichePaieRepository,
                                EmployeRepository employeRepository,
                                DashboardCalculationService calculationService) {
        this.elementPaieRepository = elementPaieRepository;
        this.fichePaieRepository = fichePaieRepository;
        this.employeRepository = employeRepository;
        this.calculationService = calculationService;
    }

    @Override
    public DashboardResponseDto getDashboardData() {
        log.info("Récupération des données dynamiques pour le tableau de bord des fiches de paie");

        try {
            YearMonth currentPeriod = YearMonth.now();
            YearMonth previousPeriod = currentPeriod.minusMonths(1);

            return DashboardResponseDto.builder()
                    .kpis(getKpisDynamiques(currentPeriod, previousPeriod))
                    .salaryDistribution(getSalaryDistributionDynamique(currentPeriod))
                    .salaryEvolution(getSalaryEvolutionDynamique())
                    .salaryStructure(getSalaryStructureDynamique(currentPeriod))
                    .payrollQuality(getPayrollQualityDynamique())
                    .variableElements(getVariableElementsDynamiques(currentPeriod))
                    .complianceActions(getComplianceActionsDynamiques(currentPeriod))
                    .lastUpdate(LocalDateTime.now())
                    .status("success")
                    .build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données du tableau de bord: {}", e.getMessage(), e);
            return DashboardResponseDto.builder()
                    .status("error")
                    .lastUpdate(LocalDateTime.now())
                    .build();
        }
    }

    private KpisDto getKpisDynamiques(YearMonth currentPeriod, YearMonth previousPeriod) {
        log.debug("Calcul des KPIs dynamiques pour la période {}", currentPeriod);

        try {
            // Calcul de la masse salariale actuelle
            BigDecimal masseSalariale = calculationService.calculateMasseSalariale(currentPeriod);
            BigDecimal evolutionMasse = calculationService.calculateMasseSalarialeEvolution(currentPeriod, previousPeriod);

            // Calcul du pourcentage du CA
            BigDecimal pourcentageCA = calculationService.calculatePourcentageCA(currentPeriod);
            BigDecimal evolutionCA = calculationService.calculatePourcentageCA(previousPeriod);
            BigDecimal changeCA = pourcentageCA.subtract(evolutionCA);

            // Calcul du taux d'erreur
            BigDecimal tauxErreur = calculationService.calculateTauxErreur(currentPeriod);
            BigDecimal tauxErreurPrecedent = calculationService.calculateTauxErreur(previousPeriod);
            BigDecimal changeTauxErreur = tauxErreur.subtract(tauxErreurPrecedent);

            // Calcul du coût par bulletin
            BigDecimal coutBulletin = calculationService.calculateCoutParBulletin(currentPeriod);
            BigDecimal coutBulletinPrecedent = calculationService.calculateCoutParBulletin(previousPeriod);
            BigDecimal changeCout = coutBulletin.subtract(coutBulletinPrecedent);

            return KpisDto.builder()
                    .masseSalariale(KpiValueDto.builder()
                            .value(masseSalariale.doubleValue())
                            .change(evolutionMasse.doubleValue())
                            .build())
                    .pourcentageCA(KpiValueDto.builder()
                            .value(pourcentageCA.doubleValue())
                            .change(changeCA.doubleValue())
                            .build())
                    .tauxErreur(KpiValueDto.builder()
                            .value(tauxErreur.doubleValue())
                            .change(changeTauxErreur.doubleValue())
                            .build())
                    .coutBulletin(KpiValueDto.builder()
                            .value(coutBulletin.doubleValue())
                            .change(changeCout.doubleValue())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Erreur lors du calcul des KPIs: {}", e.getMessage());
            return getKpisDefault();
        }
    }

    private List<SalaryStructureDto> getSalaryStructureDynamique(YearMonth periode) {
        log.debug("Calcul de la structure salariale pour la période {}", periode);

        List<SalaryStructureDto> structure = new ArrayList<>();

        try {
            BigDecimal salaireBrut = calculationService.calculateMasseSalariale(periode);
            BigDecimal cotisationsPatronales = fichePaieRepository.getCotisationsPatronalesByPeriode(periode);
            BigDecimal cotisationsSalariales = fichePaieRepository.getCotisationsSalarialesByPeriode(periode);
            BigDecimal salaireNet = fichePaieRepository.getSalaireNetByPeriode(periode);
            BigDecimal impotSurLeRevenu = fichePaieRepository.getImpotSurLeRevenuByPeriode(periode);
            BigDecimal salaireNetImposable = fichePaieRepository.getSalaireNetImposableByPeriode(periode);
            BigDecimal salaireBrutImposable = fichePaieRepository.getSalaireBrutImposableByPeriode(periode);

            // Valeurs par défaut
            if (salaireBrut == null) salaireBrut = BigDecimal.ZERO;
            if (cotisationsPatronales == null) cotisationsPatronales = BigDecimal.ZERO;
            if (cotisationsSalariales == null) cotisationsSalariales = BigDecimal.ZERO;
            if (salaireNet == null) salaireNet = BigDecimal.ZERO;
            if (impotSurLeRevenu == null) impotSurLeRevenu = BigDecimal.ZERO;
            if (salaireNetImposable == null) salaireNetImposable = BigDecimal.ZERO;

            // Calcul du coût total employeur pour les pourcentages
            BigDecimal coutTotalEmployeur = salaireBrut.add(cotisationsPatronales);

            if (coutTotalEmployeur.compareTo(BigDecimal.ZERO) > 0) {

                // 1. Salaire brut
                if (salaireBrut.compareTo(BigDecimal.ZERO) > 0) {
                    double pourcentageSalaireBrut = salaireBrut.divide(coutTotalEmployeur, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    structure.add(SalaryStructureDto.builder()
                            .category("Salaire brut")
                            .montant(salaireBrut.longValue())
                            .pourcentage((int) Math.round(pourcentageSalaireBrut))
                            .build());
                }

                // 2. Charges patronales
                if (cotisationsPatronales.compareTo(BigDecimal.ZERO) > 0) {
                    double pourcentageCotisationsPatronales = cotisationsPatronales.divide(coutTotalEmployeur, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    structure.add(SalaryStructureDto.builder()
                            .category("Charges patronales")
                            .montant(cotisationsPatronales.longValue())
                            .pourcentage((int) Math.round(pourcentageCotisationsPatronales))
                            .build());
                }

                // 3. Charges salariales (% du brut)
                if (cotisationsSalariales.compareTo(BigDecimal.ZERO) > 0 && salaireBrut.compareTo(BigDecimal.ZERO) > 0) {
                    double pourcentageCotisationsSalariales = cotisationsSalariales.divide(salaireBrut, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    structure.add(SalaryStructureDto.builder()
                            .category("Charges salariales")
                            .montant(cotisationsSalariales.longValue())
                            .pourcentage((int) Math.round(pourcentageCotisationsSalariales))
                            .build());
                }

                // 5. Impôt sur le revenu (% du net imposable)
                if (impotSurLeRevenu.compareTo(BigDecimal.ZERO) > 0 && salaireNetImposable.compareTo(BigDecimal.ZERO) > 0) {
                    double pourcentageImpot = impotSurLeRevenu.divide(salaireNetImposable, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    structure.add(SalaryStructureDto.builder()
                            .category("Impôt sur le revenu")
                            .montant(impotSurLeRevenu.longValue())
                            .pourcentage((int) Math.round(pourcentageImpot))
                            .build());
                }
            }



            return structure.isEmpty() ? getSalaryStructureDefault() : structure;
        } catch (Exception e) {
            log.error("Erreur lors du calcul de la structure salariale: {}", e.getMessage());
            return getSalaryStructureDefault();
        }
    }

    private List<SalaryEvolutionDto> getSalaryEvolutionDynamique() {
        log.debug("Calcul de l'évolution salariale sur 12 mois");

        List<SalaryEvolutionDto> evolution = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH);
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(11);

        try {
            List<Object[]> evolutionData = calculationService.getSalaryEvolutionData(startMonth, currentMonth);
            long totalEmployees = calculationService.getTotalEmployeesCount();

            for (Object[] data : evolutionData) {
                YearMonth periode = (YearMonth) data[0];
                BigDecimal masse = data[1] != null ? (BigDecimal) data[1] : BigDecimal.ZERO;
                Long count = data[2] != null ? (Long) data[2] : 0L;

                // Budget estimé (masse salariale + 10% de marge)
                BigDecimal budget = masse.multiply(BigDecimal.valueOf(1.10));

                // Coût par employé
                BigDecimal coutParEmploye = totalEmployees > 0 ?
                        masse.divide(BigDecimal.valueOf(totalEmployees), 0, RoundingMode.HALF_UP) :
                        BigDecimal.ZERO;

                evolution.add(SalaryEvolutionDto.builder()
                        .month(periode.format(formatter))
                        .masseSalariale(masse.longValue())
                        .budget(budget.longValue())
                        .coutParEmploye(coutParEmploye.longValue())
                        .build());
            }

            return evolution.isEmpty() ? getSalaryEvolutionDefault() : evolution;
        } catch (Exception e) {
            log.error("Erreur lors du calcul de l'évolution salariale: {}", e.getMessage());
            return getSalaryEvolutionDefault();
        }
    }

    private SalaryRangeDto calculerTranchesSalariales(String departement, YearMonth periode) {
        try {
            // Récupérer tous les salaires du département pour la période
            List<Object[]> salariesData = calculationService.getSalariesByDepartmentAndPeriode(departement, periode);

            int moins5000 = 0;
            int entre5000_8000 = 0;
            int entre8000_12000 = 0;
            int entre12000_20000 = 0;
            int plus20000 = 0;

            for (Object[] salaryData : salariesData) {
                BigDecimal salaire = (BigDecimal) salaryData[0]; // Le salaire brut
                double salaireMontant = salaire != null ? salaire.doubleValue() : 0.0;

                if (salaireMontant < 5000) {
                    moins5000++;
                } else if (salaireMontant >= 5000 && salaireMontant < 8000) {
                    entre5000_8000++;
                } else if (salaireMontant >= 8000 && salaireMontant < 12000) {
                    entre8000_12000++;
                } else if (salaireMontant >= 12000 && salaireMontant < 20000) {
                    entre12000_20000++;
                } else {
                    plus20000++;
                }
            }

            return SalaryRangeDto.builder()
                    .moins_5000(moins5000)
                    ._5000_8000(entre5000_8000)
                    ._8000_12000(entre8000_12000)
                    ._12000_20000(entre12000_20000)
                    .plus_20000(plus20000)
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors du calcul des tranches salariales pour le département {}: {}", departement, e.getMessage());
            return SalaryRangeDto.builder()
                    .moins_5000(0)
                    ._5000_8000(0)
                    ._8000_12000(0)
                    ._12000_20000(0)
                    .plus_20000(0)
                    .build();
        }
    }

    private List<SalaryDistributionDto> getSalaryDistributionDynamique(YearMonth periode) {
        log.debug("Calcul de la distribution salariale par département pour la période {}", periode);

        List<SalaryDistributionDto> distribution = new ArrayList<>();

        try {
            // Récupérer les données par département depuis la base de données
            List<Object[]> departmentData = calculationService.getSalaryDistributionByDepartment(periode);

            for (Object[] data : departmentData) {
                String departement = (String) data[0];
                Long nombreEmployes = data[1] != null ? (Long) data[1] : 0L;
                BigDecimal masseSalariale = data[2] != null ? (BigDecimal) data[2] : BigDecimal.ZERO;

                // Calcul du salaire moyen
                double salaireMoyen = nombreEmployes > 0 ?
                    masseSalariale.divide(BigDecimal.valueOf(nombreEmployes), 2, RoundingMode.HALF_UP).doubleValue() : 0.0;

                // Récupérer les données détaillées par tranche pour ce département
                SalaryRangeDto tranches = calculerTranchesSalariales(departement, periode);

                distribution.add(SalaryDistributionDto.builder()
                        .departement(departement != null ? departement : "Département non défini")
                        .nombreEmployes(nombreEmployes.intValue())
                        .masseSalariale(masseSalariale.doubleValue())
                        .salaireMoyen(salaireMoyen)
                        .tranches(tranches)
                        .build());
            }

            return distribution.isEmpty() ? getSalaryDistributionDefault() : distribution;
        } catch (Exception e) {
            log.error("Erreur lors du calcul de la distribution salariale par département: {}", e.getMessage());
            return getSalaryDistributionDefault();
        }
    }

    private List<PayrollQualityDto> getPayrollQualityDynamique() {
        log.debug("Calcul de la qualité de la paie sur les 6 derniers mois");

        List<PayrollQualityDto> quality = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH);

        try {
            for (int i = 5; i >= 0; i--) {
                YearMonth periode = currentMonth.minusMonths(i);
                BigDecimal tauxErreur = calculationService.calculateTauxErreur(periode);

                // Estimation du temps de traitement basé sur le nombre de fiches et le taux d'erreur
                long totalFiches = fichePaieRepository.countTotalFichesByPeriode(periode);
                int tempsBase = 35; // minutes de base par lot de 100 fiches
                int tempsSupplementaire = tauxErreur.multiply(BigDecimal.valueOf(10)).intValue();
                int tempsTraitement = tempsBase + tempsSupplementaire + (int)(totalFiches / 100) * 5;

                quality.add(PayrollQualityDto.builder()
                        .month(periode.format(formatter))
                        .tauxErreur(tauxErreur.doubleValue())
                        .tempsTraitement(tempsTraitement)
                        .build());
            }

            return quality.isEmpty() ? getPayrollQualityDefault() : quality;
        } catch (Exception e) {
            log.error("Erreur lors du calcul de la qualité de la paie: {}", e.getMessage());
            return getPayrollQualityDefault();
        }
    }

    private List<VariableElementDto> getVariableElementsDynamiques(YearMonth periode) {
        log.debug("Calcul des éléments variables pour la période {}", periode);

        List<VariableElementDto> elements = new ArrayList<>();

        try {
            List<Object[]> variableData = calculationService.getVariableElementsData(periode);

            for (Object[] data : variableData) {
                String sousType = (String) data[0];
                BigDecimal montantConsomme = data[1] != null ? (BigDecimal) data[1] : BigDecimal.ZERO;
                Long count = data[2] != null ? (Long) data[2] : 0L;

                // Budget estimé (consommé + 20% de marge)
                BigDecimal budget = montantConsomme.multiply(BigDecimal.valueOf(1.20));
                double taux = budget.compareTo(BigDecimal.ZERO) > 0 ?
                        montantConsomme.divide(budget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0;

                String impact = taux > 90 ? "Dépassement" : taux > 70 ? "Conforme" : "Sous-consommé";

                elements.add(VariableElementDto.builder()
                        .element(sousType != null ? sousType : "Prime générale")
                        .budget(budget.longValue())
                        .consomme(montantConsomme.longValue())
                        .taux(taux)
                        .impact(impact)
                        .build());
            }

            return elements.isEmpty() ? getVariableElementsDefault() : elements;
        } catch (Exception e) {
            log.error("Erreur lors du calcul des éléments variables: {}", e.getMessage());
            return getVariableElementsDefault();
        }
    }

    private List<ComplianceActionDto> getComplianceActionsDynamiques(YearMonth periode) {
        log.debug("Génération des actions de conformité pour la période {}", periode);

        List<ComplianceActionDto> actions = new ArrayList<>();

        try {
            // Vérification du taux d'erreur
            BigDecimal tauxErreur = calculationService.calculateTauxErreur(periode);
            if (tauxErreur.compareTo(BigDecimal.valueOf(2.0)) > 0) {
                actions.add(ComplianceActionDto.builder()
                        .id("ERR_001")
                        .title("Taux d'erreur élevé détecté")
                        .description(String.format("Le taux d'erreur actuel (%.2f%%) dépasse le seuil acceptable de 2%%", tauxErreur.doubleValue()))
                        .priority("urgent")
                        .deadline(periode.plusMonths(1).atDay(15).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .actions(Arrays.asList(
                                "Analyser les sources d'erreur principales",
                                "Renforcer les contrôles qualité",
                                "Former les équipes sur les processus"
                        ))
                        .build());
            }

            // Vérification des déclarations mensuelles
            actions.add(ComplianceActionDto.builder()
                    .id("DECL_001")
                    .title("Déclarations sociales à effectuer")
                    .description("Déclarations URSSAF et autres organismes pour le mois en cours")
                    .priority("normal")
                    .deadline(periode.plusMonths(1).atDay(15).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .actions(Arrays.asList(
                            "Préparer les déclarations URSSAF",
                            "Vérifier les montants des cotisations",
                            "Effectuer les virements avant échéance"
                    ))
                    .build());

            // Vérification de la masse salariale
            BigDecimal masseSalariale = calculationService.calculateMasseSalariale(periode);
            BigDecimal massePrecedente = calculationService.calculateMasseSalariale(periode.minusMonths(1));

            if (masseSalariale.compareTo(massePrecedente.multiply(BigDecimal.valueOf(1.15))) > 0) {
                actions.add(ComplianceActionDto.builder()
                        .id("MASSE_001")
                        .title("Augmentation significative de la masse salariale")
                        .description("La masse salariale a augment�� de plus de 15% par rapport au mois précédent")
                        .priority("attention")
                        .deadline(periode.plusMonths(1).atDay(10).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .actions(Arrays.asList(
                                "Analyser les causes de l'augmentation",
                                "Vérifier la cohérence des calculs",
                                "Ajuster les budgets prévisionnels"
                        ))
                        .build());
            }

            return actions;
        } catch (Exception e) {
            log.error("Erreur lors de la génération des actions de conformit��: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // Méthodes pour les données par défaut en cas d'erreur
    private KpisDto getKpisDefault() {
        return KpisDto.builder()
                .masseSalariale(KpiValueDto.builder().value(0).change(0).build())
                .pourcentageCA(KpiValueDto.builder().value(0).change(0).build())
                .tauxErreur(KpiValueDto.builder().value(0).change(0).build())
                .coutBulletin(KpiValueDto.builder().value(0).change(0).build())
                .build();
    }

    private List<SalaryStructureDto> getSalaryStructureDefault() {
        return Arrays.asList(
                SalaryStructureDto.builder()
                        .category("Aucune donnée")
                        .montant(0)
                        .pourcentage(0)
                        .build()
        );
    }

    private List<SalaryEvolutionDto> getSalaryEvolutionDefault() {
        return Arrays.asList(
                SalaryEvolutionDto.builder()
                        .month("N/A")
                        .masseSalariale(0)
                        .budget(0)
                        .coutParEmploye(0)
                        .build()
        );
    }

    private List<SalaryDistributionDto> getSalaryDistributionDefault() {
        return Arrays.asList(
                SalaryDistributionDto.builder()
                        .departement("Développement")
                        .nombreEmployes(25)
                        .masseSalariale(375000.0)
                        .salaireMoyen(15000.0)
                        .tranches(SalaryRangeDto.builder()
                                .moins_5000(0)
                                ._5000_8000(3)
                                ._8000_12000(8)
                                ._12000_20000(12)
                                .plus_20000(2)
                                .build())
                        .build(),
                SalaryDistributionDto.builder()
                        .departement("Ressources Humaines")
                        .nombreEmployes(11)
                        .masseSalariale(110000.0)
                        .salaireMoyen(10000.0)
                        .tranches(SalaryRangeDto.builder()
                                .moins_5000(2)
                                ._5000_8000(5)
                                ._8000_12000(3)
                                ._12000_20000(1)
                                .plus_20000(0)
                                .build())
                        .build(),
                SalaryDistributionDto.builder()
                        .departement("Commercial")
                        .nombreEmployes(14)
                        .masseSalariale(168000.0)
                        .salaireMoyen(12000.0)
                        .tranches(SalaryRangeDto.builder()
                                .moins_5000(1)
                                ._5000_8000(6)
                                ._8000_12000(4)
                                ._12000_20000(2)
                                .plus_20000(1)
                                .build())
                        .build()
        );
    }

    private List<PayrollQualityDto> getPayrollQualityDefault() {
        return Arrays.asList(
                PayrollQualityDto.builder()
                        .month("N/A")
                        .tauxErreur(0.0)
                        .tempsTraitement(0)
                        .build()
        );
    }

    private List<VariableElementDto> getVariableElementsDefault() {
        return Arrays.asList(
                VariableElementDto.builder()
                        .element("Aucune donnée")
                        .budget(0)
                        .consomme(0)
                        .taux(0)
                        .impact("N/A")
                        .build()
        );
    }
}
