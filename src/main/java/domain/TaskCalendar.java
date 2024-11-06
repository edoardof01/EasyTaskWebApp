package domain;

import jakarta.persistence.*;

@Entity
public class TaskCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    @OneToOne
    private Group group;

    public TaskCalendar() {}
    public TaskCalendar(Group group) {
      this.group = group;
    }

  public Group getGroup() {
    return group;
  }

  public Long getId() {
      return id;
    }

}