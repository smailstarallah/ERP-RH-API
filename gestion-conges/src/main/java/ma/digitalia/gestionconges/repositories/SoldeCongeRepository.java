package ma.digitalia.gestionconges.repositories;

import ma.digitalia.gestionconges.entities.SoldeConge;
import ma.digitalia.gestionutilisateur.entities.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoldeCongeRepository extends JpaRepository<SoldeConge, Long> {
    List<SoldeConge> findByEmploye(Employe employe);
}
