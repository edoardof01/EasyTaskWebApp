package domain;

import jakarta.persistence.*;

@Entity
public class TakenSubtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false) // Relazione con Group
    private Group group;

    @ManyToOne
    @JoinColumn(nullable = false) // Relazione con User
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false) // Relazione con Subtask
    private Subtask subtask;

    // Costruttori, getter e setter
    public TakenSubtask() {}
    public TakenSubtask(Group group, User user, Subtask subtask) {
        this.group = group;
        this.user = user;
        this.subtask = subtask;
    }

    public Subtask getSubtask() {
        return subtask;
    }
    public User getUser(){
        return user;
    }
    public Group getGroup(){
        return group;
    }
    public void setSubtask(Subtask subtask){
        this.subtask = subtask;
    }
}

