package ma.digitalia.gestionutilisateur.repositories;


import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Users, Long> {

    Optional<Employe> findByNumeroEmploye(String numeroEmploye);

    List<Employe> findByManager(Manager manager);

    List<Employe> findAllByActive(boolean active);

    @Query("SELECT e FROM Employe e WHERE e.manager.Id = ?1 AND e.active = true")
    List<Employe> findActiveEmployesByManager(Long managerId);

    @Query("SELECT e FROM Employe e WHERE e.manager.department = ?1")
    List<Employe> findByDepartement(String departement);

    @Query("SELECT e FROM Employe e ORDER BY e.manager.department")
    List<Employe> findAllEmployesOrderByDepartment();

    @Query("SELECT DISTINCT e.manager.department FROM Employe e WHERE e.manager.department IS NOT NULL")
    List<String> findAllDepartments();

    /**
     * Récupère la distribution salariale par département pour une période donnée
     * @param periode La période pour laquelle récupérer les données
     * @return Liste d'objets contenant : [departement, nombreEmployes, masseSalariale]
     */
    @Query(value = """
        SELECT 
            COALESCE(m.department, 'Non défini') as departement,
            COUNT(DISTINCT e.id) as nombreEmployes,
            COALESCE(SUM(fp.salaire_brut), 0) as masseSalariale
        FROM employe e 
        LEFT JOIN manager m ON e.manager_id = m.id 
        LEFT JOIN fiche_paie fp ON e.id = fp.employe_id 
            AND fp.periode = :periode
        WHERE e.active = true
        GROUP BY m.department
        ORDER BY m.department
        """, nativeQuery = true)
    List<Object[]> getSalaryDistributionByDepartment(@Param("periode") YearMonth periode);
}
