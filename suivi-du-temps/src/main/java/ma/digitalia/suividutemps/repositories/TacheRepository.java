package ma.digitalia.suividutemps.repositories;

import ma.digitalia.suividutemps.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TacheRepository extends JpaRepository<Tache, Long> {
}
