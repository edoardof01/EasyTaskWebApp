package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    public Group(int numUsers, LocalDateTime dateOnFeed, User admin, String name, Topic topic, TaskState state, @Nullable LocalDateTime deadline,
                 String description, int percentageOfCompletion, int complexity, int priority,
                 ArrayList<Timetable> timeTable, int totalTime, DefaultStrategy strategy, ArrayList<Resource> resources) {
        super(name, complexity, description, deadline, percentageOfCompletion, priority, totalTime, topic, state, timeTable, strategy, resources);
        this.numUsers = numUsers;
        this.dateOnFeed = dateOnFeed;
        this.members.add(admin);
        this.calendar = new TaskCalendar();
        Feed.getInstance().addTask(this);
        Feed.getInstance().getContributors().add(admin);
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
                .filter(subtask -> !takenSubtasks.containsValue(subtask))
                .collect(Collectors.toList());
    }


    public boolean getIsComplete() {
        return isComplete;
    }
    public void setIsComplete(boolean isComplete) {
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
            this.getCalendar().removeSessions(user);
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
        Subtask subtask = takenSubtasks.get(user);
        if (subtask != null) {
            this.getCalendar().addSessions(user, subtask);  // Aggiunge le sessioni al calendario del gruppo
        } else {
            throw new IllegalArgumentException("No subtask assigned to the user");
        }
        // Controlla e aggiorna lo stato del task di gruppo
        if (!this.isInProgress()) {
            this.updateIsInProgress(true);
        }

        user.getCalendar().addSessions(takenSubtasks.get(user).getSessions());
        Feed.getInstance().getGroup().remove(this);
    }

    @Override
    public void deleteTask(User user) {
        if (user == admin) {
            for (User member : this.getMembers()) {
                Subtask subtaskOfCompetence = takenSubtasks.get(member);
                member.getCalendar().removeSessions(this);
                this.getCalendar().removeSessions(member);
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
                this.getCalendar().removeSessions(member);
            }
        } else {
            throw new IllegalArgumentException("user not authorized to modify task");
        }
    }

    @Override
    public void completeTaskBySessions(User user) {
        for (User member : this.getMembers()) {
            Subtask subtaskOfCompetence = takenSubtasks.get(member);
            this.getCalendar().removeSessions(member);
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
                member.getCalendar().removeSessions(this);
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

    public void leaveGroupTask(User user) {
        if (!members.contains(user)) {
            throw new IllegalArgumentException("the user is not part of the group");
        }
        user.getCalendar().removeSessions(this);
        this.getCalendar().removeSessions(user);
        ArrayList<Folder> folders = user.getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(this)) {
                    subfolder.getTasks().remove(this);
                    taskRemoved = true;
                }
            }
        }
        this.getMembers().remove(user);
        user.getTasks().remove(this);
        this.setState(TaskState.FREEZED);

        for (User member : this.getMembers()) {
            Subtask subtaskOfCompetence = takenSubtasks.get(member);
            member.getCalendar().removeSessions(this);
            ArrayList<Folder> memberFolders = member.getFolders();
            boolean memberTaskRemoved = false;
            for (Folder folder : memberFolders) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (!memberTaskRemoved && subfolder.getTasks().contains(this)) {
                        subfolder.getTasks().remove(this);
                        memberTaskRemoved = true;
                    }
                    if (subfolder.getType() == SubfolderType.FREEZED) {
                        subfolder.getTasks().add(this);
                    }
                }
            }
        }
        Feed.getInstance().getGroup().add(this);
    }

    public void sendExchangeRequest(User sender, User receiver, Subtask reqSubtask) {
        if (!members.contains(sender) || !members.contains(receiver)) {
            throw new IllegalArgumentException("both the users must be members of the group ");
        }
        if (!takenSubtasks.get(receiver).equals(reqSubtask)) {
            throw new IllegalArgumentException("the requested subtask is not assigned to the receiver");
        }

        // Verifica che il sender abbia un subtask da offrire
        Subtask givenSubtask = takenSubtasks.get(sender);
        if (givenSubtask == null) {
            throw new IllegalArgumentException("the sender doesn't have a subtask to offer");
        }

        // Crea e salva una nuova richiesta di scambio
        Request exchangeRequest = new Request(sender, receiver, this, givenSubtask, reqSubtask);
        if (pendingRequest == null) {
            pendingRequest = new ArrayList<>();
        }
        pendingRequest.add(exchangeRequest);
    }

    public void processExchangeRequest(User receiver, Request request, boolean accept) {
        if (!request.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("The user is not the receiver of this request.");
        }
        this.getPendingRequest().remove(request);
        if (accept) {
            User sender = request.getSender();
            Subtask givenSubtask = request.getGivenSubtask();
            Subtask requestedSubtask = request.getSubtaskToReceive();
            if (!this.getTakenSubtasks().get(sender).equals(givenSubtask) ||
                    !this.getTakenSubtasks().get(receiver).equals(requestedSubtask)) {
                throw new IllegalArgumentException("Subtasks do not match the users' current assignments.");
            }

            this.getTakenSubtasks().put(sender, requestedSubtask);
            this.getTakenSubtasks().put(receiver, givenSubtask);

            sender.getCalendar().removeSubtaskSessionsForGroups(givenSubtask);
            receiver.getCalendar().removeSubtaskSessionsForGroups(requestedSubtask);

            this.getCalendar().moveSessions(sender, receiver);

            sender.getCalendar().addSubtaskSessionsForGroups(requestedSubtask);
            receiver.getCalendar().addSubtaskSessionsForGroups(givenSubtask);
        }
    }

    public void removeMember(User admin, User member, boolean substitute){
        if (!members.contains(admin)) {
            throw new IllegalArgumentException("The admin is not part of the group");
        }
        if (!members.contains(member)) {
            throw new IllegalArgumentException("The member is not part of the group");
        }
        this.getMembers().remove(member);


        if(substitute && !Feed.getInstance().getGroup().contains(member)){
            Feed.getInstance().getGroup().add(this);
            getAvailableSubtasks().add(takenSubtasks.get(member));
            takenSubtasks.remove(member);
            this.getCalendar().removeSessions(member);


            for (User user : this.getMembers()) {
                this.getCalendar().removeSessions(user);
                Subtask subtaskOfCompetence = takenSubtasks.get(user);
                user.getCalendar().removeSessions(this);
                ArrayList<Folder> folders = user.getFolders();
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
        else if(!substitute){
            this.getSubtasks().remove(takenSubtasks.get(member));
            takenSubtasks.remove(member);
        }
    }

}



