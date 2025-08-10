package ma.digitalia.gestionutilisateur.services;

import ma.digitalia.gestionutilisateur.entities.Manager;

public interface ManagerService {

    /**
     * Saves a manager entity.
     *
     * @param manager the manager entity to save
     * @return the saved manager entity
     */
    Manager save(Manager manager);

    /**
     * Finds a manager by their email.
     *
     * @param email the email of the manager to find
     * @return the found manager entity, or null if not found
     */
    Manager findByEmail(String email);

    /**
     * find a manager by their ID.
     * @param id the ID of the manager to find
     * @return the found manager entity, or null if not found
     */
    Manager findById(Long id);

    /**
     * Checks if a manager exists by their ID.
     *
     * @param id
     * @return
     */
    boolean existsById(Long id);
}
