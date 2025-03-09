package domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public void setUserSessions(List<UserSession> userSessions) {
        this.userSessions = userSessions;
    }
    public List<Session> getSessions(){
        List<Session> sessions = new ArrayList<>();
        for(UserSession userSession : userSessions){
            sessions.add(userSession.getSession());
        }
        return sessions;
    }
    public List<Session> getSessionsPerUser(User user){
        List<Session> sessions = new ArrayList<>();
        for(UserSession userSession : userSessions){
            if(userSession.getUser().equals(user)){
                sessions.add(userSession.getSession());
            }
        }
        return sessions;
    }
    public List<User> getUsers(){
        List<User> users = new ArrayList<>();
        for(UserSession userSession : userSessions){
            users.add(userSession.getUser());
        }
        return users;
    }
    public Long getId() {
        return id;
    }


    public void setGroup(Group group) {
        this.group = group;
    }

    public void addSessions(User user, Subtask subtask) {

        if (!group.getIsComplete()) {
            throw new UnsupportedOperationException(" il task non è completo");
        }
        boolean found = false;
        for(User member : group.getMembers()) {
            if (member.equals(user)) {
                found = true;
                break;
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

    public List<Session> getUserSession(User user) {
        return userSessions.stream()
                .filter(userSession -> userSession.getUser().equals(user))
                .map(UserSession::getSession)
                .collect(Collectors.toList());
    }

    public void removeTaskSessions(Group group){
        this.getSessions().removeIf(session -> group.getSessions().contains(session));
    }
}
