package ma.digitalia.generationfichepaie.services;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.generationfichepaie.Enum.ModeCalcul;
import ma.digitalia.generationfichepaie.Enum.TypeElement;
import ma.digitalia.generationfichepaie.dto.AjoutElementPaieDTO;
import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.generationfichepaie.entities.FichePaie;
import ma.digitalia.generationfichepaie.helpers.FichePaiePdfGenerateur;
import ma.digitalia.generationfichepaie.repositories.ElementPaieRepository;
import ma.digitalia.generationfichepaie.repositories.FichePaieRepository;
import ma.digitalia.gestionutilisateur.entities.Employe;
import ma.digitalia.gestionutilisateur.entities.Users;
import ma.digitalia.gestionutilisateur.repositories.EmployeRepository;
import ma.digitalia.gestionutilisateur.repositories.UsersRepository;
import ma.digitalia.gestionutilisateur.services.ManagerService;
import ma.digitalia.suividutemps.entities.RapportTemps;
import ma.digitalia.suividutemps.services.RapportTempsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ma.digitalia.generationfichepaie.Enum.StatutPaie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ma.digitalia.generationfichepaie.Enum.TypeElement.*;

@Slf4j
@Service
public class GenerationFichePaieServiceImpl implements GenerationFichePaieService {

    private final ElementPaieRepository elementPaieRepository;
    private final EmployeRepository employeRepository;
    private final RapportTempsService rapportTempsService;
    private final FichePaieRepository fichePaieRepository;
    private final UsersRepository usersRepository;
    private final ManagerService managerService;


    public GenerationFichePaieServiceImpl(ElementPaieRepository elementPaieRepository, EmployeRepository employeRepository,
                                          RapportTempsService rapportTempsService, FichePaieRepository fichePaieRepository,
                                          UsersRepository usersRepository, ManagerService managerService) {
        this.elementPaieRepository = elementPaieRepository;
        this.employeRepository = employeRepository;
        this.rapportTempsService = rapportTempsService;
        this.fichePaieRepository = fichePaieRepository;
        this.usersRepository = usersRepository;
        this.managerService = managerService;
    }

//    @PostConstruct
//    public void init() {
//        try {
//            genererFichePaie(2L);
//        } catch (Exception e) {
//            log.error("Erreur lors de la génération de la fiche de paie : {}", e.getMessage());
//        }
//    }

