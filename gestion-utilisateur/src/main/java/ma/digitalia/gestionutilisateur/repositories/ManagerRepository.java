package ma.digitalia.gestionutilisateur.repositories;

import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Users, Long> {
    //Optional<Manager> findByDepartement(String departement);*
    Optional<Manager> findByEmail(String email);


    @Query("SELECT DISTINCT m.department FROM Manager m")
    List<String> findAllDepartements();
}
