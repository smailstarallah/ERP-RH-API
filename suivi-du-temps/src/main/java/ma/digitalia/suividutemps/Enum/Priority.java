package ma.digitalia.suividutemps.Enum;

import lombok.Getter;

@Getter
public enum Priority {
    LOW("Basse"),
    MEDIUM("Moyenne"),
    HIGH("Haute"),
    CRITICAL("Critique");

    private final String libelle;

    Priority(String libelle) {
        this.libelle = libelle;
    }

}
