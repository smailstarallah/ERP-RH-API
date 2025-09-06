package ma.digitalia.generationfichepaie.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.generationfichepaie.Enum.TypeElement;
import ma.digitalia.generationfichepaie.repositories.ElementPaieRepository;
import ma.digitalia.generationfichepaie.repositories.FichePaieRepository;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardCalculationService {

    private final FichePaieRepository fichePaieRepository;
    private final ElementPaieRepository elementPaieRepository;
    private final EmployeRepository employeRepository;

    public BigDecimal calculateMasseSalariale(YearMonth periode) {
        try {
            BigDecimal masse = fichePaieRepository.getMasseSalarialeByPeriode(periode);
            return masse != null ? masse : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Erreur lors du calcul de la masse salariale pour la période {}: {}", periode, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateMasseSalarialeEvolution(YearMonth currentPeriod, YearMonth previousPeriod) {
        try {
            BigDecimal currentMasse = calculateMasseSalariale(currentPeriod);
            BigDecimal previousMasse = calculateMasseSalariale(previousPeriod);

            if (previousMasse.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            return currentMasse.subtract(previousMasse)
                    .divide(previousMasse, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Erreur lors du calcul de l'évolution de la masse salariale: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateTauxErreur(YearMonth periode) {
        try {
            long totalFiches = fichePaieRepository.countTotalFichesByPeriode(periode);
            long fichesErronees = fichePaieRepository.countFichesErroneesByPeriode(periode);
            long elementsErronees = elementPaieRepository.countElementsErroneesByPeriode(periode);

            if (totalFiches == 0) {
                return BigDecimal.ZERO;
            }

            return BigDecimal.valueOf(fichesErronees + elementsErronees)
                    .divide(BigDecimal.valueOf(totalFiches), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Erreur lors du calcul du taux d'erreur pour la période {}: {}", periode, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateCoutParBulletin(YearMonth periode) {
        try {
            long totalFiches = fichePaieRepository.countTotalFichesByPeriode(periode);
            if (totalFiches == 0) {
                return BigDecimal.ZERO;
            }

            // Estimation du coût fixe par bulletin (temps de traitement, resources IT, etc.)
            BigDecimal coutFixeParBulletin = BigDecimal.valueOf(8.50);

            // Ajouter le coût des erreurs (temps de correction)
            BigDecimal tauxErreur = calculateTauxErreur(periode);
            BigDecimal coutErreur = tauxErreur.multiply(BigDecimal.valueOf(0.15));

            return coutFixeParBulletin.add(coutErreur).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Erreur lors du calcul du coût par bulletin pour la période {}: {}", periode, e.getMessage());
            return BigDecimal.valueOf(12.50);
        }
    }

    public BigDecimal calculatePourcentageCA(YearMonth periode) {
        try {
            // Pour le calcul du pourcentage du CA, nous aurions besoin du CA total
            // En attendant cette donnée, nous utilisons une estimation basée sur la masse salariale
            BigDecimal masseSalariale = calculateMasseSalariale(periode);

            // Estimation: la masse salariale représente généralement 25-35% du CA
            // Nous utilisons 30% comme référence
            BigDecimal estimatedCA = masseSalariale.divide(BigDecimal.valueOf(0.30), 2, RoundingMode.HALF_UP);

            if (estimatedCA.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            return masseSalariale.divide(estimatedCA, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Erreur lors du calcul du pourcentage CA pour la période {}: {}", periode, e.getMessage());
            return BigDecimal.valueOf(28.5);
        }
    }

    public List<Object[]> getSalaryEvolutionData(YearMonth startPeriod, YearMonth endPeriod) {
        try {
            return fichePaieRepository.getMasseSalarialeEvolution(startPeriod, endPeriod);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'évolution salariale: {}", e.getMessage());
            return List.of();
        }
    }

    public List<Object[]> getSalaryDistributionData(YearMonth periode) {
        try {
            return fichePaieRepository.getSalaryDistributionByPoste(periode);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la distribution salariale: {}", e.getMessage());
            return List.of();
        }
    }

    public List<Object[]> getSalaryDistributionByDepartment(YearMonth periode) {
        try {
            return employeRepository.getSalaryDistributionByDepartment(periode);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la distribution salariale par département: {}", e.getMessage());
            return List.of();
        }
    }

    public List<Object[]> getSalariesByDepartmentAndPeriode(String departement, YearMonth periode) {
        try {
            return fichePaieRepository.getSalariesByDepartmentAndPeriode(departement, periode);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des salaires par département {} et période {}: {}", departement, periode, e.getMessage());
            return List.of();
        }
    }

    public int countEmployeesBySalaryRangeAndCategory(BigDecimal min, BigDecimal max, YearMonth periode, String category) {
        try {
            return fichePaieRepository.countBySalaireRangeAndPoste(min, max, periode, category);
        } catch (Exception e) {
            log.error("Erreur lors du comptage des employés par tranche salariale: {}", e.getMessage());
            return 0;
        }
    }

    public List<Object[]> getVariableElementsData(YearMonth periode) {
        try {
            return elementPaieRepository.getVariableElementsByTypeAndPeriode(TypeElement.PRIME_FIXE, periode);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des éléments variables: {}", e.getMessage());
            return List.of();
        }
    }

    public BigDecimal calculateAverageSalary(YearMonth periode) {
        try {
            BigDecimal avg = fichePaieRepository.getAvgSalaireBrutByPeriode(periode);
            return avg != null ? avg : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Erreur lors du calcul du salaire moyen: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public long getTotalEmployeesCount() {
        try {
            return employeRepository.count();
        } catch (Exception e) {
            log.error("Erreur lors du comptage des employés: {}", e.getMessage());
            return 0;
        }
    }
}
