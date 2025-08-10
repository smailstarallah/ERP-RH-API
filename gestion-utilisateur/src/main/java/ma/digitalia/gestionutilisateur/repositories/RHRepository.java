package ma.digitalia.gestionutilisateur.repositories;

import ma.digitalia.gestionutilisateur.entities.RH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RHRepository extends JpaRepository<RH, Long> {
}