    @Override
    public void ajouterElementPaie(Long employeId, ElementPaie elementPaie) {
        log.info("Ajout d'un elementPaie {} pour l'employé avec l'ID : {}", elementPaie.getType(), employeId);

        Employe employe = (Employe) employeRepository.findById(employeId)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId));
        if (elementPaieRepository.existsByEmployeAndTypeAndSousType(employe, elementPaie.getType(), elementPaie.getSousType())) {
            List<ElementPaie> elements = elementPaieRepository.findByEmployeAndTypeAndSousType(employe, elementPaie.getType(), elementPaie.getSousType());
            for (ElementPaie e : elements) {
                if (e.getSousType() != null && e.getSousType().equals(elementPaie.getSousType())) {
                    throw new IllegalArgumentException("L'élément de paie existe déjà pour cet employé.");
                }
            }
        }

        if (elementPaie.getEmploye() == null) {
            elementPaie.setEmploye(employe);
        }
        if (elementPaie.getMontant() == null) {
            elementPaie.setMontant(calculerMontant(elementPaie, YearMonth.now()));
        }
        elementPaieRepository.save(elementPaie);
        log.info("Élément de paie ajouté pour l'employé avec l'ID : {}", employeId);
    }

    @Override
    public void ajouterElementPaie(Long employeId, AjoutElementPaieDTO elementPaie) {
        log.info("Ajout d'un elementPaie {} pour l'employé avec l'ID : {}", elementPaie.getType(), employeId);

        Employe employe = (Employe) employeRepository.findById(employeId)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId));

        if (elementPaieRepository.existsByEmployeAndTypeAndSousType(employe, elementPaie.getType(), elementPaie.getSousType())) {

            List<ElementPaie> elements = elementPaieRepository.findByEmployeAndTypeAndSousType(employe, elementPaie.getType(), elementPaie.getSousType());

            for (ElementPaie e : elements) {
                if (e.getSousType() != null && e.getSousType().equals(elementPaie.getSousType())) {
                    throw new IllegalArgumentException("L'élément de paie existe déjà pour cet employé.");
                }
            }
        }

        ElementPaie newElementPaie = new ElementPaie(elementPaie, employe);
        newElementPaie.setMontant(calculerMontant(newElementPaie, YearMonth.now()));
        elementPaieRepository.save(newElementPaie);
        log.info("Élément de paie ajouté pour l'employé avec l'ID : {}", employeId);
    }

    @Override
    public List<ElementPaie> recupererElementPaie(Long employeId) {
        log.info("Récupération de l'élément de paie pour l'employé avec l'ID : {}", employeId);
        Employe employe = (Employe) employeRepository.findById(employeId)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId));
        List<ElementPaie> elements = elementPaieRepository.findByEmploye(employe);
        if (elements.isEmpty()) {
            log.warn("Aucun élément de paie trouvé pour l'employé avec l'ID : {}", employeId);
            return new ArrayList<>();
        }
        log.info("Éléments de paie récupérés pour l'employé avec l'ID : {}", employeId);
        return elements;
    }

    public BigDecimal calculerMontant(ElementPaie elementPaie, YearMonth periode) {
        if (elementPaie.getModeCalcul() == null) return BigDecimal.ZERO;
        switch (elementPaie.getModeCalcul()) {
            case MONTANT:
                return elementPaie.getMontant() != null ? elementPaie.getMontant() : BigDecimal.ZERO;
            case TAUX:
                if (elementPaie.getTaux() != null && elementPaie.getBase() != null) {
                    return elementPaie.getBase().multiply(elementPaie.getTaux()).divide(BigDecimal.valueOf(100));
                }
                return BigDecimal.ZERO;
            case PAR_HEURE:

                if (elementPaie.getType() == HEURES_SUPPLEMENTAIRES) {
                    RapportTemps rapportTemps = rapportTempsService.getMonthlyReport(elementPaie.getEmploye(), YearMonth.now());
                    BigDecimal nombreHeuresSupplementaires = new BigDecimal(String.valueOf(rapportTemps.getTotalHeuresSupplementaires().toHours()));
                    System.out.println("éééééééééééééééééé");
                    System.out.println(rapportTemps.getTotalHeuresSupplementaires().toHours() + " heures" + elementPaie.getTaux());
                    return nombreHeuresSupplementaires.multiply(elementPaie.getTaux());
                }

                if (elementPaie.getType() == DEDUCTION_RETARD) {
                    RapportTemps rapportTemps = rapportTempsService.getMonthlyReport(elementPaie.getEmploye(), YearMonth.now());
                    BigDecimal nombreJoursAbsence = new BigDecimal(rapportTemps.getNombreRetards());
                    System.out.println("éééééééééééééééééé");
                    System.out.println(rapportTemps.getNombreRetards());
                    return nombreJoursAbsence.multiply(elementPaie.getTaux());
                }

                if (elementPaie.getTaux() != null && elementPaie.getBase() != null) {
                    return elementPaie.getBase().multiply(elementPaie.getTaux()).divide(BigDecimal.valueOf(100));
                }
                return BigDecimal.ZERO;
            case PAR_JOUR:
                if (elementPaie.getType() == DEDUCTION_ABSENCE) {
                    BigDecimal nombreAbs = rapportTempsService.getNombreAbsences(elementPaie.getEmploye(), periode);
                    System.out.println("éééééééééééééééééé");
                    System.out.println(rapportTempsService.getNombreAbsences(elementPaie.getEmploye(), periode));
                    return nombreAbs.multiply(elementPaie.getTaux());
                }
                if (elementPaie.getTaux() != null && elementPaie.getBase() != null) {
                    return elementPaie.getBase().multiply(elementPaie.getTaux()).divide(BigDecimal.valueOf(100));
                }
                return BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional
    public void mettreAJourElementPaie(Long employeId, Long elementPaieId, ElementPaie elementPaie) {
        log.info("Mise à jour de l'élément de paie {} pour l'employé avec l'ID : {}", elementPaieId, employeId);
        Employe employe = (Employe) employeRepository.findById(employeId)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId));
        ElementPaie existingElementPaie = elementPaieRepository.findById(elementPaieId)
                .orElseThrow(() -> new EntityNotFoundException("Élément de paie non trouvé avec l'ID : " + elementPaieId));
        if (!existingElementPaie.getEmploye().equals(employe)) {
            throw new IllegalArgumentException("L'élément de paie n'appartient pas à l'employé spécifié.");
        }
        existingElementPaie.setType(elementPaie.getType());
        existingElementPaie.setSousType(elementPaie.getSousType());
        existingElementPaie.setLibelle(elementPaie.getLibelle());
        existingElementPaie.setModeCalcul(elementPaie.getModeCalcul());
        existingElementPaie.setMontant(elementPaie.getMontant());
        existingElementPaie.setTaux(elementPaie.getTaux());
        existingElementPaie.setBase(elementPaie.getBase());
        existingElementPaie.setDescription(elementPaie.getDescription());
    }

    @Override
    @Transactional
    public void genererEtSauvegarderFichePdf(FichePaie fichePaie) {
        byte[] pdf = FichePaiePdfGenerateur.genererPdf(fichePaie);
        fichePaie.setPdfFile(pdf);
        fichePaieRepository.save(fichePaie);
    }

    @Override
    public byte[] recupererFichePaiePdf(Long employeId, YearMonth periode) {
        Employe employe = (Employe) employeRepository.findById(employeId)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId));
        FichePaie fichePaie = fichePaieRepository.findByEmployeAndPeriode(employe, periode);
        if (fichePaie != null && fichePaie.getPdfFile() != null) {
            return fichePaie.getPdfFile();
        }
        genererFichePaie(employeId, periode);
        fichePaie = fichePaieRepository.findByEmployeAndPeriode(employe, periode);
        return fichePaie.getPdfFile();
    }

    @Override
