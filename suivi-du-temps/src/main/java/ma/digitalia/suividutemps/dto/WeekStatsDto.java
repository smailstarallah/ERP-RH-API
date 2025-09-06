package ma.digitalia.suividutemps.dto;

public class WeekStatsDto {
    private double totalHours;
    private String averageArrival;
    private double overtimeHours;
    private int absences;
    private int lateArrivals;

    public WeekStatsDto(double totalHours, String averageArrival, double overtimeHours, int absences, int lateArrivals) {
        this.totalHours = totalHours;
        this.averageArrival = averageArrival;
        this.overtimeHours = overtimeHours;
        this.absences = absences;
        this.lateArrivals = lateArrivals;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public String getAverageArrival() {
        return averageArrival;
    }

    public void setAverageArrival(String averageArrival) {
        this.averageArrival = averageArrival;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public int getAbsences() {
        return absences;
    }

    public void setAbsences(int absences) {
        this.absences = absences;
    }

    public int getLateArrivals() {
        return lateArrivals;
    }

    public void setLateArrivals(int lateArrivals) {
        this.lateArrivals = lateArrivals;
    }
}

