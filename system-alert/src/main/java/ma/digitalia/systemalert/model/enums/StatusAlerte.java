package ma.digitalia.systemalert.model.enums;

/**
 * Énumération pour les statuts d'alertes
 */
public enum StatusAlerte {
    UNREAD("Non lue"),
    READ("Lue");

    private final String description;

    StatusAlerte(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
