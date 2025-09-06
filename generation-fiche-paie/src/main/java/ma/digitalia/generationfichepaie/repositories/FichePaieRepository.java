package ma.digitalia.generationfichepaie.repositories;

import ma.digitalia.generationfichepaie.entities.FichePaie;
import ma.digitalia.gestionutilisateur.entities.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.YearMonth;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FichePaieRepository extends JpaRepository<FichePaie, Long> {
    FichePaie findByEmploye(Employe employe);

    FichePaie findByEmployeAndPeriode(Employe employe, java.time.YearMonth periode);

    @Query("SELECT SUM(fp.salaireBrut) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getMasseSalarialeByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT SUM(fp.cotisationsPatronales) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getCotisationsPatronalesByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT SUM(fp.cotisationsSalariales) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getCotisationsSalarialesByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT SUM(fp.salaireNet) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getSalaireNetByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT SUM(fp.impotSurLeRevenu) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getImpotSurLeRevenuByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT SUM(fp.salaireBrutImposable) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getSalaireBrutImposableByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT SUM(fp.salaireNetImposable) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getSalaireNetImposableByPeriode(@Param("periode") YearMonth periode);


    @Query("SELECT COUNT(fp) FROM FichePaie fp WHERE fp.salaireNet BETWEEN :min AND :max AND fp.periode = :periode AND fp.employe.manager.department = :departement")
    int countBySalaireRangeAndPoste(@Param("min") BigDecimal min, @Param("max") BigDecimal max, @Param("periode") YearMonth periode, @Param("departement") String departement);

    @Query("SELECT COUNT(fp) FROM FichePaie fp WHERE fp.periode = :periode AND (fp.salaireNet IS NULL OR fp.salaireNet <= 0 OR fp.salaireBrut IS NULL OR fp.salaireBrut <= 0 OR fp.salaireNet > fp.salaireBrut)")
    int countFichesErroneesByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT COUNT(fp) FROM FichePaie fp WHERE fp.periode = :periode")
    long countTotalFichesByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT fp.periode, SUM(fp.salaireBrut), COUNT(fp) FROM FichePaie fp WHERE fp.periode >= :startPeriode AND fp.periode <= :endPeriode GROUP BY fp.periode ORDER BY fp.periode")
    List<Object[]> getMasseSalarialeEvolution(@Param("startPeriode") YearMonth startPeriode, @Param("endPeriode") YearMonth endPeriode);

    @Query("SELECT AVG(fp.salaireBrut) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getAvgSalaireBrutByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT SUM(fp.impotSurLeRevenu) FROM FichePaie fp WHERE fp.periode = :periode")
    BigDecimal getTotalImpotsByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT fp.employe.poste, COUNT(fp), AVG(fp.salaireNet), MIN(fp.salaireNet), MAX(fp.salaireNet) FROM FichePaie fp WHERE fp.periode = :periode GROUP BY fp.employe.poste")
    List<Object[]> getSalaryDistributionByPoste(@Param("periode") YearMonth periode);

    @Query("SELECT COUNT(fp) FROM FichePaie fp WHERE fp.periode = :periode AND fp.employe.poste LIKE '%cadre%'")
    long countCadresByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT COUNT(fp) FROM FichePaie fp WHERE fp.periode = :periode AND fp.employe.poste LIKE '%technicien%'")
    long countTechniciensByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT COUNT(fp) FROM FichePaie fp WHERE fp.periode = :periode AND fp.employe.poste NOT LIKE '%cadre%' AND fp.employe.poste NOT LIKE '%technicien%'")
    long countEmployesByPeriode(@Param("periode") YearMonth periode);

    @Query("SELECT fp.salaireBrut FROM FichePaie fp WHERE fp.employe.manager.department = :departement AND fp.periode = :periode")
    List<Object[]> getSalariesByDepartmentAndPeriode(@Param("departement") String departement, @Param("periode") YearMonth periode);
}
