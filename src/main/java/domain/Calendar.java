package domain;
import jakarta.persistence.*;

import java.util.*;

@Entity
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;

    @OneToOne
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Session> sessions = new ArrayList<>();

    public Calendar(){}

    public Calendar(User user){
        this.user = user;
    }
    public long getId() {
        return id;
    }
    public List<Session> getSessions() {
        return sessions;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public void addSessions(List<Session> newSessions) {
        sessions.addAll(newSessions);
    }
    public void removeSessions(Task task) {
        if(!new HashSet<>(sessions).containsAll(task.getSessions())){
            throw new IllegalArgumentException("the task isn't in the calendar");
        }
        sessions.removeAll(task.getSessions());
    }
    public void addSubtaskSessionsForGroups(Subtask subtask){
        sessions.addAll(subtask.getSessions());
    }
    public void removeSubtaskSessionsForGroups(Subtask subtask){
        sessions.removeAll(subtask.getSessions());
    }

}
