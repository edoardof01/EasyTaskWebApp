package domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Subtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    private String name;
    private Integer level;
    private String description;
    private int totalTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();



    public Subtask() {}
    public Subtask(String name,int totalTime, int level, String description,List<Resource> resources, List<Session> sessions) {
        this.name = name;
        this.level = level;
        this.description = description;
        this.resources = resources;
        this.totalTime = totalTime;
        this.sessions = sessions;
    }

/*
    public Subtask(Subtask original) {
        if (original == null) {
            throw new IllegalArgumentException("Original subtask cannot be null");
        }
        this.id = original.id; // Assumendo che l'ID non cambi
        this.name = original.name;
        this.description = original.description;
        this.level = original.level;
        this.totalTime = original.totalTime;
        this.resources = original.resources;
        this.sessions = original.sessions != null ? new ArrayList<>(original.sessions) : new ArrayList<>();
    }*/

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
    public Integer getLevel() {
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
    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subtask subtask = (Subtask) o;
        if (!Objects.equals(level, subtask.level)) return false;
        if (totalTime != subtask.totalTime) return false;
        if (!name.equals(subtask.name)) return false;
        return description.equals(subtask.description);
        /// FORSE DEVO AGGIUNGERE CONTROLLO SU SESSIONI E RISORSE

    }
    @Override
    public int hashCode() {
        return Objects.hash(id, name, level, description, totalTime, sessions, resources);
    }


}
