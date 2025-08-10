package ma.digitalia.gestionutilisateur.repositories;


import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Users, Long> {

    Optional<Employe> findByNumeroEmploye(String numeroEmploye);

    List<Employe> findByManagerId(Long managerId);

    @Query("SELECT e FROM Employe e WHERE e.manager.Id = ?1 AND e.active = true")
    List<Employe> findActiveEmployesByManager(Long managerId);
}
