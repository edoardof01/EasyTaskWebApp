package orm;

import domain.Subtask;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubtaskDTO {
    @NotNull
    private final long id;
    private String name;
    private int totalTime;
    private String description;
    private Integer level;
    private List<ResourceDTO> resources = new ArrayList<>();
    private List<SessionDTO> sessions = new ArrayList<>();

    public SubtaskDTO(){
        this.id = -1;
    }
    public SubtaskDTO(Subtask subtask) {
        this.id = subtask.getId();
        this.name = subtask.getName();
        this.totalTime = subtask.getTotalTime();
        this.description = subtask.getDescription();
        this.level = subtask.getLevel();
        this.resources = subtask.getResources().stream().map(ResourceDTO::new).collect(Collectors.toList());
        this.sessions = subtask.getSessions().stream().map(SessionDTO::new).collect(Collectors.toList());
    }
    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public List<ResourceDTO> getResources() {
        return resources;
    }
    public void setResources(List<ResourceDTO> resources) {
        this.resources = resources;
    }
    public int getTotalTime() {
        return totalTime;
    }
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
    public List<SessionDTO> getSessions() {
        return sessions;
    }
}
