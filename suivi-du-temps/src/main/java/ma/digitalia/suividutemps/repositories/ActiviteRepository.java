package ma.digitalia.suividutemps.repositories;

import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.suividutemps.entities.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    Optional<Activite> findByPointageEmployeIdAndFinIsNull(Long employeId);

    @Query("SELECT a FROM Activite a WHERE a.pointage.employe = :employe AND a.pointage.date BETWEEN :startDate AND :endDate ORDER BY a.debut ASC")
    List<Activite> findByEmployeAndPointageDateBetweenOrderByDebutAsc(@Param("employe") Employe employe, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Activite a WHERE a.pointage.date BETWEEN :startDate AND :endDate")
    List<Activite> findByPointageDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
