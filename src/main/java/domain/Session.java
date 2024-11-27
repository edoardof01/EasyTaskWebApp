package domain;
import com.fasterxml.jackson.annotation.JsonBackReference;
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


    @Enumerated(EnumType.STRING)
    private SessionState state;

    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    private Subtask subtask;

    private long sessionDuration;

    public Session() {}

    public Session(LocalDateTime startDate, LocalDateTime endDate,Task task,@Nullable Subtask subtask) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.task = task;
        this.subtask = subtask;
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
