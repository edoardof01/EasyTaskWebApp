package orm;

import domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SharedDTO {
    private final long id;
    private String name;
    private String description;
    private int percentageOfCompletion;
    private LocalDateTime deadline;
    private boolean isInProgress = false;
    private int totalTime;
    private ArrayList<Subtask> subtasks = new ArrayList<>();
    private final Topic topic;
    private TaskState taskState;
    private int complexity;
    private int priority;
    private Set<Timetable> timetable = new HashSet<>();
    private Set<DefaultStrategy> strategies = new HashSet<>();
    private ArrayList<Resource> resources = new ArrayList<>();
    private String UserGuidance;

    public SharedDTO(Shared shared) {
        this.id = shared.getId();
        this.name = shared.getName();
        this.topic = shared.getTopic();
        this.taskState = shared.getState();
        this.description = shared.getDescription();
        this.percentageOfCompletion = shared.getPercentageOfCompletion();
        this.deadline = shared.getDeadline();
        this.priority = shared.getPriority();
        this.complexity = shared.getComplexity();
        this.isInProgress = shared.isInProgress();
        this.totalTime = shared.getTotalTime();
        this.strategies.addAll(shared.getStrategies());
        this.timetable.addAll(shared.getTimetable());
        this.resources = shared.getResources();
        this.subtasks = shared.getSubtasks();
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

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }
    public void setSubtasks(ArrayList<Subtask> subtasks) {
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
    public ArrayList<Resource> getResources() {
        return resources;
    }
    public void setResources(ArrayList<Resource> resources) {
        this.resources = resources;
    }
    public String getUserGuidance(){
        return UserGuidance;
    }
    public void setUserGuidance(String userGuidance){
        UserGuidance = userGuidance;
    }


}

