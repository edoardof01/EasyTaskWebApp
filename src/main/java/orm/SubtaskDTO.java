package orm;

import domain.Subtask;

public class SubtaskDTO {
    private final long id;
    private String name;
    private String description;
    private int level;

    public SubtaskDTO(long id, String name, String description, int level) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.level = level;
    }
    public SubtaskDTO(Subtask subtask) {
        this.id = subtask.getId();
        this.name = subtask.getName();
        this.description = subtask.getDescription();
        this.level = subtask.getLevel();
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
}
