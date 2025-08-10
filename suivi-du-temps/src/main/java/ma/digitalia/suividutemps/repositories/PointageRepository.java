package ma.digitalia.suividutemps.repositories;

import ma.digitalia.suividutemps.entities.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointageRepository extends JpaRepository<Pointage, Long> {
}
