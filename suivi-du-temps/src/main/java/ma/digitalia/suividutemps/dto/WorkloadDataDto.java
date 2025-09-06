package ma.digitalia.suividutemps.dto;

public record WorkloadDataDto(
        String employee,
        int heuresStandard,
        int heuresSupp,
        String projet,
        int productivite,
        String departement
) {}
