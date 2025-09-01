package ma.digitalia.generationfichepaie.repositories;

import ma.digitalia.generationfichepaie.Enum.TypeElement;
import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.gestionutilisateur.entities.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ElementPaieRepository extends JpaRepository<ElementPaie, Long> {

    boolean existsByEmployeAndTypeAndSousType(Employe employe, TypeElement type, String sousType);
    List<ElementPaie> findByEmploye(Employe employe);


    boolean existsByLibelleContaining(String libelle);
}
