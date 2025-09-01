package ma.digitalia.generationfichepaie.controllers;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.generationfichepaie.services.GenerationFichePaieService;
import ma.digitalia.generationfichepaie.services.GenerationFichePaieServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/fiche-paie")
public class GenerationFichePaieController {

    private final GenerationFichePaieService generationFichePaieService;

    public GenerationFichePaieController(GenerationFichePaieService generationFichePaieService) {
        this.generationFichePaieService = generationFichePaieService;
    }

    @PostMapping("/ajouter-element-paie")
    @Transactional
    ResponseEntity<?> ajouterElementPaie(
            @RequestParam Long employeId,
            @RequestBody ElementPaie elementPaie
    ){
        try {
            log.info("ajouter element-paie {}", elementPaie);
            generationFichePaieService.ajouterElementPaie(employeId, elementPaie);
            log.info("Element de paie ajouté avec succès pour l'employé ID: {}", employeId);

            // Retourner un objet JSON au lieu d'une string
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Element de paie ajouté avec succès.");
            response.put("employeId", employeId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de l'ajout de l'élément de paie : {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de l'élément de paie : {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de l'ajout de l'élément de paie : " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/elements-paie/{employeId}")
    public ResponseEntity<?> getElementsPaie(@PathVariable Long employeId) {
        try {
            log.info("Récupération des éléments de paie pour l'employé ID: {}", employeId);

            List<ElementPaie> elements = generationFichePaieService.recupererElementPaie(employeId);

            log.info("Récupération réussie de {} éléments de paie pour l'employé ID: {}",
                    elements.size(), employeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Éléments de paie récupérés avec succès");
            response.put("data", elements);
            response.put("count", elements.size());

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            log.error("Employé non trouvé ID: {} - {}", employeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employé non trouvé");

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des éléments de paie pour l'employé ID: {}", employeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la récupération des éléments de paie");
        }
    }

    // Ajoutez ceci dans `GenerationFichePaieController.java`
    @PutMapping("/modifier-element-paie/{elementPaieId}")
    @Transactional
    public ResponseEntity<?> modifierElementPaie(
            @PathVariable Long elementPaieId,
            @RequestParam Long employeId,
            @RequestBody ElementPaie elementPaie
    ) {
        try {
            log.info("Modification de l'élément de paie ID: {} pour l'employé ID: {}", elementPaieId, employeId);
            generationFichePaieService.mettreAJourElementPaie(employeId, elementPaieId, elementPaie);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Élément de paie modifié avec succès.");
            response.put("elementPaieId", elementPaieId);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.error("Élément ou employé non trouvé : {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Élément ou employé non trouvé.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'élément de paie : {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la modification de l'élément de paie.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}/pdf")
    @Transactional
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        log.info("Récupération du PDF de la fiche de paie pour l'employé ID: {}", id);
        byte[] pdf = generationFichePaieService.recupererFichePaiePdf(id);
        log.info("PDF récupéré avec succès pour l'employé ID: {}", id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fiche_paie_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
