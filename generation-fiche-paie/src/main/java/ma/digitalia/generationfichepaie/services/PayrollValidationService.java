package ma.digitalia.generationfichepaie.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.generationfichepaie.entities.FichePaie;
import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.generationfichepaie.repositories.FichePaieRepository;
import ma.digitalia.generationfichepaie.repositories.ElementPaieRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollValidationService {

    private final FichePaieRepository fichePaieRepository;
    private final ElementPaieRepository elementPaieRepository;

    /**
     * Valide la cohérence des fiches de paie pour une période donnée
     */
    public ValidationResult validatePayrollData(YearMonth periode) {
        log.debug("Validation des données de paie pour la période {}", periode);

        ValidationResult result = new ValidationResult();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Récupération des fiches de paie pour la période
            List<FichePaie> fiches = fichePaieRepository.findAll().stream()
                    .filter(fp -> fp.getPeriode().equals(periode))
                    .toList();

            if (fiches.isEmpty()) {
                warnings.add("Aucune fiche de paie trouvée pour la période " + periode);
                result.setValid(false);
                result.setErrors(errors);
                result.setWarnings(warnings);
                return result;
            }

            // Validation des montants
            validateSalaryAmounts(fiches, errors, warnings, metrics);

            // Validation des cotisations
            validateContributions(fiches, errors, warnings, metrics);

            // Validation des éléments de paie
            validatePayrollElements(fiches, errors, warnings, metrics);

            // Validation de la cohérence temporelle
            validateTemporalConsistency(fiches, errors, warnings, metrics);

            // Calcul des métriques de qualité
            calculateQualityMetrics(fiches, metrics);

            result.setValid(errors.isEmpty());
            result.setErrors(errors);
            result.setWarnings(warnings);
            result.setMetrics(metrics);

        } catch (Exception e) {
            log.error("Erreur lors de la validation des données de paie: {}", e.getMessage(), e);
            errors.add("Erreur technique lors de la validation: " + e.getMessage());
            result.setValid(false);
            result.setErrors(errors);
        }

        return result;
    }

    private void validateSalaryAmounts(List<FichePaie> fiches, List<String> errors,
                                     List<String> warnings, Map<String, Object> metrics) {
        int invalidSalaries = 0;
        BigDecimal totalBrut = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (FichePaie fiche : fiches) {
            // Vérification que le salaire brut est positif
            if (fiche.getSalaireBrut() == null || fiche.getSalaireBrut().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Fiche ID " + fiche.getId() + ": Salaire brut invalide");
                invalidSalaries++;
                continue;
            }

            // Vérification que le salaire net est positif
            if (fiche.getSalaireNet() == null || fiche.getSalaireNet().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Fiche ID " + fiche.getId() + ": Salaire net invalide");
                invalidSalaries++;
                continue;
            }

            // Vérification que le salaire net <= salaire brut
            if (fiche.getSalaireNet().compareTo(fiche.getSalaireBrut()) > 0) {
                errors.add("Fiche ID " + fiche.getId() + ": Salaire net supérieur au salaire brut");
                invalidSalaries++;
                continue;
            }

            // Vérification des variations importantes
            BigDecimal ecart = fiche.getSalaireBrut().subtract(fiche.getSalaireNet());
            BigDecimal pourcentageEcart = ecart.divide(fiche.getSalaireBrut(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (pourcentageEcart.compareTo(BigDecimal.valueOf(50)) > 0) {
                warnings.add("Fiche ID " + fiche.getId() + ": Écart important entre brut et net (" +
                           pourcentageEcart.intValue() + "%)");
            }

            totalBrut = totalBrut.add(fiche.getSalaireBrut());
            totalNet = totalNet.add(fiche.getSalaireNet());
        }

        metrics.put("fichesInvalides", invalidSalaries);
        metrics.put("totalSalaireBrut", totalBrut);
        metrics.put("totalSalaireNet", totalNet);
        metrics.put("tauxPrelevement", totalBrut.compareTo(BigDecimal.ZERO) > 0 ?
                   totalBrut.subtract(totalNet).divide(totalBrut, 4, BigDecimal.ROUND_HALF_UP)
                   .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
    }

    private void validateContributions(List<FichePaie> fiches, List<String> errors,
                                     List<String> warnings, Map<String, Object> metrics) {
        int invalidContributions = 0;
        BigDecimal totalCotisationsSalariales = BigDecimal.ZERO;
        BigDecimal totalCotisationsPatronales = BigDecimal.ZERO;

        for (FichePaie fiche : fiches) {
            // Validation des cotisations salariales
            if (fiche.getCotisationsSalariales() != null) {
                if (fiche.getCotisationsSalariales().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("Fiche ID " + fiche.getId() + ": Cotisations salariales négatives");
                    invalidContributions++;
                }
                totalCotisationsSalariales = totalCotisationsSalariales.add(fiche.getCotisationsSalariales());
            }

            // Validation des cotisations patronales
            if (fiche.getCotisationsPatronales() != null) {
                if (fiche.getCotisationsPatronales().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("Fiche ID " + fiche.getId() + ": Cotisations patronales négatives");
                    invalidContributions++;
                }
                totalCotisationsPatronales = totalCotisationsPatronales.add(fiche.getCotisationsPatronales());
            }

            // Validation de la cohérence des cotisations
            if (fiche.getSalaireBrut() != null && fiche.getCotisationsSalariales() != null) {
                BigDecimal tauxCotisation = fiche.getCotisationsSalariales()
                        .divide(fiche.getSalaireBrut(), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                if (tauxCotisation.compareTo(BigDecimal.valueOf(30)) > 0) {
                    warnings.add("Fiche ID " + fiche.getId() + ": Taux de cotisation élevé (" +
                               tauxCotisation.intValue() + "%)");
                }
            }
        }

        metrics.put("cotisationsInvalides", invalidContributions);
        metrics.put("totalCotisationsSalariales", totalCotisationsSalariales);
        metrics.put("totalCotisationsPatronales", totalCotisationsPatronales);
    }

    private void validatePayrollElements(List<FichePaie> fiches, List<String> errors,
                                       List<String> warnings, Map<String, Object> metrics) {
        int invalidElements = 0;
        int totalElements = 0;

        for (FichePaie fiche : fiches) {
            if (fiche.getElements() != null) {
                for (ElementPaie element : fiche.getElements()) {
                    totalElements++;

                    // Validation du montant
                    if (element.getMontant() == null) {
                        errors.add("Élément ID " + element.getId() + ": Montant manquant");
                        invalidElements++;
                        continue;
                    }

                    // Validation du libellé
                    if (element.getLibelle() == null || element.getLibelle().trim().isEmpty()) {
                        warnings.add("Élément ID " + element.getId() + ": Libellé manquant");
                    }

                    // Validation du type
                    if (element.getType() == null) {
                        errors.add("Élément ID " + element.getId() + ": Type manquant");
                        invalidElements++;
                    }
                }
            }
        }

        metrics.put("elementsInvalides", invalidElements);
        metrics.put("totalElements", totalElements);
        metrics.put("tauxElementsValides", totalElements > 0 ?
                   (double)(totalElements - invalidElements) / totalElements * 100 : 0);
    }

    private void validateTemporalConsistency(List<FichePaie> fiches, List<String> errors,
                                           List<String> warnings, Map<String, Object> metrics) {
        // Vérification de la cohérence des dates de génération
        for (FichePaie fiche : fiches) {
            if (fiche.getDateGeneration() == null) {
                warnings.add("Fiche ID " + fiche.getId() + ": Date de génération manquante");
            }

            if (fiche.getPeriode() == null) {
                errors.add("Fiche ID " + fiche.getId() + ": Période manquante");
            }
        }
    }

    private void calculateQualityMetrics(List<FichePaie> fiches, Map<String, Object> metrics) {
        int totalFiches = fiches.size();
        int fichesCompletes = 0;

        for (FichePaie fiche : fiches) {
            boolean complete = fiche.getSalaireBrut() != null &&
                             fiche.getSalaireNet() != null &&
                             fiche.getCotisationsSalariales() != null &&
                             fiche.getDateGeneration() != null &&
                             fiche.getPeriode() != null;

            if (complete) {
                fichesCompletes++;
            }
        }

        metrics.put("totalFiches", totalFiches);
        metrics.put("fichesCompletes", fichesCompletes);
        metrics.put("tauxCompletude", totalFiches > 0 ?
                   (double)fichesCompletes / totalFiches * 100 : 0);
    }

    /**
     * Classe pour encapsuler le résultat de validation
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private Map<String, Object> metrics = new HashMap<>();

        // Getters et setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }

        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }

        public Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    }
}
