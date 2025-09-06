package ma.digitalia.suividutemps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatusDto {

    @NotBlank
    @JsonProperty("employeeId")
    private String employeeId;

    @NotBlank
    @JsonProperty("employeeName")
    private String employeeName;

    @NotBlank
    @JsonProperty("role")
    private String role;

    @NotNull
    @JsonProperty("isConnected")
    private Boolean isConnected;

    @NotNull
    @PositiveOrZero
    @JsonProperty("todayHours")
    private Integer todayHours;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("lastActivity")
    private String lastActivity;

    @JsonProperty("performance")
    private String performance;

    @JsonProperty("weeklyHours")
    private Double weeklyHours;

    @JsonProperty("targetHours")
    private Double targetHours;

    @JsonProperty("lateArrivalCount")
    private Integer lateArrivalCount;

    @JsonProperty("overTimeHours")
    private Double overTimeHours;

    @JsonProperty("department")
    private String department;

    @JsonProperty("manager")
    private String manager;

    @JsonProperty("urgentTasks")
    private Integer urgentTasks;

    @JsonProperty("availabilityStatus")
    private String availabilityStatus;

    public String toString() {
        return "TeamStatusDto{" +
                "employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", role='" + role + '\'' +
                ", isConnected=" + isConnected +
                ", todayHours=" + todayHours +
                ", avatar='" + avatar + '\'' +
                ", lastActivity='" + lastActivity + '\'' +
                ", performance='" + performance + '\'' +
                ", weeklyHours=" + weeklyHours +
                ", targetHours=" + targetHours +
                ", lateArrivalCount=" + lateArrivalCount +
                ", overTimeHours=" + overTimeHours +
                ", department='" + department + '\'' +
                ", manager='" + manager + '\'' +
                ", urgentTasks=" + urgentTasks +
                ", availabilityStatus='" + availabilityStatus + '\'' +
                '}';
    }
}