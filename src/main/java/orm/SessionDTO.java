package orm;


import domain.*;

import java.time.LocalDateTime;

public class SessionDTO {

    private long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SessionState state;
    private final Task task;
    private final Subtask subtask;
    private User user;

    public SessionDTO(Session session) {
        this.startDate = this.getStartDate();
        this.endDate = this.getEndDate();
        this.state = this.getState();
        this.task = this.getTask();
        this.subtask = this.getSubtask();
        this.user = this.getUser();
    }
    public long getId() {
        return id;
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
    public Task getTask() {
        return task;
    }
    public Subtask getSubtask() {
        return subtask;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }






}
