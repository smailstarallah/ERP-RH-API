package ma.digitalia.appmain.controllers;

import ma.digitalia.appmain.services.DataSynthetiqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data-synthetique")
public class DataSynthetiqueController {

    @Autowired
    private DataSynthetiqueService dataSynthetiqueService;

    @PostMapping("/generer-semaine-derniere")
    public ResponseEntity<Map<String, Object>> genererDonneesSemaineDerniere() {
        Map<String, Object> response = new HashMap<>();

        try {
            dataSynthetiqueService.genererDonneesDeuxDerniersMois();
            response.put("success", true);
            response.put("message", "Données synthétiques générées avec succès pour la semaine dernière");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la génération des données : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
