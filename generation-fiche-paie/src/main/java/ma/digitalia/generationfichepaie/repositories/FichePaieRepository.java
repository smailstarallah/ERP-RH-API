package ma.digitalia.generationfichepaie.repositories;

import ma.digitalia.generationfichepaie.entities.FichePaie;
import ma.digitalia.gestionutilisateur.entities.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;

@Controller
public interface FichePaieRepository extends JpaRepository<FichePaie, Long> {
    FichePaie findByEmploye(Employe employe);
}
