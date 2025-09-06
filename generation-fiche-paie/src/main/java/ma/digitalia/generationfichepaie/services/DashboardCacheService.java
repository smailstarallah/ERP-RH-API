package ma.digitalia.generationfichepaie.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.concurrent.CompletableFuture;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardCacheService {

    private final DashboardCalculationService calculationService;

    @Cacheable(value = "masseSalariale", key = "#periode.toString()")
    public BigDecimal getCachedMasseSalariale(YearMonth periode) {
        log.debug("Calcul et mise en cache de la masse salariale pour {}", periode);
        return calculationService.calculateMasseSalariale(periode);
    }

    @Cacheable(value = "tauxErreur", key = "#periode.toString()")
    public BigDecimal getCachedTauxErreur(YearMonth periode) {
        log.debug("Calcul et mise en cache du taux d'erreur pour {}", periode);
        return calculationService.calculateTauxErreur(periode);
    }

    @Cacheable(value = "salaryEvolution", key = "#startPeriod.toString() + '_' + #endPeriod.toString()")
    public List<Object[]> getCachedSalaryEvolution(YearMonth startPeriod, YearMonth endPeriod) {
        log.debug("Calcul et mise en cache de l'évolution salariale de {} à {}", startPeriod, endPeriod);
        return calculationService.getSalaryEvolutionData(startPeriod, endPeriod);
    }

    @CacheEvict(value = {"masseSalariale", "tauxErreur", "salaryEvolution"}, allEntries = true)
    public void evictAllCaches() {
        log.info("Nettoyage de tous les caches du dashboard");
    }

    @CacheEvict(value = "masseSalariale", key = "#periode.toString()")
    public void evictMasseSalarialeCache(YearMonth periode) {
        log.debug("Nettoyage du cache masse salariale pour {}", periode);
    }

    /**
     * Calcul asynchrone des métriques pour améliorer les performances
     */
    public CompletableFuture<BigDecimal> calculateMasseSalarialeAsync(YearMonth periode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getCachedMasseSalariale(periode);
            } catch (Exception e) {
                log.error("Erreur lors du calcul asynchrone de la masse salariale: {}", e.getMessage());
                return BigDecimal.ZERO;
            }
        });
    }

    public CompletableFuture<BigDecimal> calculateTauxErreurAsync(YearMonth periode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getCachedTauxErreur(periode);
            } catch (Exception e) {
                log.error("Erreur lors du calcul asynchrone du taux d'erreur: {}", e.getMessage());
                return BigDecimal.ZERO;
            }
        });
    }
}
