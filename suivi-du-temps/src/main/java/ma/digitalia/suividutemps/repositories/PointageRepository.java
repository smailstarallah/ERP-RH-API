package ma.digitalia.suividutemps.repositories;

import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.suividutemps.Enum.StatutPointage;
import ma.digitalia.suividutemps.entities.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointageRepository extends JpaRepository<Pointage, Long> {

    List<Pointage> findByEmployeAndDateBetween(Employe employe, LocalDate dateAfter, LocalDate dateBefore);
    Optional<Pointage> findByEmployeIdAndStatut(Long employeId, StatutPointage statut);
    Pointage findByEmployeAndDate(Employe employe, LocalDate date);

    List<Pointage> findByDateBetween(LocalDate startDate, LocalDate endDate);

}
