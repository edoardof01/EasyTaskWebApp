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
    private List<User> members = new ArrayList<>();

    private LocalDateTime dateOnFeed;
    private boolean isComplete = false;
    @OneToMany
    private List<Request> pendingRequest;
    @ManyToOne
    private TaskCalendar calendar;
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TakenSubtask> takenSubtasks = new ArrayList<>();


    public Group() {}

    public Group(int numUsers,@NotNull User user, LocalDateTime dateOnFeed, String name, Topic topic, @Nullable LocalDateTime deadline,
                 String description,@NotNull List<Subtask> subtasks, List<Session> sessions, int percentageOfCompletion, int priority,
                 Set<Timetable> timeTable, int totalTime, Set<DefaultStrategy> strategies, List<Resource> resources) {
        super(name,user,description, subtasks, sessions, deadline, percentageOfCompletion, priority, totalTime, topic, timeTable, strategies, resources);
        this.numUsers = numUsers;
        this.dateOnFeed = dateOnFeed;
        this.members.add(this.getUser());
        this.calendar = new TaskCalendar();
        Feed.getInstance().getGroup().add(this);
        Feed.getInstance().getContributors().add(this.getUser());
    }

    public List<Request> getPendingRequest() {
        return pendingRequest;
    }
    public void setPendingRequest(List<Request> pendingRequest) {
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
    public List<User> getMembers() {
        return members;
    }
    public List<TakenSubtask> getTakenSubtasks() {
        return takenSubtasks;
    }
    public List<Subtask> getAvailableSubtasks() {
        return this.getSubtasks().stream()
                .filter(subtask -> takenSubtasks.stream()
                        .noneMatch(takenSubtask -> takenSubtask.getSubtask().equals(subtask)))
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
        if (this.getSubtasks().contains(subtask) && takenSubtasks.stream()
                .noneMatch(takenSubtask -> takenSubtask.getSubtask().equals(subtask))) {
            takenSubtasks.add(new TakenSubtask(this,user, subtask));
            if (this.getSubtasks().size() == takenSubtasks.size()) {
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
            // Trovo il TakenSubtask che contiene il subtask del membro
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskOfCompetence = takenSubtask.getSubtask();

            // Rimuovo il subtask dal calendario del membro
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);

            // Rimuovo il subtask dal calendario del gruppo
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
            throw new UnsupportedOperationException("The group is not complete");
        }

        // Troviamo il TakenSubtask associato al user
        TakenSubtask takenSubtask = takenSubtasks.stream()
                .filter(ts -> ts.getUser().equals(this.getUser()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to the user"));

        Subtask subtask = takenSubtask.getSubtask();

        // Aggiungiamo le sessioni al calendario del gruppo
        this.getCalendar().addSessions(this.getUser(), subtask);

        // Controlla e aggiorna lo stato del task di gruppo
        if (!this.isInProgress()) {
            this.updateIsInProgress();
        }

        // Aggiungiamo le sessioni al calendario dell'utente
        this.getUser().getCalendar().addSessions(subtask.getSessions());

        // Rimuoviamo il task dal feed del gruppo
        Feed.getInstance().getGroup().remove(this);
    }


    public void toCalendarForUser(User user) {
        if (this.isInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED || this.getState() == TaskState.FREEZED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }

        // Troviamo il TakenSubtask associato all'utente
        TakenSubtask takenSubtask = takenSubtasks.stream()
                .filter(ts -> ts.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to this user"));

        Subtask subtask = takenSubtask.getSubtask();

        // Aggiungiamo le sessioni al calendario dell'utente e al calendario del gruppo
        user.getCalendar().addSessions(subtask.getSessions());
        this.getCalendar().addSessions(user, subtask);

        // Controlla e aggiorna lo stato del task di gruppo se necessario
        if (!this.isInProgress()) {
            this.updateIsInProgress();
        }
    }



    @Override
    public void deleteTask() {
        for (User member : this.getMembers()) {
            // Troviamo il TakenSubtask associato al membro
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskOfCompetence = takenSubtask.getSubtask();

            // Rimuoviamo le sessioni dal calendario dell'utente e dal calendario del gruppo
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
            this.getCalendar().removeSessions(member);

            // Rimuoviamo il task dalle cartelle
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
        }
        // Rimuove il task dal feed del gruppo
        Feed.getInstance().getGroup().remove(this);
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
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
            this.getCalendar().removeSessions(member);

            for (Session session : subtaskOfCompetence.getSessions()) {
                if (session.getState() != SessionState.COMPLETED) {
                    throw new UnsupportedOperationException("the task can't be completed normally");
                }
            }
        }
        this.setState(TaskState.FINISHED);
        for (User member : this.getMembers()) {
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
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
            // Troviamo il TakenSubtask associato al membro
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));
            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
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
        boolean isSubtaskTaken = takenSubtasks.stream()
                .anyMatch(takenSubtask -> takenSubtask.getSubtask().equals(subtask));

        if (!this.getSubtasks().contains(subtask) || isSubtaskTaken) {
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
        if (this.getSubtasks().size() == takenSubtasks.size()) {
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
            // Ottieni il subtask assegnato a questo membro
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(t -> t.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Subtask not found for member"));
            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
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
            throw new IllegalArgumentException("both the users must be members of the group");
        }
        TakenSubtask senderTakenSubtask = takenSubtasks.stream()
                .filter(t -> t.getUser().equals(sender))
                .findFirst()
                .orElse(null);
        if (senderTakenSubtask == null || senderTakenSubtask.getSubtask() == null) {
            throw new IllegalArgumentException("the sender doesn't have a subtask to offer");
        }
        TakenSubtask receiverTakenSubtask = takenSubtasks.stream()
                .filter(t -> t.getUser().equals(receiver))
                .findFirst()
                .orElse(null);
        if (receiverTakenSubtask == null || receiverTakenSubtask.getSubtask() == null) {
            throw new IllegalArgumentException("the receiver doesn't have a subtask to exchange");
        }
        Request exchangeRequest = new Request(sender, receiver, this, senderTakenSubtask.getSubtask(), receiverTakenSubtask.getSubtask());
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

            // Controlliamo che i subtasks corrispondano a quelli attualmente assegnati agli utenti
            TakenSubtask senderTakenSubtask = takenSubtasks.stream()
                    .filter(t -> t.getUser().equals(sender))
                    .findFirst()
                    .orElse(null);

            TakenSubtask receiverTakenSubtask = takenSubtasks.stream()
                    .filter(t -> t.getUser().equals(receiver))
                    .findFirst()
                    .orElse(null);

            if (senderTakenSubtask == null || receiverTakenSubtask == null ||
                    !senderTakenSubtask.getSubtask().equals(givenSubtask) ||
                    !receiverTakenSubtask.getSubtask().equals(requestedSubtask)) {
                throw new IllegalArgumentException("Subtasks do not match the users' current assignments.");
            }

            // Scambio i subtasks tra il sender e il receiver
            senderTakenSubtask.setSubtask(requestedSubtask);
            receiverTakenSubtask.setSubtask(givenSubtask);

            // Rimuove le sessioni per i subtasks coinvolti
            sender.getCalendar().removeSubtaskSessionsForGroups(givenSubtask);
            receiver.getCalendar().removeSubtaskSessionsForGroups(requestedSubtask);

            // Sposta le sessioni tra il sender e il receiver
            this.getCalendar().moveSessions(sender, receiver);

            // Aggiunge le sessioni per i nuovi subtasks
            sender.getCalendar().addSubtaskSessionsForGroups(requestedSubtask);
            receiver.getCalendar().addSubtaskSessionsForGroups(givenSubtask);
        }
    }


    public void removeMember(User member, boolean substitute) {
        // Verifica che il membro sia effettivamente parte del gruppo
        if (!members.contains(member)) {
            throw new IllegalArgumentException("The member is not part of the group");
        }

        // Rimuovi il membro dal gruppo e decrementa il numero di membri effettivi
        this.getMembers().remove(member);
        actualMembers--;

        // Se si sta sostituendo il membro, esegui le operazioni correlate
        if (substitute) {
            // Aggiungi il gruppo al Feed se non è già presente
            if (!Feed.getInstance().getGroup().contains(this)) {
                Feed.getInstance().getGroup().add(this);
            }

            // Ottieni il TakenSubtask del membro e rimuovilo dalla mappa
            TakenSubtask takenSubtask = getTakenSubtaskForMember(member);
            getAvailableSubtasks().add(takenSubtask.getSubtask());  // Ripristina il subtask tra quelli disponibili
            takenSubtasks.remove(member);  // Rimuovi il TakenSubtask

            // Rimuovi le sessioni del membro dal calendario del gruppo e dell'utente
            this.getCalendar().removeSessions(member);
            member.getCalendar().removeSessions(this);

            // Per ogni membro restante, aggiorna il calendario e le cartelle
            for (User user : this.getMembers()) {
                // Rimuovi le sessioni dal calendario dell'utente
                this.getCalendar().removeSessions(user);
                TakenSubtask subtaskOfCompetence = getTakenSubtaskForMember(user);

                // Rimuovi le sessioni del subtask dal calendario dell'utente
                user.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence.getSubtask());

                // Rimuovi il task dalla cartella dell'utente e aggiungilo nella cartella FREEZED
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
        } else {
            // Se non si sta sostituendo il membro, rimuovi il TakenSubtask senza sostituirlo
            TakenSubtask takenSubtask = getTakenSubtaskForMember(member);
            this.getSubtasks().remove(takenSubtask.getSubtask());  // Rimuovi il subtask dal gruppo
            takenSubtasks.remove(member);  // Rimuovi il TakenSubtask
        }
    }

    private TakenSubtask getTakenSubtaskForMember(User member) {
        // Iteriamo sulla lista per trovare il TakenSubtask associato al membro
        for (TakenSubtask ts : takenSubtasks) {
            if (ts.getUser().equals(member)) {
                return ts;
            }
        }
        throw new IllegalArgumentException("The user does not have a subtask assigned.");
    }



}



