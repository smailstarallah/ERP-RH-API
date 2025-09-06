package ma.digitalia.suividutemps.dto;

public record TeamEfficiencyDto(
        String equipe,
        int efficacite,
        int satisfaction,
        int burnout
) {}
