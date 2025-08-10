package ma.digitalia.suividutemps.services;

public interface PlanningTravailService {

    /**
     * Démarre le suivi du temps pour une tâche spécifique.
     *
     * @param empId L'identifiant de l'employé pour lequel le suivi doit être démarré.
     */
    void startTimeTracking(Long empId);

    /**
     * Arrête le suivi du temps pour une tâche spécifique.
     *
     * @param empId L'identifiant de la tâche dont le suivi doit être arrêté.
     */
    void stopTimeTracking(Long empId);

    /**
     * Démarre une pause pour un employé spécifique.
     *
     * @param empId L'identifiant de l'employé pour lequel la pause doit être démarrée.
     */
    void startPauseTime(Long empId);

    /**
     * Arrête la pause pour un employé spécifique.
     *
     * @param empId L'identifiant de l'employé pour lequel la pause doit être arrêtée.
     */
    void stopPauseTime(Long empId);
}
