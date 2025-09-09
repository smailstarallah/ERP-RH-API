package ma.digitalia.systemalert.model.enums;

/**
 * Énumération pour les types d'alertes
 */
public enum TypeAlerte {
    INFO("Information"),
    WARNING("Avertissement"),
    ERROR("Erreur");

    private final String description;

    TypeAlerte(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
