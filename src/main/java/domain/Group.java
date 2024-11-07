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
    private Map<@NotNull User, @NotNull Subtask> takenSubtasks = new HashMap<>();


    public Group() {
    }


    public Group(int numUsers, LocalDateTime dateOnFeed, User admin, String name, Topic topic, TaskState state, LocalDateTime deadline,
                 String description, int percentageOfCompletion, int complexity, int priority,
                 Timetable timeTable, int totalTime, DefaultStrategy strategy, ArrayList<Resource> resources) {
        super(name, complexity, description, deadline, percentageOfCompletion, priority, totalTime, topic, state, timeTable, strategy, resources);
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

    public TaskCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(TaskCalendar calendar) {
        this.calendar = calendar;
    }

    public ArrayList<User> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<User> members) {
    }

    public ArrayList<Integer> getSkippedSessionPerUser() {
        return skippedSessionPerUser;
    }

    public Map<User, Subtask> getTakenSubtasks() {
        return takenSubtasks;
    }

    public List<Subtask> getAvailableSubtasks() {
        return subtasks.stream()
                .filter(subtask -> !takenSubtasks.containsKey(subtask))
                .collect(Collectors.toList());
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public void addMember(User member) {
        members.add(member);
    }

    public void assignSubtaskToUser(User user, Subtask subtask) {
        if (subtasks.contains(subtask) && !takenSubtasks.containsKey(subtask)) {
            takenSubtasks.put(user, subtask);
            if (subtasks.size() == takenSubtasks.size()) {
                isComplete = true;
            }
            // Potresti anche voler gestire ulteriori logiche come la notifica all'utente o l'aggiornamento dello stato del subtask
        } else {
            throw new IllegalArgumentException("Subtask not available or already assigned.");
        }
    }

    @Override
    public void handleLimitExceeded(User user) {
        // Rimuovo il subtask di competenza dal calendario di ogni membro e sposto il task dal loro subfolder INPROGRESS a quello FREEZED
        for (User member : this.getMembers()) {
            Subtask subtaskOfCompetence = takenSubtasks.get(member);
            member.getCalendar().removeSessions(this);
            ArrayList<Folder> folders = member.getFolders();
            boolean taskRemoved = false;
            for (Folder folder : folders) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (!taskRemoved && subfolder.getTasks().contains(this)) {
                        subfolder.getTasks().remove(this);
                        taskRemoved = true;
                    }
                    if (subfolder.getType() == SubfolderType.FREEZED) {
                        subfolder.getTasks().add(this);
                    }
                }
            }
        }
    }

    @Override
    public void toCalendar(User user) {
        if (this.isInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED || this.getState() == TaskState.FREEZED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        if (!isComplete) {
            throw new UnsupportedOperationException("the group is not complete");
        }
        user.getCalendar().addSessions(takenSubtasks.get(user).getSessions());
        Feed.getInstance().getGroup().add(this);
    }

    @Override
    public void deleteTask(User user) {
        if (user == admin) {
            for (User member : this.getMembers()) {
                Subtask subtaskOfCompetence = takenSubtasks.get(member);
                member.getCalendar().removeSessions(this);
                ArrayList<Folder> folders = member.getFolders();
                boolean taskRemoved = false;
                for (Folder folder : folders) {
                    for (Subfolder subfolder : folder.getSubfolders()) {
                        if (!taskRemoved && subfolder.getTasks().contains(this)) {
                            subfolder.getTasks().remove(this);
                            taskRemoved = true;
                        }
                    }
                }
                Feed.getInstance().getGroup().remove(this);
            }
        } else {
            throw new IllegalArgumentException("user not authorized to delete task");
        }
    }

    @Override
    public void modifyTask(User user) {
        if (user == admin) {
            for (User member : this.getMembers()) {
                commonModifyLogic(member);
            }
        } else {
            throw new IllegalArgumentException("user not authorized to modify task");
        }
    }

    @Override
    public void completeTaskBySessions(User user) {
        for (User member : this.getMembers()) {
            Subtask subtaskOfCompetence = takenSubtasks.get(member);
            for (Session session : subtaskOfCompetence.getSessions()) {
                if (session.getState() != SessionState.COMPLETED) {
                    throw new UnsupportedOperationException("the task can't be completed normally");
                }
            }
        }
        this.setState(TaskState.FINISHED);
        for (User member : this.getMembers()) {
            Subtask subtaskOfCompetence = takenSubtasks.get(member);
            member.getCalendar().removeSessions(this);
            ArrayList<Folder> folders = member.getFolders();
            boolean taskRemoved = false;
            for (Folder folder : folders) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (!taskRemoved && subfolder.getTasks().contains(this)) {
                        subfolder.getTasks().remove(this);
                        taskRemoved = true;
                    }
                    if (subfolder.getType() == SubfolderType.FINISHED) {
                        subfolder.getTasks().add(this);
                    }
                }
            }
        }

    }

    @Override
    public void forcedCompletion(User user) {
        if (user == admin) {
            for (User member : this.getMembers()) {
                Subtask subtaskOfCompetence = takenSubtasks.get(member);
                for (Session session : subtaskOfCompetence.getSessions()) {
                    if (session.getState() != SessionState.COMPLETED) {
                        session.setState(SessionState.COMPLETED);
                    }
                }
            }
            this.setState(TaskState.FINISHED);
            user.getCalendar().removeSessions(this);
            ArrayList<Folder> folders = user.getFolders();
            boolean taskRemoved = false;
            for (Folder folder : folders) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (!taskRemoved && subfolder.getTasks().contains(this)) {
                        subfolder.getTasks().remove(this);
                        taskRemoved = true;
                    }
                    if (subfolder.getType() == SubfolderType.FINISHED) {
                        subfolder.getTasks().add(this);
                    }
                }
            }

            }
        else{throw new IllegalArgumentException("user not authorized to forcedCompletion");}
    }


}



