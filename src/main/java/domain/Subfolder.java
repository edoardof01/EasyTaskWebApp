package domain;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
public class Subfolder {
    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private SubfolderType type;
    @OneToMany
    private ArrayList<Task> Tasks = new ArrayList<>();

    public Subfolder() {}

    public Subfolder(SubfolderType type) {
      this.type = type;
    }

    public Long getId() {
      return id;
    }
    public SubfolderType getType() {
        return type;
    }
    public ArrayList<Task> getTasks() {
        return Tasks;
    }
}