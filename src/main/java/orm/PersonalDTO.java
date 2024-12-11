package orm;

import domain.*;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PersonalDTO {
    private  long id;
    private String name;
    private UserDTO user;
    private long userId;
    private String description;
    private int percentageOfCompletion;
    private LocalDateTime deadline;
    private boolean isInProgress;
    private int totalTime;
    private List<SubtaskDTO> subtasks;
    private List<SessionDTO> sessions;
    private Topic topic;
    private TaskState taskState;
    private int complexity;
    private int priority;
    private Timetable timetable;
    private List<StrategyInstance> strategies = new ArrayList<>();
    private List<ResourceDTO> resources;

    public PersonalDTO() {
    }
    public PersonalDTO(long id, String name, long userId , String description, int percentageOfCompletion,LocalDateTime deadline,
                       boolean isInProgress,int totalTime,List<SubtaskDTO> subtasks,List<SessionDTO> sessions,Topic topic,TaskState taskState,
                       Timetable timetable,List<StrategyInstance> strategies,List<ResourceDTO> resources) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.description = description;
        this.percentageOfCompletion = percentageOfCompletion;
        this.deadline = deadline;
        this.isInProgress = isInProgress;
        this.totalTime = totalTime;
        this.subtasks = subtasks;
        this.sessions = sessions;
        this.topic = topic;
        this.taskState = taskState;
        this.timetable = timetable;
        this.strategies = strategies;
        this.resources = resources;
    }


    public PersonalDTO(Personal personal) {
        this.id = personal.getId();
        this.user = new UserDTO(personal.getUser());
        this.name = personal.getName();
        this.description = personal.getDescription();
        this.percentageOfCompletion = personal.getPercentageOfCompletion();
        this.deadline = personal.getDeadline();
        this.isInProgress = personal.getIsInProgress();
        this.totalTime = personal.getTotalTime();
        this.resources = personal.getResources().stream().map(ResourceDTO::new).collect(Collectors.toList());
        this.subtasks = personal.getSubtasks().stream().map(SubtaskDTO::new).collect(Collectors.toList());
        this.topic = personal.getTopic();
        this.strategies = personal.getStrategies();
        this.taskState = personal.getState();
        this.priority = personal.getPriority();
        this.complexity = personal.getComplexity();
        this.resources = personal.getResources().stream().map(ResourceDTO::new).collect(Collectors.toList());
        this.timetable = personal.getTimetable();
        this.sessions = personal.getSessions().stream().map(SessionDTO::new).collect(Collectors.toList());
    }
    public long getId() {
        return id;
    }

    public UserDTO getUser() {
        return user;
    }

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
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
    public List<SessionDTO> getSessions() {
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
    public Timetable getTimetable() {
        return timetable;
    }
    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }
    public List<StrategyInstance> getStrategies() {
        return strategies;
    }
    public void setStrategies(List<StrategyInstance> strategies) {
        this.strategies = strategies;
    }
    public List<ResourceDTO> getResources() {
        return resources;
    }
    public void setResources(List<ResourceDTO> resources) {
        this.resources = resources;
    }

}
