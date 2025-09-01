package ma.digitalia.suividutemps.services;

import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.suividutemps.entities.RapportTemps;

import java.time.Month;
import java.time.YearMonth;

public interface RapportTempsService {
    /**
     * Generates a time report for the specified user and period.
     *
     * @param userId the ID of the user for whom the report is generated
     * @param startDate the start date of the period for which the report is generated
     * @param endDate the end date of the period for which the report is generated
     * @return a string representing the time report
     */
    String generateTimeReport(Long userId, String startDate, String endDate);

    /**
     * generer rapport mensuel
     * @param userId the ID of the user for whom the report is generated
     * @param month the month for which the report is generated in the format "YYYY-MM"
     */
    void generateMonthlyReport(Long userId, Month month);

    /**
     * Recuperer le rapport mensuel pour un employé
     * @param employe l'employé pour lequel le rapport est généré
     * @param yearMonth l'année et le mois pour lequel le rapport est généré
     * @return le rapport mensuel de l'employé pour le mois spécifié
     */
    RapportTemps getMonthlyReport(Employe employe, YearMonth yearMonth);
}
