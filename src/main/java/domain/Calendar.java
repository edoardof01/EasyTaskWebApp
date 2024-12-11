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

    @OneToMany
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
        for (Session newSession : newSessions) {
            if (sessions.stream().anyMatch(existingSession -> existingSession.equals(newSession))) {
                throw new IllegalArgumentException("Session already exists in the calendar: " + newSession);
            }
        }
        // Se non ci sono duplicati, aggiungi le nuove sessioni
        sessions.addAll(newSessions);
    }


    public void removeSessions(Task task) {
        for (Session taskSession : task.getSessions()) {
            boolean found = false;
            for (Session calendarSession : sessions) {
                if (taskSession.equals(calendarSession)) {
                    found = true;
                    break;
                }
            }
           /* if (!found) {
                throw new IllegalArgumentException("The task isn't in the calendar. Missing session: " + taskSession+ ". ClASS CALENDAR removeSessions");
            }*/
        }
        sessions.removeAll(task.getSessions());
    }

    public void addSubtaskSessionsForGroups(Subtask subtask){
        this.sessions.addAll(subtask.getSessions());
    }
    public void removeSubtaskSessionsForGroups(Subtask subtask) {
        subtask.getSessions().forEach(session -> {

            this.sessions.remove(session);
        });
    }




}
