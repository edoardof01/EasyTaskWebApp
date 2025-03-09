package orm;

import domain.SessionState;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

public class SessionWithTaskDTO {

    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private SessionState state;

    private Long taskId;
    private String taskName;
    private String taskType;

    private Long subtaskId;
    private String subtaskName;


    public SessionWithTaskDTO() {}


    public SessionWithTaskDTO(Long id,
                              LocalDateTime startDate,
                              LocalDateTime endDate,
                              SessionState state,
                              Long taskId,
                              String taskName,
                              String taskType,
                              Long subtaskId,
                              String subtaskName
                              ) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = state;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
        this.subtaskId = subtaskId;
        this.subtaskName = subtaskName;
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public SessionState getState() {
        return state;
    }
    public void setState(SessionState state) {
        this.state = state;
    }
    public Long getTaskId() {
        return taskId;
    }
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    public String getTaskName() {
        return taskName;
    }
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    public String getTaskType() {
        return taskType;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public Long getSubtaskId() {
        return subtaskId;
    }
    public void setSubtaskId(Long subtaskId) {
        this.subtaskId = subtaskId;
    }
    public String getSubtaskName() {
        return subtaskName;
    }
    public void setSubtaskName(String subtaskName) {
        this.subtaskName = subtaskName;
    }
}
