package ma.digitalia.appmain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ma.digitalia.appmain",
        "ma.digitalia.appshared",
        "ma.digitalia.gestionutilisateur",
        "ma.digitalia.gestionconges",
        "ma.digitalia.suividutemps",
})
public class AppMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppMainApplication.class, args);
    }

}