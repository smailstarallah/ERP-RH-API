package ma.digitalia.suividutemps.dto;

import java.time.LocalDate;

public record CreateProjectRequest(
    String nom,
    String client,
    String description,
    LocalDate dateDebut,
    LocalDate dateFinPrevue,
    Double budget
) {}
