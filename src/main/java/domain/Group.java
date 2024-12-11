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
    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    private TaskCalendar calendar;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TakenSubtask> takenSubtasks = new ArrayList<>();


    public Group() {
    }

    public Group(int numUsers, @NotNull User user, LocalDateTime dateOnFeed, String name, Topic topic, @Nullable LocalDateTime deadline,
                 String description, @NotNull List<Subtask> subtasks, List<Session> sessions, int percentageOfCompletion, int priority,
                 Timetable timeTable, int totalTime, List<StrategyInstance> strategies, List<Resource> resources) {
        super(name, user, description, subtasks, sessions, deadline, percentageOfCompletion, priority, totalTime, topic, timeTable, strategies, resources);
        this.numUsers = numUsers;
        this.dateOnFeed = dateOnFeed;
        this.members.add(this.getUser());
        this.calendar = new TaskCalendar();
        this.getCalendar().setGroup(this);
        Feed.getInstance().getGroup().add(this);
        Feed.getInstance().getContributors().add(this.getUser());
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
        if (this.getSubtasks().stream()
                .anyMatch(availableSubtask -> availableSubtask.equals(subtask)) &&
                this.getTakenSubtasks().stream()
                        .noneMatch(takenSubtask -> takenSubtask.getSubtask().equals(subtask))) {

            this.getTakenSubtasks().add(new TakenSubtask(user, subtask));

            if (this.getSubtasks().size() == this.getTakenSubtasks().size()) {
                this.isComplete = true;
            }
        } else {
            throw new IllegalArgumentException("Subtask not available or already assigned.");
        }
    }


    @Override
    public void handleLimitExceeded() {

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
        }
        this.setState(TaskState.FREEZED);
        this.setIsInProgress(false);
    }


    @Override
    public void toCalendar() {
        if (this.getIsInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        if (!isComplete) {
            throw new UnsupportedOperationException("The group is not complete");
        }

        // Itera su tutti i membri, inclusi l'amministratore
        for (User member : this.getMembers()) {
            // Troviamo il TakenSubtask associato all'utente
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to the user: " + member));

            Subtask subtask = takenSubtask.getSubtask();

            // Aggiungi le sessioni al calendario del gruppo
            this.getCalendar().addSessions(member, subtask);

            // Aggiungi le sessioni al calendario dell'utente
            member.getCalendar().addSubtaskSessionsForGroups(subtask);

            // Controlla e aggiorna lo stato del task di gruppo
            if (!this.getIsInProgress()) {
                this.updateIsInProgress();
            }
        }

        // Rimuovi il task dal feed del gruppo
        Feed.getInstance().getGroup().remove(this);
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
        }
            // Rimuove il task dal feed del gruppo
            Feed.getInstance().getGroup().remove(this);
    }


    @Override
    public void modifyTask() {
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to freezed");
        }
        this.setState(TaskState.FREEZED);
        this.setIsInProgress(false);

        for (User member : this.getMembers()) {
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskInstance = takenSubtask.getSubtask();

            // Sostituire l'istanza "cattiva" con quella corretta
            Subtask correctSubtask = this.getSubtasks().stream()
                    .filter(subtask -> subtask.equals(subtaskInstance))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No matching subtask found in task"));

            // Aggiornare l'istanza nel TakenSubtask
            takenSubtask.setSubtask(correctSubtask);

            // Rimuovere le sessioni dal calendario del membro
            member.getCalendar().removeSubtaskSessionsForGroups(correctSubtask);
        }
    }




    @Override
    public void completeTaskBySessions() {
        // Ottieni la sessione con l'ultima endDate
        Session lastSession = this.getSessions().stream()
                .max(Comparator.comparing(Session::getEndDate))
                .orElseThrow(() -> new IllegalStateException("No sessions available for this task"));

        // Verifica che tutte le sessioni tranne l'ultima siano COMPLETED
        for (Session session : this.getSessions()) {
            if (!session.equals(lastSession) && session.getState() != SessionState.COMPLETED) {
                throw new IllegalStateException("All sessions except the last one must be completed.");
            }
        }
        // Verifica che l'ultima sessione sia PROGRAMMED
        if (lastSession.getState() != SessionState.PROGRAMMED) {
            throw new IllegalStateException("The last session must be in PROGRAMMED state.");
        }
        lastSession.setState(SessionState.COMPLETED);
        for (User member : this.getMembers()) {
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskOfCompetence = takenSubtask.getSubtask();

            for (Session session : subtaskOfCompetence.getSessions()) {
                if (session.getState() != SessionState.COMPLETED) {
                    session.setState(SessionState.COMPLETED);
                }
            }
        }

        setState(TaskState.FINISHED);
        for (User member : this.getMembers()) {
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
            /// Calendario utente
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
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
            for (Session session : subtaskOfCompetence.getSessions()) {
                if (session.getState() != SessionState.COMPLETED) {
                    session.setState(SessionState.COMPLETED);
                }
            }
        }
        this.setState(TaskState.FINISHED);
        for (Session session: this.getSessions()){
            if(session.getState() != SessionState.COMPLETED){
                session.setState(SessionState.COMPLETED);
            }
        }


    }

    @Override
    public void completeSession(Session session) {
        if (session.getState() != SessionState.PROGRAMMED) {
            throw new IllegalStateException("Only sessions programmed can be completed.");
        }
        session.setState(SessionState.COMPLETED);

        boolean found = false;
        for(Session taskSession:this.getSessions()){
            if (taskSession.equals(session)) {
                found = true;
                break;
            }
        }
        if(!found){
            throw new IllegalStateException("the session you want to complete doesn't belong to this group task");
        }
        if(this.doExistNextSession(session)){
            completeTaskBySessions();
        }
        else {
            for (User member : this.getMembers()) {
                TakenSubtask takenSubtask = takenSubtasks.stream()
                        .filter(ts -> ts.getUser().equals(member))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));
                Subtask subtaskOfCompetence = takenSubtask.getSubtask();
                for (Session subSession : subtaskOfCompetence.getSessions()) {
                    if (subSession.equals(session)) {
                        subSession.setState(SessionState.COMPLETED);
                    }
                }
            }
            resetConsecutiveSkippedSessions();
            int completedCounter = 0;
            for (Session taskSession : this.getSessions()) {
                if (taskSession.getState() == SessionState.COMPLETED) {
                    completedCounter += 1;
                }
            }
            int percentageOfCompletion = (completedCounter / this.getSessions().size()) * 100;
            this.setPercentageOfCompletion(percentageOfCompletion);

            for (User member : this.getMembers()) {
                for (Session subSession : member.getCalendar().getSessions()) {
                    if (subSession.equals(session)) {
                        subSession.setState(SessionState.COMPLETED);
                        break;
                    }
                }
            }
        }
    }

    public void completeSubtaskSession( Session session) {
        if (session.getState() != SessionState.PROGRAMMED) {
            throw new IllegalStateException("Only sessions programmed can be completed.");
        }
        session.setState(SessionState.COMPLETED);
        for(Session taskSession: this.getSessions()) {
            if(taskSession.equals(session)){
                this.completeSession(taskSession);
                break;
            }
        }
    }

    public void joinGroup(@NotNull User user, @NotNull Subtask subtask) {
        boolean isSubtaskTaken = takenSubtasks.stream()
                .anyMatch(takenSubtask -> takenSubtask.getSubtask().equals(subtask));

        if (isSubtaskTaken) {
            throw new IllegalArgumentException("Subtask does not exist or is already taken.");
        }
        if (isComplete) {
            throw new UnsupportedOperationException("The group is already complete.");
        }

        if (members.contains(user)) {
            throw new IllegalArgumentException("User is already member of this group.");
        }
        this.assignSubtaskToUser(user, subtask);
        this.addMember(user);

        // è il TaskCalendar
        this.calendar.addSessions(user, subtask);
        if(actualMembers == numUsers){
            isComplete = true;
        }
        if (this.getSubtasks().size() == takenSubtasks.size()) {
            isComplete = true;
        }
    }



    public void leaveGroupTask(User user) {
        if (!members.contains(user)) {
            throw new IllegalArgumentException("the user is not part of the group");
        }
        actualMembers--;
        this.getCalendar().removeSessions(user);
        user.getTasks().remove(this);
        this.setState(TaskState.FREEZED);
        TakenSubtask takenLeftSubtask = takenSubtasks.stream()
                .filter(t -> t.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("TakenSubtask not found for the user"));
        this.getTakenSubtasks().remove(takenLeftSubtask);
        this.getMembers().remove(user);
        for (User member : this.getMembers()) {
            // Ottieni il subtask assegnato a questo membro
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(t -> t.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Subtask not found for member"));
            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
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
        receiver.getPendingRequests().add(exchangeRequest);
    }



    public void processExchangeRequest(User receiver, Request request, boolean accept) {
        if (accept) {
            Subtask senderSubtask = this.getSubtasks().stream()
                    .filter(subtask -> subtask.equals(request.getGivenSubtask()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Given subtask not found in group's subtasks."));

            Subtask receiverSubtask = this.getSubtasks().stream()
                    .filter(subtask -> subtask.equals(request.getSubtaskToReceive()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Subtask to receive not found in group's subtasks."));

            TakenSubtask senderTakenSubtask = this.getTakenSubtasks().stream()
                    .filter(t -> t.getSubtask().equals(senderSubtask))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Sender does not have an assigned subtask."));

            TakenSubtask receiverTakenSubtask = this.getTakenSubtasks().stream()
                    .filter(t -> t.getSubtask().equals(receiverSubtask))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Receiver does not have an assigned subtask."));

            if (!senderTakenSubtask.getSubtask().equals(senderSubtask) || !receiverTakenSubtask.getSubtask().equals(receiverSubtask)) {
                throw new IllegalArgumentException("Current TakenSubtasks do not match the requested exchange.");
            }


            takenSubtasks.remove(senderTakenSubtask);
            takenSubtasks.remove(receiverTakenSubtask);

            TakenSubtask newSenderTakenSubtask = new TakenSubtask(request.getSender(), receiverSubtask);
            TakenSubtask newReceiverTakenSubtask = new TakenSubtask(receiver, senderSubtask);

            takenSubtasks.add(newSenderTakenSubtask);
            takenSubtasks.add(newReceiverTakenSubtask);

            this.calendar.moveSessions(request.getSender(), request.getReceiver());

            receiver.getCalendar().removeSubtaskSessionsForGroups(receiverSubtask);
            request.getSender().getCalendar().removeSubtaskSessionsForGroups(senderSubtask);

            receiver.getCalendar().addSubtaskSessionsForGroups(senderSubtask);
            request.getSender().getCalendar().addSubtaskSessionsForGroups(receiverSubtask);

            receiver.getPendingRequests().remove(request);
        } else {
            receiver.getPendingRequests().remove(request);
        }
    }

    public void addTakenSubtasksAndSessions(User receiver, Request request) {
        Subtask senderSubtask = this.getSubtasks().stream()
                .filter(subtask -> subtask.equals(request.getGivenSubtask()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Given subtask not found in group's subtasks."));

        Subtask receiverSubtask = this.getSubtasks().stream()
                .filter(subtask -> subtask.equals(request.getSubtaskToReceive()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Subtask to receive not found in group's subtasks."));


        TakenSubtask newSenderTakenSubtask = new TakenSubtask(request.getSender(), receiverSubtask);
        TakenSubtask newReceiverTakenSubtask = new TakenSubtask(receiver, senderSubtask);

        takenSubtasks.add(newSenderTakenSubtask);
        takenSubtasks.add(newReceiverTakenSubtask);

        receiver.getCalendar().addSubtaskSessionsForGroups(senderSubtask);
        request.getSender().getCalendar().addSubtaskSessionsForGroups(receiverSubtask);
    }









    public void removeMember(User admin,User member, boolean substitute) {

        if(this.getUser().equals(member)){
            throw new IllegalArgumentException("Only admin users can remove members");
        }
        // Verifica che il membro sia effettivamente parte del gruppo
        if (!members.contains(member) || !members.contains(admin)) {
            throw new IllegalArgumentException("The member is not part of the group");
        }


        // Se si sta sostituendo il membro, esegui le operazioni correlate
        if (substitute) {
            // Aggiungi il gruppo al Feed se non è già presente
            boolean found = false;
            for(Group group:Feed.getInstance().getGroup()){
                if (group.equals(this)) {
                    found = true;
                    break;
                }
            }
            if(!found){
                Feed.getInstance().getGroup().add(this);
            }

            this.setState(TaskState.FREEZED);

            // Ottieni il TakenSubtask del membro e rimuovilo dalla lista
            TakenSubtask takenSubtask = getTakenSubtaskForMember(member);
            getAvailableSubtasks().add(takenSubtask.getSubtask());  // Ripristina il subtask tra quelli disponibili
            takenSubtasks.remove(takenSubtask);  // Rimuovi il TakenSubtask

            // Rimuovi le sessioni del membro dal calendario del gruppo e dell'utente
            this.getCalendar().removeSessions(member);

            member.getCalendar().removeSubtaskSessionsForGroups(takenSubtask.getSubtask());
            this.getMembers().remove(member);
            // Per ogni membro restante, aggiorna il calendario
            for (User user : this.getMembers()) {
                // Rimuovi le sessioni dal calendario dell'utente
                this.getCalendar().removeSessions(user);
                TakenSubtask subtaskOfCompetence = getTakenSubtaskForMember(user);
                // Rimuovi le sessioni del subtask dal calendario dell'utente
                user.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence.getSubtask());
            }
            actualMembers--;

        } else {
            // Se non si sta sostituendo il membro, rimuovi il TakenSubtask senza sostituirlo
            TakenSubtask takenSubtask = getTakenSubtaskForMember(member);
            this.getSubtasks().remove(takenSubtask.getSubtask());  // Rimuovi il subtask dal gruppo
            takenSubtasks.remove(takenSubtask);  // Rimuovi il TakenSubtask
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