//    @Transactional
    public void genererFichePaie(Long employeId, YearMonth yearMonth) {

        FichePaie fichePaie = new FichePaie();

        Employe employe = (Employe) employeRepository.findById(employeId)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé avec l'ID : " + employeId));

        fichePaie.setEmploye(employe);
        fichePaie.setStatut(StatutPaie.BROUILLON);

        RapportTemps rapportTemps = rapportTempsService.getMonthlyReport(employe, yearMonth);

        if (rapportTemps == null) {
            log.warn("Aucun rapport de temps trouvé pour l'employé {} et la période {}", employe.getId(), yearMonth);
            rapportTempsService.generateMonthlyReport(employeId, yearMonth.getMonth());
            rapportTemps = rapportTempsService.getMonthlyReport(employe, yearMonth);

        }

        fichePaie.setJoursTravailles(rapportTemps.getNombreJoursTravail());
        fichePaie.setHeuresSupplementaires((int) rapportTemps.getTotalHeuresSupplementaires().toHours());


        List<ElementPaie> elements = elementPaieRepository.findByEmploye(employe);

        List<ElementPaie> attachedElements = new ArrayList<>();
        for (ElementPaie element : elements) {
            attachedElements.add(elementPaieRepository.findById(element.getId()).orElseThrow());
        }

        fichePaie.setElements(attachedElements);

        Set<String> tests = new java.util.HashSet<>();

        log.info("Génération de la fiche de paie pour l'employé {} : {}", employe.getId(), tests);
        fichePaie.setPeriode(yearMonth);
        fichePaie.setDateGeneration(java.time.LocalDateTime.now());
        fichePaie.setSalaireBrut(calculerSalaireBrut(fichePaie));
        fichePaie.setSalaireBrutImposable(calculerSalaireBrutImposable(fichePaie));
        fichePaie.setCotisationsSalariales(calculerCotisationSalariale(fichePaie));
        fichePaie.setCotisationsPatronales(calculerCotisationPatronale(fichePaie));
        fichePaie.setSalaireNetImposable(fichePaie.getSalaireBrutImposable().subtract(fichePaie.getCotisationsSalariales()));
        //                                                          --> nombre de personne à charge
        fichePaie.setImpotSurLeRevenu(calculerImpotSurLeRevenu(fichePaie, 2));
        fichePaie.setSalaireNet(calculerSalaireNet(fichePaie));

        tests.forEach(log::info);
        System.out.println(fichePaie);

        List<ElementPaie> newElements = elementPaieRepository.findByEmploye(employe);

        List<ElementPaie> newAttachedElements = new ArrayList<>();
        for (ElementPaie element : newElements) {
            newAttachedElements.add(elementPaieRepository.findById(element.getId()).orElseThrow());
        }

        fichePaie.setElements(newAttachedElements);

        fichePaie.setPdfFile(FichePaiePdfGenerateur.genererPdf(fichePaie));

        fichePaieRepository.save(fichePaie);
        log.info("Fiche de paie générée pour l'employé {} : {}", employe.getId(), fichePaie);
    }

    BigDecimal calculerSalaireBrut(FichePaie fichePaie) {
        BigDecimal salaireBrute = BigDecimal.ZERO;
        for (ElementPaie element : fichePaie.getElements()) {
            switch (element.getType()) {
                case SALAIRE_BASE:
                case PRIME_FIXE:
                case PRIME_VARIABLE:
                case HEURES_SUPPLEMENTAIRES:
                case INDEMNITE:
                    salaireBrute = salaireBrute.add(element.getMontant());
                    break;
                case DEDUCTION_ABSENCE:
                case DEDUCTION_AUTRE:
                    salaireBrute = salaireBrute.subtract(element.getMontant());
                    break;
                default:
                    // Ignore other types for salary calculation
                    break;
            }
        }
        return salaireBrute;
    }

    BigDecimal calculerSalaireBrutImposable(FichePaie fichePaie) {
        return fichePaie.getElements().stream()
                .filter(ElementPaie::isSoumisIR)
                .map(ElementPaie::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal calculerCotisationSalariale(FichePaie fichePaie) {
        BigDecimal cotisationsSalariales = BigDecimal.ZERO;
        BigDecimal baseCNSS;
        BigDecimal soumisCNSS = fichePaie.getElements().stream()
                .filter(ElementPaie::isSoumisCNSS)
                .map(ElementPaie::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retenue = fichePaie.getElements().stream()
                .filter(elementPaie -> elementPaie.getType() == DEDUCTION_ABSENCE || elementPaie.getType() == DEDUCTION_AUTRE || elementPaie.getType() == DEDUCTION_RETARD)
                .map(ElementPaie::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        baseCNSS = soumisCNSS.subtract(retenue);

        BigDecimal plafond = new BigDecimal("6000");
        BigDecimal tauxCNSS = new BigDecimal("0.0448");
        BigDecimal tauxAMO = new BigDecimal("0.0226");

        BigDecimal cotisationCNSS;
        BigDecimal cotisationAMO;

        if (baseCNSS.compareTo(plafond) > 0) {
            cotisationCNSS = plafond.multiply(tauxCNSS);
            cotisationAMO = plafond.multiply(tauxAMO);
        } else {
            cotisationCNSS = baseCNSS.multiply(tauxCNSS);
            cotisationAMO = baseCNSS.multiply(tauxAMO);
        }
            log.info("Ajout des cotisations CNSS à la fiche de paie.");
            ElementPaie cnssElement = new ElementPaie(null, TypeElement.COTISATION_SOCIALE, "Cotisation CNSS", "Cotisation CNSS", ModeCalcul.TAUX, cotisationCNSS
                    , tauxCNSS.multiply(BigDecimal.valueOf(100)), baseCNSS, "Cotisation CNSS calculée automatiquement", false, false, null, null);
            if (!elementPaieRepository.existsByEmployeAndLibelleContaining(fichePaie.getEmploye(), "Cotisation CNSS")) {
            ajouterElementPaie(fichePaie.getEmploye().getId(), cnssElement);
        }

            log.info("Ajout des cotisations AMO à la fiche de paie.");
            ElementPaie amoElement = new ElementPaie(null, TypeElement.COTISATION_SOCIALE, "cotisation AMO", "Cotisation AMO", ModeCalcul.TAUX, cotisationAMO
                    , tauxAMO.multiply(BigDecimal.valueOf(100)), baseCNSS, "Cotisation AMO calculée automatiquement", false, false, null, null);
            if (!elementPaieRepository.existsByEmployeAndLibelleContaining(fichePaie.getEmploye(), "Cotisation AMO")) {

            ajouterElementPaie(fichePaie.getEmploye().getId(), amoElement);
        }
        cotisationsSalariales = cotisationsSalariales.add(cotisationCNSS).add(cotisationAMO);
        return cotisationsSalariales;
    }

    BigDecimal calculerCotisationPatronale(FichePaie fichePaie) {
        BigDecimal cotisationsPatronales = BigDecimal.ZERO;

        // Base brute soumise CNSS
        BigDecimal soumisCNSS = fichePaie.getElements().stream()
                .filter(ElementPaie::isSoumisCNSS)
                .map(ElementPaie::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal retenue = fichePaie.getElements().stream()
                .filter(elementPaie -> elementPaie.getType() == DEDUCTION_ABSENCE || elementPaie.getType() == DEDUCTION_AUTRE)
                .map(ElementPaie::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal baseCNSS = soumisCNSS.subtract(retenue);

        BigDecimal plafond = new BigDecimal("6000");

        // Taux patronaux
        BigDecimal tauxCNSSPatronale = new BigDecimal("0.0898"); // 8,98%
        BigDecimal tauxAMOPatronale = new BigDecimal("0.0411");  // 4,11%
        BigDecimal tauxFormationPro = new BigDecimal("0.016");   // 1,6%
        BigDecimal tauxAllocationsFamiliales = new BigDecimal("0.064"); // 6,4%

        // Cotisations
        BigDecimal cotisationCNSSPatronale;
        BigDecimal cotisationAMOPatronale;
        BigDecimal cotisationFormationPro;
        BigDecimal cotisationAllocationsFamiliales;

        if (baseCNSS.compareTo(plafond) > 0) {
            cotisationCNSSPatronale = plafond.multiply(tauxCNSSPatronale);
            cotisationAllocationsFamiliales = plafond.multiply(tauxAllocationsFamiliales);
        } else {
            cotisationCNSSPatronale = baseCNSS.multiply(tauxCNSSPatronale);
            cotisationAllocationsFamiliales = baseCNSS.multiply(tauxAllocationsFamiliales);
        }

        // AMO & Formation Pro n’ont généralement pas de plafond
        cotisationAMOPatronale = baseCNSS.multiply(tauxAMOPatronale);
        cotisationFormationPro = baseCNSS.multiply(tauxFormationPro);

        log.info("Base CNSS pour les cotisations patronales : {}", baseCNSS);
        log.info("Cotisation CNSS Patronale : {}", cotisationCNSSPatronale);
        log.info("Cotisation AMO Patronale : {}", cotisationAMOPatronale);
        log.info("Cotisation Formation Professionnelle : {}", cotisationFormationPro);
        log.info("Cotisation Allocations Familiales : {}", cotisationAllocationsFamiliales);

            log.info("Ajout des cotisations CNSS Patronale à la fiche de paie.");
            ElementPaie cnssPat = new ElementPaie(null, TypeElement.COTISATION_SOCIALE,
                    "Cotisation CNSS Patronale", "CNSS Patronale", ModeCalcul.TAUX, cotisationCNSSPatronale,
                    tauxCNSSPatronale.multiply(BigDecimal.valueOf(100)), baseCNSS,
                    "Cotisation CNSS part employeur", false, false, null, null);
        if (!elementPaieRepository.existsByEmployeAndLibelleContaining(fichePaie.getEmploye(), "CNSS Patronale")) {
            ajouterElementPaie(fichePaie.getEmploye().getId(), cnssPat);
        }

            log.info("Ajout des cotisations AMO Patronale à la fiche de paie.");
            ElementPaie amoPat = new ElementPaie(null, TypeElement.COTISATION_SOCIALE,
                    "Cotisation AMO Patronale", "AMO Patronale", ModeCalcul.TAUX, cotisationAMOPatronale,
                    tauxAMOPatronale.multiply(BigDecimal.valueOf(100)), baseCNSS,
                    "Cotisation AMO part employeur", false, false, null, null);
            if (!elementPaieRepository.existsByEmployeAndLibelleContaining(fichePaie.getEmploye(), "AMO Patronale")) {
            ajouterElementPaie(fichePaie.getEmploye().getId(), amoPat);
        }

            log.info("Ajout des cotisations Formation Professionnelle à la fiche de paie.");
            ElementPaie formPro = new ElementPaie(null, TypeElement.COTISATION_SOCIALE,
                    "Cotisation Formation Professionnelle", "Formation Pro", ModeCalcul.TAUX, cotisationFormationPro,
                    tauxFormationPro.multiply(BigDecimal.valueOf(100)), baseCNSS,
                    "Cotisation Formation Pro part employeur", false, false, null, null);
            if (!elementPaieRepository.existsByEmployeAndLibelleContaining(fichePaie.getEmploye(), "Formation Pro")) {
            ajouterElementPaie(fichePaie.getEmploye().getId(), formPro);
        }

            log.info("Ajout des cotisations Allocations Familiales à la fiche de paie.");
            ElementPaie alloc = new ElementPaie(null, TypeElement.COTISATION_SOCIALE,
                    "Cotisation Allocations Familiales", "Allocations Familiales", ModeCalcul.TAUX, cotisationAllocationsFamiliales,
                    tauxAllocationsFamiliales.multiply(BigDecimal.valueOf(100)), baseCNSS,
                    "Cotisation Allocations Familiales part employeur", false, false, null, null);
            if (!elementPaieRepository.existsByEmployeAndLibelleContaining(fichePaie.getEmploye(), "Allocations Familiales")) {
            ajouterElementPaie(fichePaie.getEmploye().getId(), alloc);
        }

        cotisationsPatronales = cotisationsPatronales
                .add(cotisationCNSSPatronale)
                .add(cotisationAMOPatronale)
                .add(cotisationFormationPro)
                .add(cotisationAllocationsFamiliales);

        return cotisationsPatronales;
    }


    BigDecimal calculerSalaireNet(FichePaie fichePaie) {
        BigDecimal salaireNet;
        salaireNet = fichePaie.getSalaireBrutImposable()
                .subtract(fichePaie.getCotisationsSalariales())
                .subtract(fichePaie.getImpotSurLeRevenu());
        return salaireNet;
    }

    public BigDecimal calculerImpotSurLeRevenu(FichePaie fichePaie, int nombrePersonnesCharge) {
        BigDecimal salaireBrutImposable = fichePaie.getSalaireBrutImposable();
        BigDecimal salaireAnnuelImposable = salaireBrutImposable.multiply(BigDecimal.valueOf(12));

        // 1. Frais professionnels : 20% plafonné à 30 000 DH/an
        BigDecimal fraisProfessionnels = salaireAnnuelImposable.multiply(BigDecimal.valueOf(0.20))
                .min(BigDecimal.valueOf(30000));

        // 2. Base imposable annuelle
        BigDecimal baseImposable = salaireAnnuelImposable.subtract(fraisProfessionnels);

        // 3. Application barème progressif (exemple Maroc)
        BigDecimal impotAnnuel;
        if (baseImposable.compareTo(BigDecimal.valueOf(30000)) <= 0) {
            impotAnnuel = BigDecimal.ZERO;
        } else if (baseImposable.compareTo(BigDecimal.valueOf(50000)) <= 0) {
            impotAnnuel = baseImposable.subtract(BigDecimal.valueOf(30000))
                    .multiply(BigDecimal.valueOf(0.10));
        } else if (baseImposable.compareTo(BigDecimal.valueOf(60000)) <= 0) {
            impotAnnuel = BigDecimal.valueOf(2000)
                    .add(baseImposable.subtract(BigDecimal.valueOf(50000))
                            .multiply(BigDecimal.valueOf(0.20)));
        } else if (baseImposable.compareTo(BigDecimal.valueOf(80000)) <= 0) {
            impotAnnuel = BigDecimal.valueOf(4000)
                    .add(baseImposable.subtract(BigDecimal.valueOf(60000))
                            .multiply(BigDecimal.valueOf(0.30)));
        } else if (baseImposable.compareTo(BigDecimal.valueOf(180000)) <= 0) {
            impotAnnuel = BigDecimal.valueOf(10000)
                    .add(baseImposable.subtract(BigDecimal.valueOf(80000))
                            .multiply(BigDecimal.valueOf(0.34)));
        } else {
            impotAnnuel = BigDecimal.valueOf(54800)
                    .add(baseImposable.subtract(BigDecimal.valueOf(180000))
                            .multiply(BigDecimal.valueOf(0.38)));
        }

        // 4. Abattement charges de famille (360 DH / pers / an, max 6 pers)
        int nbPers = Math.min(nombrePersonnesCharge, 6);
        BigDecimal abattement = BigDecimal.valueOf(nbPers * 360L);
        impotAnnuel = impotAnnuel.subtract(abattement).max(BigDecimal.ZERO);

        // 5. Impôt mensuel
        BigDecimal impotMensuel = impotAnnuel.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        //ajouter element paie impot sur le revenu si n'existe pas
        if (!elementPaieRepository.existsByEmployeAndLibelleContaining(fichePaie.getEmploye(), "Impôt sur le revenu")) {
            log.info("Ajout de l'impôt sur le revenu à la fiche de paie.");
            ElementPaie irElement = new ElementPaie(null, TypeElement.IMPOT, "Impôt sur le revenu", "Impôt sur le revenu", ModeCalcul.TAUX, impotMensuel
                    , null, null, "Impôt sur le revenu calculé automatiquement", false, false, null, null);
            ajouterElementPaie(fichePaie.getEmploye().getId(), irElement);
        }
        return impotMensuel;
    }

    @Override
    @Transactional
    public void supprimerElementPaie(Long elementPaieId) {
        ElementPaie element = elementPaieRepository.findById(elementPaieId)
                .orElseThrow(() -> new EntityNotFoundException("Élément de paie non trouvé avec l'ID: " + elementPaieId));
        elementPaieRepository.delete(element);
        log.info("Élément de paie supprimé avec succès, ID: {}", elementPaieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FichePaie> getFichePaieByEmployeId(Long userId) {
        log.info("Récupération des fiches de paie pour l'utilisateur avec l'ID : {}", userId);

        Users users = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));
        log.info("Utilisateur trouvé : {} de type {}", users.getPreNom(), users.getUserType());
        List<FichePaie> fichesAccessibles = new ArrayList<>();

        switch (users.getUserType()) {
            case EMPLOYE:
                log.info("Récupération des fiches de paie pour l'employé avec l'ID : {}", userId);
                Employe employe = (Employe) employeRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé pour l'utilisateur avec l'ID : " + userId));
                fichesAccessibles = fichePaieRepository.findByEmploye(employe);
                log.info("Employé - {} fiches de paie récupérées pour l'utilisateur {}", fichesAccessibles.size(), userId);
                break;
            case MANAGER:
                List<Employe> equipe = employeRepository.findByManager(managerService.findById(userId));
                for (Employe membre : equipe) {
                    fichesAccessibles.addAll(fichePaieRepository.findByEmploye(membre));
                }
                log.info("Manager - {} fiches de paie récupérées (propres + équipe) pour l'utilisateur {}", fichesAccessibles.size(), userId);
                break;
            default:
                log.warn("Type d'utilisateur non géré : {} pour l'utilisateur {}", users.getUserType(), userId);
                break;
        }
        fichesAccessibles.sort((f1, f2) -> f2.getDateGeneration().compareTo(f1.getDateGeneration()));
        return fichesAccessibles;
    }
}

