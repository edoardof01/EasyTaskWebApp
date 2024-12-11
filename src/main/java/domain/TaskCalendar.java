package domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TaskCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Group group;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSession> userSessions = new ArrayList<>();

    public TaskCalendar() {}

    public TaskCalendar(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public List<UserSession> getUserSessions() {
        return userSessions;
    }

    public Long getId() {
        return id;
    }


    public void setGroup(Group group) {
        this.group = group;
    }

    public void addSessions(User user, Subtask subtask) {
        // Verifica che l'utente sia membro del gruppo e che il task sia attivo
        if (!group.getIsComplete()) {
            throw new UnsupportedOperationException(" il task non è completo");
        }
        boolean found = false;
        for(User member : group.getMembers()) {
            if(member.equals(user)) {
                found = true;
            }
        }
        if(!found) {
            throw new IllegalArgumentException("the user is not a member of the group");
        }

        // Verifica che il subtask appartenga effettivamente all'utente
        boolean subtaskFound = false;
        for (TakenSubtask ts : group.getTakenSubtasks()) {
            if (ts.getUser().equals(user) && ts.getSubtask().equals(subtask)) {
                subtaskFound = true;
                break; // Esce dal ciclo non appena trovato
            }
        }
        if (!subtaskFound) {
            throw new IllegalArgumentException("Il subtask fornito non è associato all'utente specificato");
        }

        // Aggiungi ciascuna sessione del subtask al calendario
        for (Session session : subtask.getSessions()) {
            boolean sessionExists = userSessions.stream()
                    .anyMatch(userSession -> userSession.getUser().equals(user) && userSession.getSession().equals(session));

            if (!sessionExists) {
                userSessions.add(new UserSession(user, session));
            }
        }
    }


    public void removeSessions(User user) {
        userSessions.removeIf(userSession -> userSession.getUser().equals(user));
    }

    public List<Session> getUserSessions(User user) {
        List<Session> sessions = new ArrayList<>();
        for (UserSession userSession : userSessions) {
            if (userSession.getUser().equals(user)) {
                sessions.add(userSession.getSession());
            }
        }
        return sessions;
    }

    public void moveSessions(User sender, User receiver) {
        List<Session> senderSessions = getUserSessions(sender);
        List<Session> receiverSessions = getUserSessions(receiver);

        removeSessions(sender);
        removeSessions(receiver);

        for (Session session : senderSessions) {
            userSessions.add(new UserSession(receiver, session));
        }
        for (Session session : receiverSessions) {
            userSessions.add(new UserSession(sender, session));
        }
    }
}
