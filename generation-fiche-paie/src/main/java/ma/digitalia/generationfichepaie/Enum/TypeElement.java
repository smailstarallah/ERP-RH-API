package ma.digitalia.generationfichepaie.Enum;

public enum TypeElement {
    SALAIRE_BASE,
    PRIME_FIXE,
    PRIME_VARIABLE,
    HEURES_SUPPLEMENTAIRES,
    INDEMNITE,              // Indemnités diverses (transport, repas...)
    DEDUCTION_ABSENCE,      // Retenues pour absences
    DEDUCTION_AUTRE,        // Autres déductions (pénalités...)
    COTISATION_SOCIALE,     // CNSS, AMO, CIMR...
    IMPOT,                  // IR, taxe pro...
    AUTRE
}
