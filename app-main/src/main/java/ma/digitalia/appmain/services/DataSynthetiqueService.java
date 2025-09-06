package ma.digitalia.appmain.services;

import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Users;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import ma.digitalia.suividutemps.Enum.StatutPointage;
import ma.digitalia.suividutemps.Enum.TypeActivite;
import ma.digitalia.suividutemps.entities.Activite;
import ma.digitalia.suividutemps.entities.Pointage;
import ma.digitalia.suividutemps.entities.Projet;
import ma.digitalia.suividutemps.entities.Tache;
import ma.digitalia.suividutemps.repositories.ActiviteRepository;
import ma.digitalia.suividutemps.repositories.PointageRepository;
import ma.digitalia.suividutemps.repositories.ProjetRepository;
import ma.digitalia.suividutemps.repositories.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
@Service
@Transactional
public class DataSynthetiqueService {

    @Autowired
    private PointageRepository pointageRepository;

    @Autowired
    private ActiviteRepository activiteRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private TacheRepository tacheRepository;

    private final Random random = new Random();

    private final String[] descriptions = {
            "Développement de nouvelles fonctionnalités",
            "Résolution de bugs critiques",
            "Tests et validation",
            "Rédaction de documentation",
            "Réunion d'équipe",
            "Formation technique",
            "Analyse des besoins client",
            "Révision de code",
            "Déploiement en production",
            "Maintenance système"
    };

    /**
     * Génère les données manquantes pour les 2 derniers mois
     */
    public void genererDonneesDeuxDerniersMois() {
        LocalDate aujourd = LocalDate.now();
        LocalDate dateDebut = aujourd.minusMonths(2);

        // Récupérer tous les employés
        List<Employe> employes = employeRepository.findAll().stream()
                .filter(user -> user instanceof Employe)
                .map(user -> (Employe) user)
                .collect(Collectors.toList());

        if (employes.isEmpty()) {
            System.out.println("⚠ Aucun employé trouvé.");
            return;
        }

        // Créer projets/tâches si besoin
        List<Projet> projets = obtenirOuCreerProjets();
        List<Tache> taches = obtenirOuCreerTaches(projets);

        // Parcourir chaque jour ouvrable (lundi-vendredi)
        LocalDate dateCourante = dateDebut;
        while (!dateCourante.isAfter(aujourd)) {
            if (dateCourante.getDayOfWeek().getValue() <= 5) { // du lundi au vendredi
                for (Employe employe : employes) {
                    Pointage pointageExistant = pointageRepository.findByEmployeAndDate(employe, dateCourante);
                    if (pointageExistant == null) {
                        creerPointageJournee(employe, dateCourante, projets, taches);
                    }
                }
            }
            dateCourante = dateCourante.plusDays(1);
        }

        System.out.println("✅ Données synthétiques générées pour les 2 derniers mois !");
    }

    private void creerPointageJournee(Employe employe, LocalDate date, List<Projet> projets, List<Tache> taches) {
        Pointage pointage = new Pointage();
        pointage.setEmploye(employe);
        pointage.setDate(date);

        // Heures d'arrivée réalistes (8h00 à 9h15)
        LocalTime heureArrivee = LocalTime.of(8, 0).plusMinutes(random.nextInt(75));
        pointage.setHeureEntree(LocalDateTime.of(date, heureArrivee));

        // Heure de sortie (17h00 à 18h30)
        LocalTime heureSortie = LocalTime.of(17, 0).plusMinutes(random.nextInt(90));
        pointage.setHeureSortie(LocalDateTime.of(date, heureSortie));

        pointage.setStatut(StatutPointage.TERMINE);

        pointage = pointageRepository.save(pointage);

        // Générer les activités du jour
        creerActivitesJournee(pointage, projets, taches);

        // Calcul heures travaillées
        pointage.calculerHeuresTravaillees();
        pointageRepository.save(pointage);
    }

    private void creerActivitesJournee(Pointage pointage, List<Projet> projets, List<Tache> taches) {
        LocalDateTime debut = pointage.getHeureEntree();
        LocalDateTime finJournee = pointage.getHeureSortie();
        List<Activite> activites = new ArrayList<>();

        while (debut.isBefore(finJournee)) {
            Activite activite = new Activite();
            activite.setPointage(pointage);
            activite.setDebut(debut);

            if (activites.isEmpty() || activites.get(activites.size() - 1).getType() == TypeActivite.PAUSE) {
                // Travail : 1h30 à 3h
                activite.setType(TypeActivite.TRAVAIL);
                LocalDateTime fin = debut.plusMinutes(90 + random.nextInt(60));
                if (fin.isAfter(finJournee)) fin = finJournee;
                activite.setFin(fin);
                activite.setDescription(descriptions[random.nextInt(descriptions.length)]);

                if (!projets.isEmpty()) activite.setProjet(projets.get(random.nextInt(projets.size())));
                if (!taches.isEmpty()) activite.setTache(taches.get(random.nextInt(taches.size())));

                debut = fin;
            } else {
                // Pause : 20-45 min
                activite.setType(TypeActivite.PAUSE);
                LocalDateTime fin = debut.plusMinutes(20 + random.nextInt(25));
                if (fin.isAfter(finJournee)) break;
                activite.setFin(fin);
                activite.setDescription("Pause café/déjeuner");

                debut = fin;
            }

            activites.add(activite);
            activiteRepository.save(activite);

            if (debut.plusMinutes(30).isAfter(finJournee)) break;
        }
    }

    private List<Projet> obtenirOuCreerProjets() {
        List<Projet> projets = projetRepository.findAll();
        if (projets.isEmpty()) {
            String[] nomsProjects = {"ERP RH", "App Mobile", "E-commerce", "Gestion interne"};
            String[] clients = {"Client A", "Client B", "Client C", "Interne"};

            for (int i = 0; i < nomsProjects.length; i++) {
                Projet projet = new Projet();
                projet.setNom(nomsProjects[i]);
                projet.setClient(clients[i]);
                projet.setDescription("Projet " + nomsProjects[i]);
                projet.setDateDebut(LocalDate.now().minusMonths(2));
                projet.setDateFinPrevue(LocalDate.now().plusMonths(6));
                projet.setBudget(20000.0 + random.nextDouble() * 80000);
                projets.add(projetRepository.save(projet));
            }
        }
        return projets;
    }

    private List<Tache> obtenirOuCreerTaches(List<Projet> projets) {
        List<Tache> taches = tacheRepository.findAll();
        if (taches.isEmpty() && !projets.isEmpty()) {
            String[] nomsTaches = {"Analyse", "Design UI", "Backend", "Frontend", "Tests", "Déploiement"};
            for (Projet projet : projets) {
                for (String nom : nomsTaches) {
                    Tache tache = new Tache();
                    tache.setNom(nom);
                    tache.setDescription("Tâche: " + nom);
                    tache.setEstimationHeures(8.0 + random.nextDouble() * 24);
                    tache.setProjet(projet);
                    taches.add(tacheRepository.save(tache));
                }
            }
        }
        return taches;
    }
}
