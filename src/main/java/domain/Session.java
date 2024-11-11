package domain;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;


@Entity
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @OneToOne
    private User user;
    private SessionState state;
    @ManyToOne
    private Task task;
    @ManyToOne
    private Subtask subtask;
    private long sessionDuration;

    public Session() {}

    public Session(long id, LocalDateTime startDate, LocalDateTime endDate, User user,Task task,@Nullable Subtask subtask) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
        this.task = task;
        this.subtask = subtask;
        this.sessionDuration =  Duration.between(startDate,endDate).toHours();
    }
    public Session(long id, LocalDateTime startDate, LocalDateTime endDate, User user, Task task) {
        this(id, startDate, endDate, user, task, null);
    }


    public Long getId() {
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
    public void findEndDate(long hours){
        endDate = startDate.plusHours(hours);
    }
    public long getSessionDuration() {
        return sessionDuration;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Task getTask() {
        return task;
    }
    public Subtask getSubtask(){
        if(subtask!=null) {
            return subtask;
        }
        return null;
    }
    public SessionState getState() {
        return state;
    }
    public void setState(SessionState state) {
        this.state = state;
    }


}
