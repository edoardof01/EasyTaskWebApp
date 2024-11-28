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
        for (Session newSession : newSessions) {
            // Verifica se la nuova sessione si sovrappone a quelle già nel calendario
            for (Session existingSession : sessions) {
                if (newSession.overlaps(existingSession)) {
                    throw new IllegalArgumentException("Session " + newSession + " overlaps with an existing session. CLASS CALENDAR");
                }
            }
        }
        // Se non ci sono sovrapposizioni, aggiungi le nuove sessioni
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
            if (!found) {
                throw new IllegalArgumentException("The task isn't in the calendar. Missing session: " + taskSession+ ". ClASS CALENDAR");
            }
        }
        sessions.removeAll(task.getSessions());
    }


    public List<Session> getSessionsSortedByStartDate(List<Session> sessions) {
        return sessions.stream()
                .sorted(Comparator.comparing(Session::getStartDate))
                .toList();
    }
    public void addSubtaskSessionsForGroups(Subtask subtask){
        sessions.addAll(subtask.getSessions());
    }
    public void removeSubtaskSessionsForGroups(Subtask subtask){
        subtask.getSessions().forEach(sessions::remove);
    }


}
