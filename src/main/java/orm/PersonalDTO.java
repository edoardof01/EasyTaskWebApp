package orm;

import domain.*;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PersonalDTO {
    private final long id;
    private String name;
    private long user_id;
    private String description;
    private int percentageOfCompletion;
    private LocalDateTime deadline;
    private boolean isInProgress;
    private int totalTime;
    private List<SubtaskDTO> subtasks;
    private Topic topic;
    private TaskState taskState;
    private int complexity;
    private int priority;
    private Set<Timetable> timetable = new HashSet<>();
    private Set<DefaultStrategy> strategies = new HashSet<>();
    private List<ResourceDTO> resources;

    public PersonalDTO() {
        this.id = -1;
    }
    public PersonalDTO(Personal personal) {
        this.id = personal.getId();
        this.user_id = personal.getUser().getId();
        this.name = personal.getName();
        this.description = personal.getDescription();
        this.percentageOfCompletion = personal.getPercentageOfCompletion();
        this.deadline = personal.getDeadline();
        this.isInProgress = personal.isInProgress();
        this.totalTime = personal.getTotalTime();
        this.resources = personal.getResources().stream().map(ResourceDTO::new).collect(Collectors.toList());
        this.subtasks = personal.getSubtasks().stream().map(SubtaskDTO::new).collect(Collectors.toList());
        this.topic = personal.getTopic();
        this.taskState = personal.getState();
        this.priority = personal.getPriority();
        this.complexity = personal.getComplexity();
        this.resources = personal.getResources().stream().map(ResourceDTO::new).collect(Collectors.toList());
        this.timetable.addAll(personal.getTimetable());
    }
    public long getId() {
        return id;
    }

    public long getUser_id() {
        return user_id;
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

    public int getPercentageOfCompletion() {
        return percentageOfCompletion;
    }

    public void setPercentageOfCompletion(int percentageOfCompletion) {
        this.percentageOfCompletion = percentageOfCompletion;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public boolean isInProgress() {
        return isInProgress;
    }

    public void setInProgress(boolean inProgress) {
        isInProgress = inProgress;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public List<SubtaskDTO> getSubtasks() {
        return subtasks;
    }
    public void setSubtasks(List<SubtaskDTO> subtasks) {
        this.subtasks = subtasks;
    }
    public Topic getTopic() {
        return topic;
    }
    public TaskState getTaskState() {
        return taskState;
    }
    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }
    public int getComplexity() {
        return complexity;
    }
    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public Set<Timetable> getTimetable() {
        return timetable;
    }
    public void setTimetable(Set<Timetable> timetable) {
        this.timetable = timetable;
    }
    public Set<DefaultStrategy> getStrategies() {
        return strategies;
    }
    public void setStrategies(Set<DefaultStrategy> strategies) {
        this.strategies = strategies;
    }
    public List<ResourceDTO> getResources() {
        return resources;
    }
    public void setResources(List<ResourceDTO> resources) {
        this.resources = resources;
    }

}
