package ma.digitalia.appmain.services;

import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Users;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import ma.digitalia.gestionutilisateur.repositories.ManagerRepository;
import ma.digitalia.gestionconges.entities.TypeConge;
import ma.digitalia.gestionconges.entities.SoldeConge;
import ma.digitalia.gestionconges.entities.DemandeConge;
import ma.digitalia.gestionconges.repositories.TypeCongeRpository;
import ma.digitalia.gestionconges.repositories.SoldeCongeRepository;
import ma.digitalia.gestionconges.repositories.DemandeCongeRepository;
import ma.digitalia.gestionconges.Enum.StatutDemande;
import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.generationfichepaie.repositories.ElementPaieRepository;
import ma.digitalia.generationfichepaie.Enum.TypeElement;
import ma.digitalia.generationfichepaie.Enum.ModeCalcul;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final PointageRepository pointageRepository;
    private final ActiviteRepository activiteRepository;
    private final EmployeRepository employeRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final ManagerRepository managerRepository;
    private final TypeCongeRpository typeCongeRpository;
    private final SoldeCongeRepository soldeCongeRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final ElementPaieRepository elementPaieRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSynthetiqueService(PointageRepository pointageRepository, ActiviteRepository activiteRepository,
                                EmployeRepository employeRepository, ProjetRepository projetRepository,
                                TacheRepository tacheRepository, ManagerRepository managerRepository,
                                TypeCongeRpository typeCongeRpository, SoldeCongeRepository soldeCongeRepository,
                                DemandeCongeRepository demandeCongeRepository, ElementPaieRepository elementPaieRepository,
                                PasswordEncoder passwordEncoder) {
        this.pointageRepository = pointageRepository;
        this.activiteRepository = activiteRepository;
        this.employeRepository = employeRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.managerRepository = managerRepository;
        this.typeCongeRpository = typeCongeRpository;
        this.soldeCongeRepository = soldeCongeRepository;
        this.demandeCongeRepository = demandeCongeRepository;
        this.elementPaieRepository = elementPaieRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        // Générer d'abord les données de base : manager, employés, types de congés et éléments de paie
        System.out.println("🏗 Génération des données de base...");
        genererDonneesDeBase();

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

        System.out.println("📅 Génération des pointages pour les 2 derniers mois...");
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

    /**
     * Génère les données de base : 1 manager, ses employés, types de congés et éléments de paie
     */
    public void genererDonneesDeBase() {
        // Générer 1 manager
        Manager manager = genererManager();

        // Générer ses employés
        List<Employe> employes = genererEmployesPourManager(manager);

        // Générer 3-4 types de congés
        List<TypeConge> typesConges = genererTypesConges();

        // Générer soldes de congés pour chaque employé
        genererSoldesCongesPourEmployes(employes, typesConges);

        // Générer 4-5 éléments de paie pour chaque employé
        genererElementsPaiePourEmployes(employes);

        // Générer des demandes de congés avec différents statuts
        genererDemandesCongesPourEmployes(employes, typesConges, manager);

        System.out.println("✅ Données de base générées : 1 manager, " + employes.size() +
                          " employés, " + typesConges.size() + " types de congés");
    }

    /**
     * Génère un manager synthétique
     */
    private Manager genererManager() {
        // Vérifier s'il existe déjà un manager
        List<Users> managersExistants = managerRepository.findAll();
        if (!managersExistants.isEmpty()) {
            System.out.println("📋 Manager existant trouvé, utilisation du manager : " + managersExistants.get(0).getNom());
            return (Manager) managerRepository.findById(managersExistants.get(0).getId()).get();
        }

        Manager manager = new Manager();
        manager.setNom("BENALI");
        manager.setPreNom("Ahmed");
        manager.setEmail("ahmed.benali@digitalia.ma");
        manager.setTelephone("0661234567");
        manager.setDateNaissance(LocalDate.of(1980, 5, 15).toString());
        manager.setDepartment("Développement");
        manager.setPassword(passwordEncoder.encode("manager123"));

        manager = managerRepository.save(manager);
        System.out.println("👨‍💼 Manager créé : " + manager.getNom() + " " + manager.getPreNom());
        return manager;
    }

    /**
     * Génère des employés pour un manager
     */
    private List<Employe> genererEmployesPourManager(Manager manager) {
        List<Employe> employes = new ArrayList<>();

        // Vérifier s'il y a déjà des employés pour ce manager
        if (manager.getEmployes() != null && !manager.getEmployes().isEmpty()) {
            System.out.println("👥 " + manager.getEmployes().size() + " employés existants trouvés pour le manager");
            return manager.getEmployes();
        }

        String[][] donneesEmployes = {
            {"ALAMI", "Sara", "sara.alami@digitalia.ma", "0662345678"},
            {"TAZI", "Youssef", "youssef.tazi@digitalia.ma", "0663456789"},
            {"KABBAJ", "Leila", "leila.kabbaj@digitalia.ma", "0664567890"},
            {"BENNANI", "Karim", "karim.bennani@digitalia.ma", "0665678901"},
            {"FASSI", "Amina", "amina.fassi@digitalia.ma", "0666789012"}
        };

        for (String[] donnees : donneesEmployes) {
            Employe employe = new Employe();
            employe.setNom(donnees[0]);
            employe.setPreNom(donnees[1]);
            employe.setEmail(donnees[2]);
            employe.setTelephone(donnees[3]);
            employe.setDateNaissance(LocalDate.of(1985 + random.nextInt(10),
                                                 1 + random.nextInt(12),
                                                 1 + random.nextInt(28)).toString());
            employe.setManager(manager);
            employe.setPassword(passwordEncoder.encode("employe123"));

            employe = employeRepository.save(employe);
            employes.add(employe);
        }

        System.out.println("👥 " + employes.size() + " employés créés pour le manager " + manager.getNom());
        return employes;
    }

    /**
     * Génère 3-4 types de congés
     */
    private List<TypeConge> genererTypesConges() {
        List<TypeConge> typesConges = typeCongeRpository.findAll();
        if (!typesConges.isEmpty()) {
            System.out.println("🏖 " + typesConges.size() + " types de congés existants trouvés");
            return typesConges;
        }

        Object[][] donneesTypesConges = {
            {"Congés Payés", 30, true, "#4CAF50"},
            {"Congés Maladie", 90, true, "#F44336"},
            {"Congés Sans Solde", 365, false, "#9E9E9E"},
            {"Congés Maternité", 98, true, "#E91E63"}
        };

        for (Object[] donnees : donneesTypesConges) {
            TypeConge typeConge = new TypeConge();
            typeConge.setNom((String) donnees[0]);
            typeConge.setNombreJoursMax((Integer) donnees[1]);
            typeConge.setPaye((Boolean) donnees[2]);
            typeConge.setCouleur((String) donnees[3]);

            typeConge = typeCongeRpository.save(typeConge);
            typesConges.add(typeConge);
        }

        System.out.println("🏖 " + typesConges.size() + " types de congés créés");
        return typesConges;
    }

    /**
     * Génère des soldes de congés pour chaque employé
     */
    private void genererSoldesCongesPourEmployes(List<Employe> employes, List<TypeConge> typesConges) {
        int anneeActuelle = LocalDate.now().getYear();
        int soldesGeneres = 0;

        for (Employe employe : employes) {
            // Vérifier s'il y a déjà des soldes pour cet employé
            List<SoldeConge> soldesExistants = soldeCongeRepository.findByEmploye(employe);
            if (!soldesExistants.isEmpty()) {
                continue; // Passer à l'employé suivant
            }

            for (TypeConge typeConge : typesConges) {
                SoldeConge solde = new SoldeConge();
                solde.setEmploye(employe);
                solde.setType(typeConge);
                solde.setAnnee(anneeActuelle);

                // Définir les soldes initiaux selon le type de congé
                int soldeInitial;
                int soldePris;
                switch (typeConge.getNom()) {
                    case "Congés Payés":
                        soldeInitial = 30;
                        soldePris = random.nextInt(10); // Entre 0 et 9 jours pris
                        break;
                    case "Congés Maladie":
                        soldeInitial = 90;
                        soldePris = random.nextInt(5); // Entre 0 et 4 jours pris
                        break;
                    case "Congés Sans Solde":
                        soldeInitial = 365;
                        soldePris = 0; // Généralement pas utilisé
                        break;
                    case "Congés Maternité":
                        soldeInitial = 98;
                        soldePris = 0; // Pas encore utilisé
                        break;
                    default:
                        soldeInitial = typeConge.getNombreJoursMax();
                        soldePris = random.nextInt(Math.min(5, soldeInitial));
                        break;
                }

                solde.setSoldeInitial(soldeInitial);
                solde.setSoldePris(soldePris);
                solde.setSoldeRestant(soldeInitial - soldePris);

                soldeCongeRepository.save(solde);
                soldesGeneres++;
            }
        }

        System.out.println("💳 " + soldesGeneres + " soldes de congés générés pour " + employes.size() + " employés");
    }

    /**
     * Génère des demandes de congés avec différents statuts pour chaque employé
     */
    private void genererDemandesCongesPourEmployes(List<Employe> employes, List<TypeConge> typesConges, Manager validateur) {
        String[] motifs = {
            "Congés annuels",
            "Repos médical suite maladie",
            "Événement familial",
            "Formation personnelle",
            "Voyage personnel",
            "Repos compensateur"
        };

        StatutDemande[] statuts = {StatutDemande.EN_ATTENTE, StatutDemande.VALIDEE, StatutDemande.REJETEE};
        int demandesGenerees = 0;
        LocalDate dateActuelle = LocalDate.now();

        for (Employe employe : employes) {
            // Générer 2-4 demandes par employé
            int nombreDemandes = 2 + random.nextInt(3);

            for (int i = 0; i < nombreDemandes; i++) {
                TypeConge typeConge = typesConges.get(random.nextInt(typesConges.size()));

                // Dates aléatoires dans les prochains 6 mois
                LocalDate dateDebut = dateActuelle.plusDays(random.nextInt(180));
                LocalDate dateFin = dateDebut.plusDays(1 + random.nextInt(10)); // 1 à 10 jours

                DemandeConge demande = new DemandeConge();
                demande.setDemandeur(employe);
                demande.setTypeConge(typeConge);
                demande.setDateDebut(dateDebut);
                demande.setDateFin(dateFin);
                demande.setMotif(motifs[random.nextInt(motifs.length)]);
                demande.setValidateur(validateur);

                // Statut aléatoire
                StatutDemande statut = statuts[random.nextInt(statuts.length)];
                demande.setStatut(statut);

                // Si validée ou rejetée, ajouter date de traitement
                if (statut != StatutDemande.EN_ATTENTE) {
                    demande.setDateTraitement(LocalDateTime.now().minusDays(random.nextInt(30)));
                    if (statut == StatutDemande.REJETEE) {
                        demande.setCommentaire("Période de forte activité dans l'équipe");
                    } else {
                        demande.setCommentaire("Demande approuvée");
                    }
                }

                // Calculer automatiquement le nombre de jours (méthode dans l'entité)
                demande.setNombreJours(demande.calculeJoursOuvrables(dateDebut, dateFin));

                demandeCongeRepository.save(demande);
                demandesGenerees++;
            }
        }

        System.out.println("📝 " + demandesGenerees + " demandes de congés générées pour " + employes.size() + " employés");
    }

    /**
     * Génère 4-5 éléments de paie pour chaque employé
     */
    private void genererElementsPaiePourEmployes(List<Employe> employes) {
        Object[][] donneesElementsPaie = {
            {"Salaire de Base", TypeElement.SALAIRE_BASE, ModeCalcul.MONTANT, 15000.0, null, null, true, true},
            {"Prime de Performance", TypeElement.PRIME_FIXE, ModeCalcul.MONTANT, 2000.0, null, null, true, true},
            {"Indemnité Transport", TypeElement.INDEMNITE, ModeCalcul.MONTANT, 500.0, null, null, false, false},
            {"Heures Supplémentaires", TypeElement.HEURES_SUPPLEMENTAIRES, ModeCalcul.PAR_HEURE, null, 150.0, 8.0, true, true},
            {"Retenue CNSS", TypeElement.COTISATION_SOCIALE, ModeCalcul.TAUX, null, 0.0448, null, false, true}
        };

        int elementsGeneres = 0;
        for (Employe employe : employes) {
            for (Object[] donnees : donneesElementsPaie) {
                ElementPaie element = new ElementPaie();
                element.setLibelle((String) donnees[0]);
                element.setType((TypeElement) donnees[1]);
                element.setModeCalcul((ModeCalcul) donnees[2]);

                if (donnees[3] != null) {
                    element.setMontant(java.math.BigDecimal.valueOf((Double) donnees[3]));
                }
                if (donnees[4] != null) {
                    element.setTaux(java.math.BigDecimal.valueOf((Double) donnees[4]));
                }
                if (donnees[5] != null) {
                    element.setBase(java.math.BigDecimal.valueOf((Double) donnees[5]));
                }

                element.setSoumisIR((Boolean) donnees[6]);
                element.setSoumisCNSS((Boolean) donnees[7]);
                element.setEmploye(employe);
                element.setDescription("Élément de paie généré automatiquement");

                elementPaieRepository.save(element);
                elementsGeneres++;
            }
        }

        System.out.println("💰 " + elementsGeneres + " éléments de paie générés pour " + employes.size() + " employés");
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
