package orm;

import domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupDTO {
    private  long id;
    private long userId;
    private String name;
    private String description;
    private int percentageOfCompletion;
    private LocalDateTime deadline;
    private  LocalDateTime dateOnFeed;
    private boolean isInProgress;
    private int totalTime;
    private List<SubtaskDTO> subtasks;
    private List<SessionDTO> sessions;
    private  Topic topic;
    private TaskState taskState;
    private int complexity;
    private int priority;
    private Timetable timetable;
    private List<StrategyInstance> strategies;
    private List<ResourceDTO> resources;
    private int numUser;
    private int actualMembers;
    private  UserDTO user;


    public GroupDTO(){}

    public GroupDTO(long id, long userId, String name,  String description, int percentageOfCompletion, LocalDateTime deadline,
                    LocalDateTime dateOnFeed, boolean isInProgress, int totalTime, List<SubtaskDTO> subtasks, List<SessionDTO> sessions, Topic topic,
                    TaskState taskState, int complexity, int priority, Timetable timetable, List<StrategyInstance> strategies,
                    List<ResourceDTO> resources,  int numUser,  int actualMembers){
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.percentageOfCompletion = percentageOfCompletion;
        this.deadline = deadline;
        this.dateOnFeed = dateOnFeed;
        this.isInProgress = isInProgress;
        this.totalTime = totalTime;
        this.subtasks = subtasks;
        this.sessions = sessions;
        this.topic = topic;
        this.taskState = taskState;
        this.complexity = complexity;
        this.priority = priority;
        this.timetable = timetable;
        this.strategies = strategies;
        this.resources = resources;
        this.numUser = numUser;
        this.actualMembers = actualMembers;
    }
    public GroupDTO(Group group) {
        this.id = group.getId();
        this.user = new UserDTO(group.getUser());
        this.name = group.getName();
        this.topic = group.getTopic();
        this.taskState = group.getState();
        this.description = group.getDescription();
        this.percentageOfCompletion = group.getPercentageOfCompletion();
        this.deadline = group.getDeadline();
        this.dateOnFeed = group.getDateOnFeed();
        this.priority = group.getPriority();
        this.complexity = group.getComplexity();
        this.isInProgress = group.getIsInProgress();
        this.totalTime = group.getTotalTime();
        this.strategies = group.getStrategies();
        this.timetable= group.getTimetable();
        this.resources = group.getResources().stream().map(ResourceDTO::new).collect(Collectors.toList());
        this.subtasks = group.getSubtasks().stream().map(SubtaskDTO::new).collect(Collectors.toList());
        this.sessions = group.getSessions().stream().map(SessionDTO::new).collect(Collectors.toList());
        this.numUser = group.getNumUsers();
        this.actualMembers = group.getActualMembers();
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
    public int getNumUser() {
        return numUser;
    }
    public void setNumUser(int numUser) {
        this.numUser = numUser;
    }
    public int getActualMembers() {
        return actualMembers;
    }
    public LocalDateTime getDateOnFeed() {
        return dateOnFeed;
    }
    public void setActualMembers(int actualMembers) {
        this.actualMembers = actualMembers;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userid) {
        this.userId = userid;
    }
}



