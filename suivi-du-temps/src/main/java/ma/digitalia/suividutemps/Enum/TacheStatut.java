package ma.digitalia.suividutemps.Enum;


public enum TacheStatut {
    A_FAIRE("À Faire"),
    EN_COURS("En Cours"),
    TERMINEE("Terminée"),
    BLOQUEE("Bloquée");

    private final String libelle;

    TacheStatut(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
