package ma.digitalia.suividutemps.dto;

public record WellnessDataDto(
        String indicateur,
        int valeur,
        int cible,
        String statut
) {}
