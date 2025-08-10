package ma.digitalia.gestionutilisateur.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.entities.Manager;
import ma.digitalia.gestionutilisateur.repositories.ManagerRepository;
import ma.digitalia.gestionutilisateur.repositories.UsersRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ManagerServiceImpl implements ManagerService {

    ManagerRepository managerRepository;
    UsersRepository usersRepository;

    public ManagerServiceImpl(ManagerRepository managerRepository, UsersRepository usersRepository) {
        this.managerRepository = managerRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    public Manager save(Manager manager) {
        Manager savedManager = null;
        if (manager != null) {
            savedManager = managerRepository.save(manager);
        }
        return savedManager;
    }

    @Override
    public Manager findByEmail(String email) {
    if (email != null && !email.isEmpty()) {
            return (Manager) usersRepository.findByEmail(email).orElseThrow( () ->
                new EntityNotFoundException("Manager with email " + email + " not found"));
        }
        return null;
    }

    @Override
    public Manager findById(Long id) {
        log.info("Recherche du manager avec l'ID: {}", id);

        Manager manager = (Manager) managerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Manager avec l'ID {} introuvable", id);
                    return new EntityNotFoundException("Manager with id " + id + " not found");
                });

        log.info("Manager trouvé: {}", manager);
        return manager;
    }

    @Override
    public boolean existsById(Long id) {
        return managerRepository.existsById(id);
    }

}
