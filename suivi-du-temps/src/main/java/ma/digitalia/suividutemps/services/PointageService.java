package ma.digitalia.suividutemps.services;

import ma.digitalia.suividutemps.Enum.TypeActivite;
import ma.digitalia.suividutemps.dto.*;
import ma.digitalia.suividutemps.entities.Activite;
import ma.digitalia.suividutemps.entities.Pointage;

import java.time.LocalDate;
import java.util.List;

public interface PointageService {

    /**
     * Démarre le suivi du temps pour une tâche spécifique.
     *
     * @param pointageRequest L'objet contenant les informations nécessaires pour démarrer le suivi du temps.
     */
    void startTimeTracking(PointageRequest pointageRequest);

    /**
     * Arrête le suivi du temps pour une tâche spécifique.
     *
     * @param pointageRequest L'objet contenant les informations nécessaires pour arrêter le suivi du temps.
     */
    void stopTimeTracking(PointageRequest pointageRequest);

    /**
     * Démarre une pause pour un employé spécifique.
     *
     * @param pointageRequest L'objet contenant les informations nécessaires pour démarrer la pause.
     */
    void startPauseTime(PointageRequest pointageRequest);

    /**
     * Arrête la pause pour un employé spécifique.
     *
     * @param pointageRequest L'objet contenant les informations nécessaires pour arrêter la pause.
     */
    void stopPauseTime(PointageRequest pointageRequest);

    Activite demarrerOuChangerActivite(Long employeId, TypeActivite type, Long projetId, Long tacheId, String description);

    /**
     * Récupère le pointage d'un employé pour une date spécifique.
     * @param employeId
     * @param date
     * @return Pointage de l'employé pour la date spécifiée.
     */
    Pointage getPointageByEmployeAndDate(Long employeId, LocalDate date);

    /**
     * Récupère le pointage d'un employé pour une date spécifique.
     * @param employeId
     * @param date
     * @return WeeklyPointageDto pour la semaine de l'employé à partir de la date spécifiée.
     */
    WeeklyPointageDto getPointagesSemaineByEmploye (Long employeId, LocalDate date);

    List<EfficaciteParSemaineDTO> getEfficaciteParSemaine(Long employeId, LocalDate date);

    List<TeamStatusDto> getTeamStatus(Long managerId, LocalDate date);

    /**
     * Récupère les punchs (horodatages) pour un employé donné entre deux dates.
     *
     * @param employeId L'ID de l'employé.
     * @param startDate La date de début de la période.
     * @param endDate   La date de fin de la période.
     * @return Une liste de PunchDto représentant les punchs de l'employé dans la période spécifiée.
     */
    List<PunchDto> getPunchsForEmployee(Long employeId, LocalDate startDate, LocalDate endDate);

    MonthlyPresenceReportDto getMonthlyPresenceReport(Long employeId, int year, int month);

    List<ActivityOfDayDto> getActivitiesOfDay(Long empId);

    /**
     * Récupère les pointages et la planification pour une semaine donnée.
     *
     * @param weekStart La date de début de la semaine
     * @return PointageResponseDto contenant les activités et la planification de la semaine
     */
    PointageResponseDto getPointagesForWeek(LocalDate weekStart, Long employeId);

    /**
     * Démarre le suivi du temps pour une réunion.
     * @param pointageRequest L'objet contenant les informations nécessaires pour démarrer la réunion.
     */
    void startReunionTime(PointageRequest pointageRequest);
}
