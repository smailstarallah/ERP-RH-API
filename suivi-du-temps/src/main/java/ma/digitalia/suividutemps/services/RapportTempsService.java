package ma.digitalia.suividutemps.services;

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
}
