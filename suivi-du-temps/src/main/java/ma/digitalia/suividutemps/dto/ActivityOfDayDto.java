package ma.digitalia.suividutemps.dto;

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

