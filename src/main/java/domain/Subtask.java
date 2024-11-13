package domain;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
public class Subtask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String name;
    private int level;
    private String description;
    @OneToMany
    private ArrayList<Session> sessions = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL)
    private ArrayList<Resource> resources = new ArrayList<>();



    public Subtask() {}
    public Subtask(String name, int level, String description) {
        this.name = name;
        this.level = level;
        this.description = description;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public ArrayList<Session> getSessions() {
        return sessions;
    }
    public ArrayList<Resource> getResources() {
        return resources;
    }


}
