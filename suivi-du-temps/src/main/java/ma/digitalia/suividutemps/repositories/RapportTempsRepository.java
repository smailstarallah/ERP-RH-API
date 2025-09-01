package ma.digitalia.suividutemps.repositories;

import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.suividutemps.entities.RapportTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RapportTempsRepository extends JpaRepository<RapportTemps, Long> {
    /**
     * Find a RapportTemps by employee ID and period.
     *
     * @param employe   the employee for whom the report is generated
     * @param periode   the period of the report (e.g., "2024-01", "2024-Q1", "2024")
     * @return the RapportTemps entity if found, otherwise null
     */
    RapportTemps findByEmployeAndPeriode(Employe employe, String periode);
}
