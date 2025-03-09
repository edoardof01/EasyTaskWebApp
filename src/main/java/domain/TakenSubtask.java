package domain;

import jakarta.persistence.*;

@Entity
public class TakenSubtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @OneToOne
    private Subtask subtask;

    // Costruttori, getter e setter
    public TakenSubtask() {}
    public TakenSubtask( User user, Subtask subtask) {
        this.user = user;
        this.subtask = subtask;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Subtask getSubtask() {
        return subtask;
    }
    public User getUser(){
        return user;
    }
    public void setUser(User user){
        this.user = user;
    }
    public void setSubtask(Subtask subtask){
        this.subtask = subtask;
    }
}

