package ma.digitalia.suividutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOfDayDto {
    public String type;
    public String startTime;
    public String endTime;
    public String description;
    public TaskDto task;
    public String projectName;

    public static class TaskDto {
        public String id;
        public String name;
        public String priority;
    }
}

