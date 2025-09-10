package ma.digitalia.appmain;

import ma.digitalia.appmain.services.DataSynthetiqueService;
import ma.digitalia.suividutemps.repositories.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "ma.digitalia.appmain",
        "ma.digitalia.appshared",
        "ma.digitalia.gestionutilisateur",
        "ma.digitalia.gestionconges",
        "ma.digitalia.suividutemps",
        "ma.digitalia.generationfichepaie",
        "ma.digitalia.systemalert",
})
@EnableJpaRepositories(basePackages = {
        "ma.digitalia.gestionutilisateur.repositories",
        "ma.digitalia.gestionconges.repositories",
        "ma.digitalia.suividutemps.repositories",
        "ma.digitalia.generationfichepaie.repositories",
        "ma.digitalia.systemalert.repository"
})
public class AppMainApplication implements CommandLineRunner {

    @Autowired
    private DataSynthetiqueService dataSynthetiqueService;

    public static void main(String[] args) {
        SpringApplication.run(AppMainApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Génération des données synthétiques de pointage ===");
        try {
            dataSynthetiqueService.genererDonneesDeuxDerniersMois();
            System.out.println("✅ Génération terminée avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération des données : " + e.getMessage());
            e.printStackTrace();
        }
    }
}