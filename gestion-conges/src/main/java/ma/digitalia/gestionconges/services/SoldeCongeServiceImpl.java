package ma.digitalia.gestionconges.services;

import jakarta.persistence.EntityExistsException;
import ma.digitalia.gestionconges.dto.SoldeCongeDTO;
import ma.digitalia.gestionconges.entities.SoldeConge;
import ma.digitalia.gestionconges.repositories.SoldeCongeRepository;
import ma.digitalia.gestionutilisateur.services.EmployeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SoldeCongeServiceImpl implements SoldeCongeService {

    private final EmployeService employeService;
    SoldeCongeRepository soldeCongeRepository;

    public SoldeCongeServiceImpl(SoldeCongeRepository soldeCongeRepository, EmployeService employeService) {
        this.soldeCongeRepository = soldeCongeRepository;
        this.employeService = employeService;
    }

    @Override
    public void mettreAJourSoldeConges(Long userId, double nouveauSolde) {

    }

    @Override
    public List<SoldeCongeDTO> recupererSoldeCongesRestant(Long userId) {
        List<SoldeConge> soldes = soldeCongeRepository.findByEmploye(employeService.findById(userId));
        if (soldes != null && !soldes.isEmpty()) {
            List<SoldeCongeDTO> soldeCongeDTO = soldes.stream()
                    .map(SoldeCongeDTO::new)
                    .collect(Collectors.toList());
            System.out.println("Solde de congés récupéré avec succès pour l'utilisateur ID: " + userId);
            System.out.println("Solde de congés: " + soldeCongeDTO);
            return soldeCongeDTO;
        }
        throw new EntityExistsException("Le soldeConge n'existe pas");
    }
}
