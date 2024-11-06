package domain;

import jakarta.persistence.*;

@Entity
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    @ManyToOne
    private User sender;
    @ManyToOne
    private User receiver;
    @ManyToOne
    private Subtask givenSubtask;
    @ManyToOne
    private Subtask SubtaskToReceive;
    @ManyToOne
    private Group group;

    public Request() {}
    public Request(User sender, User receiver, Group group, Subtask givenSubtask, Subtask SubtaskToReceive) {
      this.sender = sender;
      this.receiver = receiver;
      this.group = group;
      this.givenSubtask = givenSubtask;
      this.SubtaskToReceive = SubtaskToReceive;
    }
    public User getSender() {
      return sender;
    }
    public Long getId() {
      return id;
    }
    public Group getGroup() {
      return group;
    }
    public Subtask getGivenSubtask() {
      return givenSubtask;
    }
    public Subtask getSubtaskToReceive() {
      return SubtaskToReceive;
    }
    public User getReceiver() {
        return receiver;
    }
}


