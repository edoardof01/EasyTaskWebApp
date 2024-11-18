package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@DiscriminatorValue("group")
public class Group extends Task {
    private int numUsers;
    private int actualMembers = 1;
    @ManyToMany
    private ArrayList<User> members = new ArrayList<>();
    @ElementCollection
    private ArrayList<Integer> skippedSessionPerUser;
    private LocalDateTime dateOnFeed;
    private boolean isComplete = false;
    @OneToMany
    private ArrayList<Request> pendingRequest;
    @ManyToOne
    private TaskCalendar calendar;
    @OneToMany
    private List<Subtask> subtasks = new ArrayList<>();
    @OneToMany
    private Map<@NotNull User, @NotNull Subtask> takenSubtasks = new HashMap<>();


    public Group() {}

    public Group(int numUsers, User user, LocalDateTime dateOnFeed, String name, Topic topic, TaskState state, @Nullable LocalDateTime deadline,
                 String description, int percentageOfCompletion, int priority,
                 Set<Timetable> timeTable, int totalTime, Set<DefaultStrategy> strategy, List<Resource> resources) {
        super(name,user,description, deadline, percentageOfCompletion, priority, totalTime, topic, state, timeTable, strategy, resources);
        this.numUsers = numUsers;
        this.dateOnFeed = dateOnFeed;
        this.members.add(this.getUser());
        this.calendar = new TaskCalendar();
        Feed.getInstance().getGroup().add(this);
        Feed.getInstance().getContributors().add(this.getUser());
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
    public int getActualMembers() {
        return actualMembers;
    }
    public void setNumUsers(int numUsers) {
        this.numUsers = numUsers;
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
        actualMembers++;
    }

    public void assignSubtaskToUser(User user, Subtask subtask) {
        if (subtasks.contains(subtask) && !takenSubtasks.containsValue(subtask)) {
            takenSubtasks.put(user, subtask);
            if (subtasks.size() == takenSubtasks.size()) {
                isComplete = true;
            }
        } else {
            throw new IllegalArgumentException("Subtask not available or already assigned.");
        }
    }

    @Override
    public void handleLimitExceeded() {
        // Rimuovo il subtask di competenza dal calendario di ogni membro e sposto il task dal loro subfolder INPROGRESS a quello FREEZED
        for (User member : this.getMembers()) {
            Subtask subtaskOfCompetence = takenSubtasks.get(member);
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
            this.getCalendar().removeSessions(member);
            List<Folder> folders = member.getFolders();
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
    public void toCalendar() {

        if (this.isInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED || this.getState() == TaskState.FREEZED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        if (!isComplete) {
            throw new UnsupportedOperationException("the group is not complete");
        }
        Subtask subtask = takenSubtasks.get(this.getUser());
        if (subtask != null) {
            this.getCalendar().addSessions(this.getUser(), subtask);  // Aggiunge le sessioni al calendario del gruppo
        } else {
            throw new IllegalArgumentException("No subtask assigned to the user");
        }
        // Controlla e aggiorna lo stato del task di gruppo
        if (!this.isInProgress()) {
            this.updateIsInProgress();
        }

        this.getUser().getCalendar().addSessions(takenSubtasks.get(this.getUser()).getSessions());
        Feed.getInstance().getGroup().remove(this);
    }


    public void toCalendarForUser(User user) {
        if (this.isInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED || this.getState() == TaskState.FREEZED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        Subtask subtask = takenSubtasks.get(user);
        if (subtask != null) {
            user.getCalendar().addSessions(subtask.getSessions()); // Aggiunge le sessioni al calendario dell'utente
            this.getCalendar().addSessions(user, subtask); // Aggiunge le sessioni al calendario del task di gruppo
        } else {
            throw new IllegalArgumentException("No subtask assigned to this user");
        }

        // Controlla e aggiorna lo stato del task di gruppo se necessario
        if (!this.isInProgress()) {
            this.updateIsInProgress();
        }
    }


    @Override
    public void deleteTask() {
            for (User member : this.getMembers()) {
                Subtask subtaskOfCompetence = takenSubtasks.get(member);
                member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
                this.getCalendar().removeSessions(member);
                List<Folder> folders = member.getFolders();
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
        }


    @Override
    public void modifyTask() {
            for (User member : this.getMembers()) {
                commonModifyLogic(member);
                this.getCalendar().removeSessions(member);
            }
    }

    @Override
    public void completeTaskBySessions() {
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
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
            member.getCalendar().removeSessions(this);
            List<Folder> folders = member.getFolders();
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
    public void forcedCompletion() {
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
            this.getUser().getCalendar().removeSessions(this);
            List<Folder> folders = this.getUser().getFolders();
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

    public void joinGroup(@NotNull User user, Subtask subtask) {
        if (!subtasks.contains(subtask) || takenSubtasks.containsValue(subtask)) {
            throw new IllegalArgumentException("Subtask does not exist or is already taken.");
        }
        if (isComplete) {
            throw new UnsupportedOperationException("The group is already complete.");
        }
        if (members.contains(user)) {
            throw new IllegalArgumentException("User is already member of this group.");
        }
        assignSubtaskToUser(user, subtask);
        calendar.addSessions(user, subtask);
        user.getCalendar().addSessions(subtask.getSessions());
        this.addMember(user);
        if (subtasks.size() == takenSubtasks.size()) {
            isComplete = true;
        }
    }


    public void leaveGroupTask(User user) {
        if (!members.contains(user)) {
            throw new IllegalArgumentException("the user is not part of the group");
        }
        actualMembers--;
        user.getCalendar().removeSessions(this);
        this.getCalendar().removeSessions(user);
        List<Folder> folders = user.getFolders();
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
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
            List<Folder> memberFolders = member.getFolders();
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

    public void sendExchangeRequest(User sender, User receiver) {
        if (!members.contains(sender) || !members.contains(receiver)) {
            throw new IllegalArgumentException("both the users must be members of the group ");
        }


        // Verifica che il sender abbia un subtask da offrire
        Subtask givenSubtask = takenSubtasks.get(sender);
        if (givenSubtask == null) {
            throw new IllegalArgumentException("the sender doesn't have a subtask to offer");
        }

        // Crea e salva una nuova richiesta di scambio
        Subtask receiverSubtask = takenSubtasks.get(receiver);
        Request exchangeRequest = new Request(sender, receiver, this, givenSubtask, receiverSubtask );
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

    public void removeMember( User member, boolean substitute){
        if (!members.contains(member)) {
            throw new IllegalArgumentException("The member is not part of the group");
        }
        this.getMembers().remove(member);
        actualMembers--;
        if(substitute && !Feed.getInstance().getGroup().contains(this)){
            Feed.getInstance().getGroup().add(this);
            getAvailableSubtasks().add(takenSubtasks.get(member));
            takenSubtasks.remove(member);
            this.getCalendar().removeSessions(member);


            for (User user : this.getMembers()) {
                this.getCalendar().removeSessions(user);
                Subtask subtaskOfCompetence = takenSubtasks.get(user);
                user.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
                List<Folder> folders = user.getFolders();
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


    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }
}



