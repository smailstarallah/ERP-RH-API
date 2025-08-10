package ma.digitalia.gestionutilisateur.services;

import ma.digitalia.gestionutilisateur.entities.Employe;

import java.util.List;


public interface EmployeService {
    /**
     * Save an employee.
     * @param employe
     * @return the saved employee
     */
    Employe save(Employe employe);

    /**
     * Find an employee by ID.
     * @param id
     * @return the employee with the given ID, or null if not found
     */
    Employe findById(Long id);

    /**
     * Update an employee.
     * @param id
     * @param employe
     * @return the updated employee, or null if the employee with the given ID does not exist
     */
    Employe update(Long id, Employe employe);

    /**
     * Find an employee by their department.
     * @param departement
     * @return a list of employees in the specified department
     */
    List<Employe> findByDepartementGere(String departement);

    /**
     * existsById
     * @param id
     * @return
     */
    boolean existsById(Long id);
}
