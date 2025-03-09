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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Session> sessions = new ArrayList<>();

    public Calendar(){}

    public Calendar(User user){
        this.user = user;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public List<Session> getSessions() {
        return sessions;
    }
    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
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
            boolean collision = sessions.stream().anyMatch(existingSession ->
                    newSession.getStartDate().isBefore(existingSession.getEndDate()) &&
                            newSession.getEndDate().isAfter(existingSession.getStartDate())
            );
            if (collision) {
                throw new IllegalArgumentException("Session collides with an existing session: " + newSession);
            }
        }
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
                throw new IllegalArgumentException("The task isn't in the calendar. Missing session: " + taskSession+ ". ClASS CALENDAR removeSessions");
            }
        }
        sessions.removeAll(task.getSessions());
    }

    public void addSubtaskSessionsForGroups(Subtask subtask) {
        for (Session newSession : subtask.getSessions()) {
            boolean alreadyExists = sessions.stream().anyMatch(existing -> existing.equals(newSession));
            if (alreadyExists) {
                throw new IllegalArgumentException("Session " + newSession + " already exists in the calendar.");
            }
            boolean collision = sessions.stream().anyMatch(existing ->
                    newSession.getStartDate().isBefore(existing.getEndDate())
                            && newSession.getEndDate().isAfter(existing.getStartDate()));
            if (collision) {
                throw new IllegalArgumentException("Session " + newSession + " collides with an existing session in the calendar.");
            }
        }
        this.sessions.addAll(subtask.getSessions());
    }

    public void removeSubtaskSessionsForGroups(Subtask subtask) {
        sessions.removeIf(session -> subtask.getSessions().contains(session));
    }

}
