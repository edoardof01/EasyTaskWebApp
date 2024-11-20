package domain;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;


@Entity
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private SessionState state;

    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    private Subtask subtask;

    private long sessionDuration;

    public Session() {}

    public Session( LocalDateTime startDate, LocalDateTime endDate, User user,Task task,@Nullable Subtask subtask) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
        this.task = task;
        this.subtask = subtask;
        this.sessionDuration =  Duration.between(startDate,endDate).toHours();
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
