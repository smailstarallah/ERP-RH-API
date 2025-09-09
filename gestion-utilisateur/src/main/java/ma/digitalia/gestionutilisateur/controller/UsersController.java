package ma.digitalia.gestionutilisateur.controller;


import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.Enum.UserType;
import ma.digitalia.gestionutilisateur.dto.DepartmentEmployeesDTO;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.repositories.UsersRepository;
import ma.digitalia.gestionutilisateur.services.EmployeService;
import ma.digitalia.gestionutilisateur.services.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UsersController {
    private final EmployeService employeService;
    private final ManagerService managerService;
    private final UsersRepository userRepo;


    public UsersController(EmployeService employeService, UsersRepository userRepo, ManagerService managerService) {
        this.employeService = employeService;
        this.userRepo = userRepo;
        this.managerService = managerService;
    }

    @GetMapping("/employe-par-departments")
    public ResponseEntity<List<DepartmentEmployeesDTO>> getEmployesParDepartements() {
        try {
            log.info("Récupération des employés par département");
            return ResponseEntity.ok(employeService.getEmployesByDepartmentDTO());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/active-users/{id}")
    public ResponseEntity<String> getActiveUsers(@PathVariable("id") Long id) {
        try {
            log.info("Récupération des utilisateurs actifs");
            employeService.activateEmploye(id);
            return ResponseEntity.ok("Utilisateur activé avec succès");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/update-user")
    public ResponseEntity<String> updateUsers(@RequestBody Employe employe) {
        try {
            log.info("Mise à jour des utilisateurs");
            employeService.updateEmploye(employe);
            return ResponseEntity.ok("Utilisateurs mis à jour avec succès");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/get-user/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        try {
            log.info("Récupération des utilisateurs actifs");
            UserType userType = userRepo.findById(id).get().getUserType();
            if (userType == UserType.EMPLOYE)
                return ResponseEntity.ok(employeService.findById(id));
            if (userType == UserType.MANAGER)
                return ResponseEntity.ok(managerService.findById(id));
            if (userType == UserType.RH)
                return ResponseEntity.ok(userRepo.findById(id));
            return ResponseEntity.badRequest().body("User no found");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}
