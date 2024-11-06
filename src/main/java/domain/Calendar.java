package domain;
import domain.User;
import jakarta.persistence.*;

import java.util.*;

@Entity
public class Calendar {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private long id;
    @OneToOne
    private User user;
    @OneToMany
    private ArrayList<Session> sessions = new ArrayList<>();

    public Calendar(){}

    public Calendar(User user){
        this.user = user;
    }
    public long getId() {
        return id;
    }
    public ArrayList<Session> getSessions() {
        return sessions;
    }
    public void addSessions(ArrayList<Session> newSessions) {
        sessions.addAll(newSessions);
    }
}
