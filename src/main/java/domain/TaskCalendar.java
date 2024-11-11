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

    public void addSessions() {
        if (group.getIsComplete() || !group.isInProgress()) {
            throw new UnsupportedOperationException("Non puoi aggiungere le sessioni se il task non è al completo");
        }
        for (User member : group.getMembers()) {
            Subtask subtask = group.getTakenSubtasks().get(member);

            if (subtask != null) {
                for (Session session : subtask.getSessions()) {
                    userSessions.add(new UserSession(member, session));
                }
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
