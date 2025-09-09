package ma.digitalia.gestionutilisateur.services;

import ma.digitalia.gestionutilisateur.dto.DepartmentEmployeesDTO;
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
     * Find all employees.
     * @return a list of all employees
     */
    List<Employe> findAll();

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
     * @return true if an employee with the given ID exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Find all employees in the department.
     * @return a list of employees in the department
     */
    List<Employe> findByDepartement(String departement);

    /**
     * Get all employees ordered by department.
     * @return a list of employees ordered by department
     */
    List<DepartmentEmployeesDTO> getEmployesByDepartmentDTO();

    /**
     * Update an employee.
     * This method updates the employee's information in the database.
     * @param employe the employee to update
     */
    void updateEmploye(Employe employe);

    void activateEmploye(Long id);


}
