package ma.digitalia.gestionconges.services;

import ma.digitalia.gestionconges.entities.TypeConge;

import java.util.List;
import java.util.Map;

public interface TypeCongeService {

    /**
     * Finds a TypeConge by its ID.
     * @param id
     * @return
     */
    TypeConge findById(Long id);

    List<Map<Long, String>> getTypeConges();
    /**
     * Retrieves all TypeConge entities.
     * @return a list of TypeConge
     */
    List<TypeConge> getAllTypeConges();

    void save(TypeConge typeConge);

    /**
     * Deletes a TypeConge by its ID.
     * @param id the ID of the TypeConge to delete
     */
    void deleteById(Long id);
}
