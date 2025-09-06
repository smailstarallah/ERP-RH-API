package ma.digitalia.suividutemps.dto;

public class WeekRowDto {
    private String day;
    private String arrivee;
    private String sortie;
    private int effective;
    private int pauses;
    private String retard;

    public WeekRowDto(String day, String arrivee, String sortie, int effective, int pauses, String retard) {
        this.day = day;
        this.arrivee = arrivee;
        this.sortie = sortie;
        this.effective = effective;
        this.pauses = pauses;
        this.retard = retard;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getArrivee() {
        return arrivee;
    }

    public void setArrivee(String arrivee) {
        this.arrivee = arrivee;
    }

    public String getSortie() {
        return sortie;
    }

    public void setSortie(String sortie) {
        this.sortie = sortie;
    }

    public int getEffective() {
        return effective;
    }

    public void setEffective(int effective) {
        this.effective = effective;
    }

    public int getPauses() {
        return pauses;
    }

    public void setPauses(int pauses) {
        this.pauses = pauses;
    }

    public String getRetard() {
        return retard;
    }

    public void setRetard(String retard) {
        this.retard = retard;
    }
}

