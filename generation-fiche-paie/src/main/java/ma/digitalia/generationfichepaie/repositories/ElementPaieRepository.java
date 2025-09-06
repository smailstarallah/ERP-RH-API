package ma.digitalia.generationfichepaie.repositories;

import ma.digitalia.generationfichepaie.Enum.TypeElement;
import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.gestionutilisateur.entities.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public interface ElementPaieRepository extends JpaRepository<ElementPaie, Long> {

    boolean existsByEmployeAndTypeAndSousType(Employe employe, TypeElement type, String sousType);

    List<ElementPaie> findByEmployeAndTypeAndSousType(Employe employe, TypeElement type, String sousType);

    List<ElementPaie> findByEmploye(Employe employe);


    boolean existsByEmployeAndLibelleContaining(Employe employe,String libelle);

    @Query("SELECT ep.sousType, SUM(ep.montant), COUNT(ep) FROM ElementPaie ep WHERE ep.type = :type AND ep.fichePaie.periode = :periode GROUP BY ep.sousType")
    List<Object[]> getVariableElementsByTypeAndPeriode(@Param("type") TypeElement type, @Param("periode") YearMonth periode);

    @Query("SELECT SUM(ep.montant) FROM ElementPaie ep WHERE ep.type = :type AND ep.fichePaie.periode = :periode")
    BigDecimal getTotalMontantByTypeAndPeriode(@Param("type") TypeElement type, @Param("periode") YearMonth periode);

    @Query("SELECT ep.libelle, SUM(ep.montant), COUNT(ep) FROM ElementPaie ep WHERE ep.type = 'PRIME' AND ep.fichePaie.periode = :periode GROUP BY ep.libelle")
    List<Object[]> getPrimesStatsByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT COUNT(ep) FROM ElementPaie ep WHERE ep.fichePaie.periode = :periode AND (ep.montant IS NULL OR ep.montant < 0)")
    long countElementsErroneesByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT ep.fichePaie.periode, ep.sousType, SUM(ep.montant) FROM ElementPaie ep WHERE ep.type = :type AND ep.fichePaie.periode >= :startPeriode AND ep.fichePaie.periode <= :endPeriode GROUP BY ep.fichePaie.periode, ep.sousType ORDER BY ep.fichePaie.periode")
    List<Object[]> getEvolutionElementsByType(@Param("type") TypeElement type, @Param("startPeriode") YearMonth startPeriode, @Param("endPeriode") YearMonth endPeriode);
}
