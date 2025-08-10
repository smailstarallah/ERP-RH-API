package ma.digitalia.gestionutilisateur.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeServiceImpl implements EmployeService {

    EmployeRepository empRepo;

    public EmployeServiceImpl(EmployeRepository empRepo) {
        this.empRepo = empRepo;
    }


    @Override
    public Employe save(Employe employe) {
        Employe emp = empRepo.save(employe);
        return emp;
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
}
