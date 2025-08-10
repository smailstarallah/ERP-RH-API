package ma.digitalia.gestionconges.repositories;

import ma.digitalia.gestionconges.entities.TypeConge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TypeCongeRpository extends JpaRepository<TypeConge, Long> {

    @Query("SELECT t.id, t.nom FROM TypeConge t")
    List<Object[]> getIdAndNomAsArray();
}
