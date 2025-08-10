package ma.digitalia.suividutemps.services;

public interface PointageService {
    /**
     * Enregistre un pointage pour l'utilisateur courant.
     *
     * @param dateHeure le pointage à enregistrer
     */
    void enregistrerPointage(String dateHeure);

    /**
     * Récupère le dernier pointage de l'utilisateur courant.
     *
     * @return le dernier pointage
     */
    String recupererDernierPointage();

    /**
     * Récupère tous les pointages de l'utilisateur courant.
     *
     * @return une liste de tous les pointages
     */
    java.util.List<String> recupererTousLesPointages();
}
