package ma.digitalia.suividutemps.services;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.entities.Users;
import ma.digitalia.gestionutilisateur.repositories.UsersRepository;
import ma.digitalia.suividutemps.Enum.Priority;
import ma.digitalia.suividutemps.Enum.TacheStatut;
import ma.digitalia.suividutemps.dto.CreateProjectRequest;
import ma.digitalia.suividutemps.dto.CreateTaskRequest;
import ma.digitalia.suividutemps.dto.ProjectWithTasksDto;
import ma.digitalia.suividutemps.entities.Projet;
import ma.digitalia.suividutemps.entities.Tache;
import ma.digitalia.suividutemps.repositories.ProjetRepository;
import ma.digitalia.suividutemps.repositories.TacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {


    private final ProjetRepository projetRepository;
    private final UsersRepository usersRepository;
    private final TacheRepository tacheRepository;

        public ProjectServiceImpl(ProjetRepository projetRepository, TacheRepository tacheRepository, UsersRepository usersRepository) {
            this.projetRepository = projetRepository;
            this.tacheRepository = tacheRepository;
            this.usersRepository = usersRepository;
        }

        @Transactional
        @Override
        public Projet createProject(CreateProjectRequest request) {
            Projet projet = new Projet();
            projet.setNom(request.nom());
            projet.setClient(request.client());
            projet.setDescription(request.description());
            projet.setDateDebut(request.dateDebut());
            projet.setDateFinPrevue(request.dateFinPrevue());
            projet.setBudget(request.budget());
            return projetRepository.save(projet);
        }

        @Transactional
        @Override
        public Tache createTaskForProject(Long projetId, CreateTaskRequest request) {
            Projet projet = projetRepository.findById(projetId)
                    .orElseThrow(() -> new EntityNotFoundException("Projet non trouvé avec l'id : " + projetId));
            if (request.userId() == null) {
                throw new IllegalArgumentException("L'ID de l'utilisateur est obligatoire pour créer une tâche.");
            }
            Users user = usersRepository.findById(request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + request.userId()));
            Tache tache = new Tache();
            tache.setNom(request.nom());
            tache.setEstimationHeures(request.estimationHeures());
            tache.setDescription(request.description());
            tache.setStatut(TacheStatut.A_FAIRE);
            tache.setProjet(projet);
            tache.setAjoutePar(user);
            log.info("Creating task for project: {} assigned to user: {}", projet.getNom(), user.getPreNom());
            if(request.priority() != null) {
                Priority p = Priority.valueOf(request.priority());
                tache.setPriority(p);
            }

            return tacheRepository.save(tache);
        }

        @Transactional(readOnly = true)
        @Override
        public List<Projet> getAllProjectsWithTasks() {
            // NOTE: Dans un projet réel, utiliser un DTO et/ou un EntityGraph
            // pour éviter les problèmes de N+1 queries.
            return projetRepository.findAll();
        }

    @Override
    public void changeTaskStatus(Long taskId, String status) {
        tacheRepository.findById(taskId).ifPresentOrElse(tache -> {
            try {
                TacheStatut newStatus = TacheStatut.valueOf(status);
                tache.setStatut(newStatus);
                tacheRepository.save(tache);
                log.info("Tâche id {} statut changé en {}", taskId, status);
            } catch (IllegalArgumentException e) {
                log.error("Statut invalide: {}", status);
                throw new IllegalArgumentException("Statut invalide: " + status);
            }
        }, () -> {
            log.error("Tâche non trouvée avec l'id : {}", taskId);
            throw new EntityNotFoundException("Tâche non trouvée avec l'id : " + taskId);
        });
    }

    @Override
    public List<ProjectWithTasksDto> getAllProjectsWithTasksDto() {
        List<Projet> projets = projetRepository.findAll();
        List<ProjectWithTasksDto> dtos = new ArrayList<>();
        for (Projet projet : projets) {
            ProjectWithTasksDto dto = new ProjectWithTasksDto();
            dto.id = projet.getId();
            dto.nom = projet.getNom();
            dto.client = projet.getClient();
            dto.description = projet.getDescription();
            dto.taches = new ArrayList<>();
            if (projet.getTaches() != null) {
                for (Tache tache : projet.getTaches()) {
                    ProjectWithTasksDto.TaskDto tacheDto = new ProjectWithTasksDto.TaskDto();
                    tacheDto.id = tache.getId();
                    tacheDto.nom = tache.getNom();
                    tacheDto.priority = tache.getPriority() != null ? tache.getPriority().name() : null;
                    tacheDto.statut = tache.getStatut() != null ? tache.getStatut().name() : null;
                    dto.taches.add(tacheDto);
                }
            }
            dtos.add(dto);
        }
        return dtos;
    }

}
