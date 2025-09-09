package ma.digitalia.gestionconges.controllers;

import ma.digitalia.gestionconges.dto.CongesPrisDTO;
import ma.digitalia.gestionconges.services.CongesPrisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/conges-pris")
public class CongesPrisController {

    @Autowired
    private CongesPrisService congesPrisService;

    @GetMapping
    public ResponseEntity<List<CongesPrisDTO>> getAllCongesPris(@RequestParam Long userId) {
        try {
            List<CongesPrisDTO> congesPris = congesPrisService.getAllCongesPris(userId);
            return ResponseEntity.ok(congesPris);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/annee/{annee}")
    public ResponseEntity<List<CongesPrisDTO>> getCongesPrisByAnnee(@PathVariable int annee) {
        try {
            List<CongesPrisDTO> congesPris = congesPrisService.getCongesPrisByAnnee(annee);
            return ResponseEntity.ok(congesPris);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<CongesPrisDTO>> getCongesPrisByEmploye(@PathVariable Long employeId) {
        try {
            List<CongesPrisDTO> congesPris = congesPrisService.getCongesPrisByEmploye(employeId);
            return ResponseEntity.ok(congesPris);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/departement/{departement}")
    public ResponseEntity<List<CongesPrisDTO>> getCongesPrisByDepartement(@PathVariable String departement) {
        try {
            List<CongesPrisDTO> congesPris = congesPrisService.getCongesPrisByDepartement(departement);
            return ResponseEntity.ok(congesPris);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/periode")
    public ResponseEntity<List<CongesPrisDTO>> getCongesPrisByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        try {
            List<CongesPrisDTO> congesPris = congesPrisService.getCongesPrisByPeriode(dateDebut, dateFin);
            return ResponseEntity.ok(congesPris);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/type/{typeConge}")
    public ResponseEntity<List<CongesPrisDTO>> getCongesPrisByType(@PathVariable String typeConge) {
        try {
            List<CongesPrisDTO> congesPris = congesPrisService.getCongesPrisByType(typeConge);
            return ResponseEntity.ok(congesPris);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/total-jours/employe/{employeId}")
    public ResponseEntity<Integer> getTotalJoursCongesPris(
            @PathVariable Long employeId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int annee) {
        try {
            int totalJours = congesPrisService.getTotalJoursCongesPris(employeId, annee);
            return ResponseEntity.ok(totalJours);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/total-jours/departement/{departement}")
    public ResponseEntity<Integer> getTotalJoursCongesPrisByDepartement(
            @PathVariable String departement,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int annee) {
        try {
            int totalJours = congesPrisService.getTotalJoursCongesPrisByDepartement(departement, annee);
            return ResponseEntity.ok(totalJours);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
