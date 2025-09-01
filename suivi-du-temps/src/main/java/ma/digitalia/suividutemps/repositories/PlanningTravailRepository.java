package ma.digitalia.suividutemps.repositories;

import ma.digitalia.suividutemps.entities.PlanningTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;

@Repository
public interface PlanningTravailRepository extends JpaRepository<PlanningTravail, Long> {

    PlanningTravail findByJourSemaine(DayOfWeek jourSemaine);
}
