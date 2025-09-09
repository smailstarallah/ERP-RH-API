package ma.digitalia.gestionconges.services;

import ma.digitalia.gestionconges.Enum.StatutDemande;
import ma.digitalia.gestionconges.dto.CongesPrisDTO;
import ma.digitalia.gestionconges.entities.DemandeConge;
import ma.digitalia.gestionconges.repositories.DemandeCongeRepository;
import ma.digitalia.gestionutilisateur.Enum.UserType;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Users;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import ma.digitalia.gestionutilisateur.repositories.UsersRepository;
import ma.digitalia.gestionutilisateur.services.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CongesPrisServiceImpl implements CongesPrisService {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ManagerService managerService;

    @Override
    public List<CongesPrisDTO> getAllCongesPris(Long userId) {
        Users currentUser = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if(currentUser.getUserType() == UserType.RH)
            return getCongesPrisByAnnee(LocalDate.now().getYear());
        if(currentUser.getUserType() == UserType.MANAGER) {
            return getCongesPrisByValidateur(userId, LocalDate.now().getYear());
        } else {
            return getCongesPrisByEmploye(currentUser.getId());
        }
    }

    @Override
    public List<CongesPrisDTO> getCongesPrisByAnnee(int annee) {
        List<DemandeConge> congesValidees = demandeCongeRepository.findByStatut(StatutDemande.VALIDEE)
                .stream()
                .filter(demande -> demande.getDateDebut().getYear() == annee)
                .collect(Collectors.toList());

        return convertToDTO(congesValidees);
    }

    public List<CongesPrisDTO> getCongesPrisByValidateur(Long ValidateurId, int annee) {
        List<DemandeConge> congesValidees = demandeCongeRepository.findDemandeCongeByValidateur(managerService.findById(ValidateurId))
                .stream()
                .filter(demande -> demande.getDateDebut().getYear() == annee)
                .collect(Collectors.toList());

        return convertToDTO(congesValidees);
    }
    @Override
    public List<CongesPrisDTO> getCongesPrisByEmploye(Long employeId) {
        Employe employe = (Employe) employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        List<DemandeConge> congesEmploye = demandeCongeRepository.findDemandeCongeByDemandeur(employe)
                .stream()
                .filter(demande -> demande.getStatut() == StatutDemande.VALIDEE)
                .collect(Collectors.toList());

        return convertToDTO(congesEmploye);
    }

    @Override
    public List<CongesPrisDTO> getCongesPrisByDepartement(String departement) {
        List<Employe> employees = employeRepository.findByDepartement(departement);

        List<DemandeConge> congesDepartement = employees.stream()
                .flatMap(employe -> demandeCongeRepository.findDemandeCongeByDemandeur(employe).stream())
                .filter(demande -> demande.getStatut() == StatutDemande.VALIDEE)
                .collect(Collectors.toList());

        return convertToDTO(congesDepartement);
    }

    @Override
    public List<CongesPrisDTO> getCongesPrisByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        List<DemandeConge> congesPeriode = demandeCongeRepository.findByStatut(StatutDemande.VALIDEE)
                .stream()
                .filter(demande ->
                    !demande.getDateFin().isBefore(dateDebut) &&
                    !demande.getDateDebut().isAfter(dateFin))
                .collect(Collectors.toList());

        return convertToDTO(congesPeriode);
    }

    @Override
    public List<CongesPrisDTO> getCongesPrisByType(String typeConge) {
        List<DemandeConge> congesParType = demandeCongeRepository.findByStatut(StatutDemande.VALIDEE)
                .stream()
                .filter(demande -> demande.getTypeConge().getNom().equalsIgnoreCase(typeConge))
                .collect(Collectors.toList());

        return convertToDTO(congesParType);
    }

    @Override
    public int getTotalJoursCongesPris(Long employeId, int annee) {
        return getCongesPrisByEmploye(employeId).stream()
                .filter(conge -> conge.getDateDebut().getYear() == annee)
                .mapToInt(CongesPrisDTO::getNombreJours)
                .sum();
    }

    @Override
    public int getTotalJoursCongesPrisByDepartement(String departement, int annee) {
        return getCongesPrisByDepartement(departement).stream()
                .filter(conge -> conge.getDateDebut().getYear() == annee)
                .mapToInt(CongesPrisDTO::getNombreJours)
                .sum();
    }

    /**
     * Convertit une liste de DemandeConge en CongesPrisDTO
     */
    private List<CongesPrisDTO> convertToDTO(List<DemandeConge> demandes) {
        return demandes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une DemandeConge en CongesPrisDTO
     */
    private CongesPrisDTO convertToDTO(DemandeConge demande) {
        String couleurType = demande.getTypeConge().getCouleur() != null ?
                           demande.getTypeConge().getCouleur() :
                           getDefaultColor(demande.getTypeConge().getNom());

        String departement = null;
        if (demande.getDemandeur().getManager() != null) {
            departement = demande.getDemandeur().getManager().getDepartment();
        }

        return new CongesPrisDTO(
                demande.getId(),
                demande.getDemandeur().getNom(),
                demande.getDemandeur().getPreNom(),
                departement,
                demande.getTypeConge().getNom(),
                demande.getDateDebut(),
                demande.getDateFin(),
                demande.getNombreJours(),
                demande.getMotif(),
                demande.getDateTraitement() != null ? demande.getDateTraitement().toLocalDate() : null,
                couleurType
        );
    }

    /**
     * Retourne une couleur par défaut basée sur le nom du type de congé
     */
    private String getDefaultColor(String typeName) {
        switch (typeName.toLowerCase()) {
            case "congé":
            case "congés payés":
                return "#3B82F6";
            case "rtt":
                return "#10B981";
            case "maladie":
                return "#EF4444";
            case "formation":
                return "#F59E0B";
            default:
                return "#6B7280";
        }
    }
}
