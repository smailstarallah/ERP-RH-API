package ma.digitalia.gestionutilisateur.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.dto.DepartmentEmployeesDTO;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import ma.digitalia.gestionutilisateur.repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeServiceImpl implements EmployeService {

    EmployeRepository empRepo;

    public EmployeServiceImpl(EmployeRepository empRepo) {
        this.empRepo = empRepo;
    }


    @Override
    public Employe save(Employe employe) {
        return empRepo.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        if (empRepo.existsById(id)) {
            return (Employe) empRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Employe not found with id: " + id));
        } else {
            throw new EntityNotFoundException("Employe not found with id: " + id);
        }
    }

    @Override
    public List<Employe> findAll() {
        return empRepo.findAllByActive(true);
    }

    @Override
    public Employe update(Long id, Employe employe) {
        return null;
    }

    @Override
    public List<Employe> findByDepartementGere(String departement) {
        return List.of();
    }

    @Override
    public boolean existsById(Long id) {
        return empRepo.existsById(id);
    }

    @Override
    public List<Employe> findByDepartement(String departement) {
        try {
            if (departement != null && !departement.isEmpty()) {
                return empRepo.findByDepartement(departement);
            }
            log.warn("Department is null or empty, returning empty list.");
            throw new Exception("Department is null or empty, returning empty list.");
        } catch (Exception e) {
            log.error("Error while fetching employees by department: {}", departement, e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, List<Employe>> getEmployesGroupedByDepartment() {
        List<Employe> allEmployes = empRepo.findAllEmployesOrderByDepartment();

        return allEmployes.stream()
                .filter(e -> e.getManager() != null && e.getManager().getDepartment() != null)
                .collect(Collectors.groupingBy(e -> e.getManager().getDepartment()));
    }

    @Override
    public List<DepartmentEmployeesDTO> getEmployesByDepartmentDTO() {
        Map<String, List<Employe>> groupedEmployes = getEmployesGroupedByDepartment();

        return groupedEmployes.entrySet().stream()
                .map(entry -> new DepartmentEmployeesDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateEmploye(Employe employe) {
        if (employe == null || employe.getId() == null) {
            throw new EntityNotFoundException("Employe or Employe ID cannot be null");
        }
        if (!empRepo.existsById(employe.getId())) {
            throw new EntityNotFoundException("Employe not found with id: " + employe.getId());
        }
        Employe existEmploye = (Employe) empRepo.findById(employe.getId()).get();

        existEmploye.setNom(employe.getNom());
        existEmploye.setPreNom(employe.getPreNom());
        existEmploye.setEmail(employe.getEmail());
        existEmploye.setDateNaissance(employe.getDateNaissance());
        existEmploye.setDateEmbauche(employe.getDateEmbauche());
        existEmploye.setTelephone(employe.getTelephone());
        existEmploye.setSalairBase(employe.getSalairBase());
        existEmploye.setTauxHoraire(employe.getTauxHoraire());
        existEmploye.setAdresse(employe.getAdresse());
        existEmploye.setPoste(employe.getPoste());
        existEmploye.setCin(employe.getCin());
    }

    @Override
    @Transactional
    public void activateEmploye(Long id) {
        if (empRepo.existsById(id)) {
            Employe employe = (Employe) empRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Employe not found with id: " + id));
            employe.setActive(!employe.isActive());
        } else {
            throw new EntityNotFoundException("Employe not found with id: " + id);
        }
    }

}
