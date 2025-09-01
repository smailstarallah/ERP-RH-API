package ma.digitalia.suividutemps.repositories;

import ma.digitalia.suividutemps.entities.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetRepository extends JpaRepository<Projet, Long> {
}
