package ma.digitalia.generationfichepaie.Enum;


public enum ModeCalcul {
    MONTANT,             // Montant fixe
    TAUX,      // % d'une base
    BAREME,           // Selon un barème (ex: IR, CNSS)
    PAR_JOUR,         // Ex: indemnité par jour
    PAR_HEURE,        // Ex: heures supplémentaires
    FORMULE,           // Calcul personnalisé (script ou règle métier)
    AUTOMATIQUE_TIME_TRACKING
}