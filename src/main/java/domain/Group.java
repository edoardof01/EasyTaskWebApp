package domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.swing.UIManager.put;

@Entity
@DiscriminatorValue("group")
public class Group extends Task {
    private int numUsers;
    @ManyToMany
    private ArrayList<User> members = new ArrayList<>();
    @ElementCollection
    private ArrayList<Integer> skippedSessionPerUser;
    @ManyToOne
    private User admin;
    private LocalDateTime dateOnFeed;
    private boolean isComplete = false;
    @OneToMany
    private ArrayList<Request> pendingRequest;
    @ManyToOne
    private TaskCalendar calendar;
    @OneToMany
    private ArrayList<Subtask> subtasks = new ArrayList<>();
    @OneToMany
    private Map<@NotNull User,@NotNull Subtask> takenSubtasks = new HashMap<>();




    public Group() {}


    public Group(int numUsers, LocalDateTime dateOnFeed,User admin,String name, Topic topic, TaskState state, LocalDateTime deadline,
                 String description, int percentageOfCompletion, int complexity, int priority,
                 Timetable timeTable, int totalTime,DefaultStrategy strategy,ArrayList<Resource> resources) {
        super(name,complexity,description,deadline,percentageOfCompletion,priority,totalTime,topic,state,timeTable,strategy,resources);
        this.numUsers = numUsers;
        this.dateOnFeed = dateOnFeed;
        this.members.add(admin);
    }

    public ArrayList<Request> getPendingRequest() {
        return pendingRequest;
    }
    public void setPendingRequest(ArrayList<Request> pendingRequest) {
        this.pendingRequest = pendingRequest;
    }
    public int getNumUsers() {
        return numUsers;
    }
    public User getAdmin() {
        return admin;
    }
    public LocalDateTime getDateOnFeed() {
        return dateOnFeed;
    }
    public void setDateOnFeed(LocalDateTime dateOnFeed) {
        this.dateOnFeed = dateOnFeed;
    }
    public boolean isComplete() {
        return isComplete;
    }
    public void addMember(User member) {
        members.add(member);
    }
    public TaskCalendar getCalendar() {
        return calendar;
    }
    public void setCalendar(TaskCalendar calendar) {
        this.calendar = calendar;
    }
    public ArrayList<User> getMembers() {
        return members;
    }
    public void setMembers(ArrayList<User> members) {}
    public ArrayList<Integer> getSkippedSessionPerUser() {
        return skippedSessionPerUser;
    }
    public Map<User,Subtask> getTakenSubtasks() {
        return takenSubtasks;
    }

    public void assignSubtaskToUser(User user, Subtask subtask) {
        if (subtasks.contains(subtask) && !takenSubtasks.containsKey(subtask)) {
            takenSubtasks.put(user, subtask);
            if(subtasks.size()== takenSubtasks.size()){
                isComplete = true;
            }
            // Potresti anche voler gestire ulteriori logiche come la notifica all'utente o l'aggiornamento dello stato del subtask
        } else {
            throw new IllegalArgumentException("Subtask not available or already assigned.");
        }
    }
    public List<Subtask> getAvailableSubtasks() {
        return subtasks.stream()
                .filter(subtask -> !takenSubtasks.containsKey(subtask))
                .collect(Collectors.toList());
    }
    @Override
    public void handleLimitExceeded() {

    }

    @Override
    public void toCalendar(Calendar calendar, User user) {
        if (this.isInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED || this.getState() == TaskState.FREEZED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        if(!isComplete){
            throw new UnsupportedOperationException("the group is not complete");
        }
        calendar.addSessions(takenSubtasks.get(user).getSessions());
        Feed.getInstance().getGroup().add(this);
        }
    }



