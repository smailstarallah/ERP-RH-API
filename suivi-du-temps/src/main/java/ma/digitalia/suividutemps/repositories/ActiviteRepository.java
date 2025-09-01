package ma.digitalia.suividutemps.repositories;

import ma.digitalia.suividutemps.entities.Activite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    Optional<Activite> findByPointageEmployeIdAndFinIsNull(Long employeId);

}
