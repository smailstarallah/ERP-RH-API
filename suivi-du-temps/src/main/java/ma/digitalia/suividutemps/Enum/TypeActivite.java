package ma.digitalia.suividutemps.Enum;

public enum TypeActivite {
    TRAVAIL("Travail"),
    PAUSE("Pause");

    private final String libelle;

    TypeActivite(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}