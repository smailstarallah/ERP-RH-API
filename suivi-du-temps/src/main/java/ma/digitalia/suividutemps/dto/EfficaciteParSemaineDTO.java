package ma.digitalia.suividutemps.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class EfficaciteParSemaineDTO {
    private LocalDate date;
    private int totalMinutes;
    private double totalHours;
    private String name;
    @Override
    public String toString() {
        return "getEfficaciteParSemaineDTO{" +
                "date=" + date +
                ", name='" + name + '\'' +
                ", totalMinutes=" + totalMinutes +
                ", totalHours=" + totalHours +
                '}';
    }
}
