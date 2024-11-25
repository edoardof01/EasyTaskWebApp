package domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Subtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;
    private int level;
    @Column(length = 1000)
    private String description;
    private int totalTime;

    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Resource> resources = new ArrayList<>();


    public Subtask() {}
    public Subtask(String name,int totalTime, int level, String description,List<Resource> resources) {
        this.name = name;
        this.level = level;
        this.description = description;
        this.resources = resources;
        this.totalTime = totalTime;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getTotalTime(){
        return totalTime;
    }
    public void setTotalTime(int totalTime){
        this.totalTime = totalTime;
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
    public List<Session> getSessions() {
        return sessions;
    }
    public List<Resource> getResources() {
        return resources;
    }


}
