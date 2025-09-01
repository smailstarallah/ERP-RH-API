package ma.digitalia.generationfichepaie.services;

import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.generationfichepaie.entities.FichePaie;

import java.util.List;

public interface GenerationFichePaieService {

    /**
     * Ajouter un élément de paie pour un employé
     * @param employeId l'identifiant de l'employé
     */
    void ajouterElementPaie(Long employeId, ElementPaie elementPaie);

    /**
     * recuperer element de paie pour un employé
     * @param employeId l'identifiant de l'employé
     * @return l'élément de paie pour l'employé
     */
    List<ElementPaie> recupererElementPaie(Long employeId);

    /**
     * Generer la fiche de paie pour un employe
     * @param employeId l'identifiant de l'employé
     */
    void genererFichePaie(Long employeId);

    /**
     * Mettre à jour un élément de paie pour un employé
     * @param employeId l'identifiant de l'employé
     * @param elementPaie l'Objet qui contient les informations de l'élément de paie à mettre à jour
     * @param elementPaieId l'identifiant de l'élément de paie à mettre à jour
     */
    void mettreAJourElementPaie(Long employeId, Long elementPaieId, ElementPaie elementPaie);

    /**
     * generer et sauvgarder fiche pdf pour un employé
     * @param fichePaie la fiche de paie
     */
    void genererEtSauvegarderFichePdf(FichePaie fichePaie);

    /**
     * Récupérer la fiche de paie pour un employé
     * @param employeId l'identifiant de l'employé
     * @return la fiche de paie pour l'employé
     */
    byte[] recupererFichePaiePdf(Long employeId);
}
