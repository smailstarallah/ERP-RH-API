package ma.digitalia.suividutemps.repositories;

import ma.digitalia.suividutemps.entities.RapportTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RapportTempsRepository extends JpaRepository<RapportTemps, Long> {
}
