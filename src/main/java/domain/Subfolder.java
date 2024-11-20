package domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Subfolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SubfolderType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private Folder folder;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();

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
    public List<Task> getTasks() {
        return tasks;
    }
}