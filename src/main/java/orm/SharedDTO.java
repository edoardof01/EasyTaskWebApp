package orm;

import domain.*;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SharedDTO {
    private final long id;
    @ManyToOne
    private final UserDTO user;
    private String name;
    private String description;
    private int percentageOfCompletion;
    private LocalDateTime deadline;
    private boolean isInProgress;
    private int totalTime;
    private List<SubtaskDTO> subtasks;
    private List<SessionDTO> sessions;
    private final Topic topic;
    private TaskState taskState;
    private int complexity;
    private int priority;
    private Set<Timetable> timetable = new HashSet<>();
    private Set<DefaultStrategy> strategies = new HashSet<>();
    private List<ResourceDTO> resources;
    private String UserGuidance;

    public SharedDTO(Shared shared) {
        this.id = shared.getId();
        this.user = new UserDTO(shared.getUser());
        this.name = shared.getName();
        this.sessions = shared.getSessions().stream().map(SessionDTO::new).collect(Collectors.toList());
        this.topic = shared.getTopic();
        this.taskState = shared.getState();
        this.description = shared.getDescription();
        this.percentageOfCompletion = shared.getPercentageOfCompletion();
        this.strategies = shared.getStrategies();
        this.deadline = shared.getDeadline();
        this.priority = shared.getPriority();
        this.complexity = shared.getComplexity();
        this.isInProgress = shared.isInProgress();
        this.totalTime = shared.getTotalTime();
        this.strategies.addAll(shared.getStrategies());
        this.timetable.addAll(shared.getTimetable());
        this.resources = shared.getResources().stream().map(ResourceDTO::new).collect(Collectors.toList());
        this.subtasks = shared.getSubtasks().stream().map(SubtaskDTO::new).collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public UserDTO getUser() {
        return user;
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

    public List<SessionDTO> getSessions(){
        return sessions;
    }
    public void setSessions(List<SessionDTO> sessions) {
        this.sessions = sessions;
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

    public String getUserGuidance() {
        return UserGuidance;
    }

    public void setUserGuidance(String userGuidance) {
        UserGuidance = userGuidance;
    }
}
