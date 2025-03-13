package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private boolean isOnFeed = false;


    public Group() {
    }

    public Group(int numUsers, @NotNull User user, String name, Topic topic, @Nullable LocalDateTime deadline,
                 String description, @NotNull List<Subtask> subtasks, List<Session> sessions, int percentageOfCompletion, int priority,
                 Timetable timeTable, int totalTime, List<StrategyInstance> strategies, List<Resource> resources) {
        super(name, user, description, subtasks, sessions, deadline, priority, totalTime, topic, timeTable, strategies, resources);
        this.numUsers = numUsers;
        this.members.add(this.getUser());
        this.calendar = new TaskCalendar();
        this.getCalendar().setGroup(this);

    }


    public int getNumUsers() {
        return numUsers;
    }
    public int getActualMembers() {
        return actualMembers;
    }
    public void setActualMembers(int actualMembers) {
        this.actualMembers = actualMembers;
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
    public void setTakenSubtasks(List<TakenSubtask> takenSubtasks) {
        this.takenSubtasks = takenSubtasks;
    }
    public List<Subtask> getAvailableSubtasks() {
        return this.getSubtasks().stream()
                .filter(subtask -> takenSubtasks.stream()
                        .noneMatch(takenSubtask -> takenSubtask.getSubtask().equals(subtask)))
                .collect(Collectors.toList());
    }
    public boolean getIsOnFeed() {
        return isOnFeed;
    }
    public void setIsOnFeed(boolean onFeed) {
        isOnFeed = onFeed;
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
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
            this.getCalendar().removeSessions(member);
        }
        this.setState(TaskState.FREEZED);
        this.setIsInProgress(false);
    }


    @Override
    public void toCalendar() {
        if (this.getState()==TaskState.INPROGRESS ) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        if (!isComplete) {
            throw new UnsupportedOperationException("The group is not complete");
        }


        this.setIsOnFeed(false);
        this.setState(TaskState.INPROGRESS);
        for (User member : this.getMembers()) {
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to the user: " + member));

            Subtask subtask = takenSubtask.getSubtask();
            this.getCalendar().addSessions(member, subtask);
            member.getCalendar().addSubtaskSessionsForGroups(subtask);

            this.setIsInProgress(true);
            this.setState(TaskState.INPROGRESS);
        }
    }



    @Override
    public void deleteTask() {
        for (User member : this.getMembers()) {
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));
            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
            this.getCalendar().removeSessions(member);
        }
        this.setIsInProgress(false);
        this.setIsOnFeed(false);
    }

    public void toFeed(){
        if(this.getState() == TaskState.FINISHED || this.getState() == TaskState.INPROGRESS) {
            throw new UnsupportedOperationException("It can't go on feed");
        }
        if(this.getIsOnFeed()){
            throw new UnsupportedOperationException("It is already on feed");
        }
        this.setIsOnFeed(true);
        this.setDateOnFeed(LocalDateTime.now());
    }



    @Override
    public void modifyTask() {
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to freezed");
        }
        this.setState(TaskState.FREEZED);
        this.setIsInProgress(false);
        this.setIsOnFeed(false);

        for (User member : this.getMembers()) {
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(ts -> ts.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No subtask assigned to member"));

            Subtask subtaskInstance = takenSubtask.getSubtask();

            Subtask correctSubtask = this.getSubtasks().stream()
                    .filter(subtask -> subtask.equals(subtaskInstance))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No matching subtask found in task"));

            takenSubtask.setSubtask(correctSubtask);
        }
    }




    @Override
    public void completeTaskBySessions() {
        if(this.getState() != TaskState.INPROGRESS) {
            throw new IllegalStateException("It must be in progress");
        }
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
        this.setIsInProgress(false);
        setState(TaskState.FINISHED);
        this.setIsOnFeed(false);
        this.setPercentageOfCompletion(100);
    }


    @Override
    public void forcedCompletion() {
        if(this.getState()!=TaskState.INPROGRESS){
            throw new IllegalStateException("It is not in INPROGRESS state.");
        }
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
        this.setIsOnFeed(false);
        this.setPercentageOfCompletion(100);

        this.setState(TaskState.FINISHED);
        for (Session session: this.getSessions()){
            if(session.getState() != SessionState.COMPLETED){
                session.setState(SessionState.COMPLETED);
            }
        }
    }

    public void completeSubtaskSession( Session session) {
        if (session.getState() != SessionState.PROGRAMMED) {
            throw new IllegalStateException("Only sessions programmed can be completed.");
        }
        boolean found = false;
        for(Session taskSession: this.getSessions()) {
            if(taskSession.equals(session)){
                this.completeSession(taskSession);
                found = true;
                break;
            }
        }
        if(!found){
            throw new EntityNotFoundException("No such session in task");
        }
    }

    public void joinGroup(@NotNull User user, @NotNull Subtask subtask) {

        if(this.getState() == TaskState.FINISHED){
            throw new IllegalStateException("The group is already finished.");
        }

        boolean isSubtaskTaken = takenSubtasks.stream()
                .anyMatch(takenSubtask -> takenSubtask.getSubtask().equals(subtask));

        if(!this.getIsOnFeed()){
            throw new IllegalArgumentException("the task must be on feed for joining");
        }
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

        if(actualMembers == numUsers){
            isComplete = true;
        }
        if (this.getSubtasks().size() == takenSubtasks.size()) {
            isComplete = true;
        }
    }



    public void leaveGroupTask(User user) {
        if(this.getState() == TaskState.FINISHED){
            throw new IllegalStateException("The group is already finished.");
        }
        if (!members.contains(user)) {
            throw new IllegalArgumentException("the user is not part of the group");
        }
        actualMembers--;
        this.getCalendar().removeSessions(user);
        user.getTasks().remove(this);
        this.setState(TaskState.FREEZED);
        this.setIsInProgress(false);
        this.setIsComplete(false);

        TakenSubtask takenLeftSubtask = takenSubtasks.stream()
                .filter(t -> t.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("TakenSubtask not found for the user"));
        this.getTakenSubtasks().remove(takenLeftSubtask);
        user.getCalendar().removeSubtaskSessionsForGroups(takenLeftSubtask.getSubtask());
        this.getCalendar().removeSessions(user);
        this.getMembers().remove(user);
        for (User member : this.getMembers()) {
            // Ottieni il subtask assegnato a questo membro
            TakenSubtask takenSubtask = takenSubtasks.stream()
                    .filter(t -> t.getUser().equals(member))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Subtask not found for member"));
            Subtask subtaskOfCompetence = takenSubtask.getSubtask();
            this.getCalendar().removeSessions(member);
            member.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence);
        }
        this.setIsOnFeed(true);
    }




    @Override
    public void skipSession(Session session) {
        if(!this.getSessions().contains(session)){
            throw new EntityNotFoundException("The session does not exist.");
        }
        session.setState(SessionState.SKIPPED);
        Subtask incriminatedSubtask = null;
        boolean sessionFound = false;
        for (Subtask subtask : this.getSubtasks()) {
            for (Session subSession : subtask.getSessions()) {
                if (subSession.equals(session)) {
                    incriminatedSubtask = subtask;
                    subSession.setState(SessionState.SKIPPED);
                    sessionFound = true;
                    break;
                }
            }
        }
        if (!sessionFound) {
            throw new IllegalStateException("Session not found in any subtask. CLASS TASK skipSession");
        }

        boolean shouldAddAtEnd = getStrategies().stream()
                .anyMatch(strategyInstance ->
                        strategyInstance.getStrategy() == DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING
                                && !strategyInstance.getStrategy().requiresTot()
                                && !strategyInstance.getStrategy().requiresMaxConsecSkipped()
                );

        Subtask finalIncriminatedSubtask = incriminatedSubtask;
        TakenSubtask foundTakenSubtask = takenSubtasks.stream()
                .filter(ts -> ts.getSubtask().equals(finalIncriminatedSubtask))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No TakenSubtask found for the given Subtask"));
        if (shouldAddAtEnd) {
            LocalDateTime newStartDate = calculateNewSessionDate(session);
            LocalDateTime newEndDate = newStartDate.plusMinutes(session.getDurationMinutes());


            if (this.getDeadline() != null && newEndDate.isAfter(this.getDeadline())) {

                foundTakenSubtask.getUser().getCalendar().getSessions().stream()
                        .filter(s -> s.equals(session))
                        .forEach(s -> s.setState(SessionState.SKIPPED));
            } else {

                this.getSessions().remove(session);
                incriminatedSubtask.getSessions().remove(session);
                foundTakenSubtask.getUser().getCalendar().getSessions().remove(session);
                this.getCalendar().getUserSessions().removeIf(us -> us.getSession().equals(session));

                Session newSession = new Session(newStartDate, newEndDate);
                newSession.setState(SessionState.PROGRAMMED);

                this.getSessions().add(newSession);
                incriminatedSubtask.getSessions().add(newSession);
                foundTakenSubtask.getUser().getCalendar().getSessions().add(newSession);
                UserSession newUserSession = new UserSession(foundTakenSubtask.getUser(),newSession);
                this.getCalendar().getUserSessions().add(newUserSession);

                this.getSessions().sort(Comparator.comparing(Session::getStartDate));
            }
        }

        this.setSkippedSessions(this.getSkippedSessions()+1);
        this.setConsecutiveSkippedSessions(this.getConsecutiveSkippedSessions()+1);

        boolean limitExceeded = getStrategies().stream()
                .filter(strategyInstance -> strategyInstance.getStrategy().requiresTot()) // Filtra strategie che richiedono TOT
                .map(StrategyInstance::getTot)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .map(max -> this.getSkippedSessions() > max)
                .orElse(false);

        limitExceeded = limitExceeded || getStrategies().stream()
                .filter(strategyInstance -> strategyInstance.getStrategy().requiresMaxConsecSkipped())
                .map(StrategyInstance::getMaxConsecSkipped)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .map(max -> this.getConsecutiveSkippedSessions() > max)
                .orElse(false);

        if (limitExceeded) {
            handleLimitExceeded();
        }
    }

    private LocalDateTime calculateNewSessionDate(Session skippedSession) {

        LocalDateTime lastSessionEnd = getSessions().stream()
                .map(Session::getEndDate)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        // Recupera la fascia d'inizio e di fine del timetable (modifica in base alle tue esigenze)
        LocalTime timetableStart = getTimetableStart();
        LocalTime timetableEnd = getTimetableEnd();

        // Iniziamo due giorni dopo la fine dell'ultima sessione, all'orario d'inizio del timetable
        LocalDateTime candidateDate = lastSessionEnd.plusDays(2)
                .withHour(timetableStart.getHour())
                .withMinute(timetableStart.getMinute())
                .withSecond(0)
                .withNano(0);

        // Trova uno slot valido: aumenta di 30 minuti fino a quando non trovi un orario compatibile
        while (!isValidSessionTime(candidateDate, skippedSession.getDurationMinutes())) {
            candidateDate = candidateDate.plusMinutes(30);
            // Se l'orario corrente supera il limite del timetable, passa al giorno successivo
            if (candidateDate.toLocalTime().isAfter(timetableEnd)) {
                candidateDate = candidateDate.plusDays(1)
                        .withHour(timetableStart.getHour())
                        .withMinute(timetableStart.getMinute())
                        .withSecond(0)
                        .withNano(0);
            }
        }
        return candidateDate;
    }

    // Esempi di metodi helper per ottenere l'orario d'inizio e fine in base al timetable del task
    private LocalTime getTimetableStart() {
        return switch (this.getTimetable()) {
            case MORNING, MORNING_AFTERNOON, MORNING_EVENING -> LocalTime.of(6, 0);
            case AFTERNOON, AFTERNOON_EVENING -> LocalTime.of(12, 0);
            case EVENING -> LocalTime.of(18, 0);
            case NIGHT, NIGHT_AFTERNOON, NIGHT_MORNING, ALL_DAY -> LocalTime.of(0, 0);

        };
    }

    private LocalTime getTimetableEnd() {
        return switch (this.getTimetable()) {
            case MORNING, NIGHT_MORNING -> LocalTime.of(12, 0);
            case AFTERNOON, MORNING_AFTERNOON, NIGHT_AFTERNOON -> LocalTime.of(18, 0);
            case EVENING, AFTERNOON_EVENING, MORNING_EVENING, ALL_DAY -> LocalTime.of(23, 59);
            case NIGHT -> LocalTime.of(6, 0);
        };
    }


    private boolean isValidSessionTime(LocalDateTime startDate, int durationMinutes) {
        LocalDateTime endDate = startDate.plusMinutes(durationMinutes);

        // Verifica se la sessione rientra nella timetable del task
        boolean isWithinTimetable = isWithinTimeSlot(startDate.toLocalTime(), endDate.toLocalTime());

        // Verifica se non ci sono conflitti con sessioni esistenti
        boolean hasNoConflict = this.getSessions().stream().noneMatch(session ->
                startDate.isBefore(session.getEndDate()) && endDate.isAfter(session.getStartDate()));

        return isWithinTimetable && hasNoConflict;
    }

    private boolean isWithinTimeSlot(LocalTime startTime, LocalTime endTime) {
        return switch (this.getTimetable()) {
            case MORNING -> !startTime.isBefore(LocalTime.of(6, 0)) && endTime.isBefore(LocalTime.of(12, 0));
            case AFTERNOON -> !startTime.isBefore(LocalTime.of(12, 0)) && endTime.isBefore(LocalTime.of(18, 0));
            case EVENING -> !startTime.isBefore(LocalTime.of(18, 0)) && endTime.isBefore(LocalTime.of(23, 59));
            case NIGHT -> !startTime.isBefore(LocalTime.of(0, 0)) && endTime.isBefore(LocalTime.of(6, 0));
            case MORNING_AFTERNOON ->
                    (!startTime.isBefore(LocalTime.of(6, 0)) && endTime.isBefore(LocalTime.of(18, 0)));
            case AFTERNOON_EVENING ->
                    (!startTime.isBefore(LocalTime.of(12, 0)) && endTime.isBefore(LocalTime.of(23, 59)));
            case NIGHT_AFTERNOON ->
                    (!startTime.isBefore(LocalTime.of(0, 0)) && endTime.isBefore(LocalTime.of(18, 0)));
            case MORNING_EVENING ->
                    (!startTime.isBefore(LocalTime.of(6, 0)) && endTime.isBefore(LocalTime.of(0, 0)));
            case NIGHT_MORNING ->
                    (!startTime.isBefore(LocalTime.of(0, 0)) && endTime.isBefore(LocalTime.of(12, 0)));
            case ALL_DAY -> true;  // Copre l'intera giornata
        };
    }


    @Override
    public void removeAndFreezeTask(User user){
        if(user != this.getUser()){
            throw new UnsupportedOperationException("the user must be the admin");
        }
        takenSubtasks.forEach(ts -> ts.getUser().getCalendar().removeSubtaskSessionsForGroups(ts.getSubtask()));
        this.getCalendar().removeTaskSessions(this);
        this.setState(TaskState.FREEZED);
        this.setIsInProgress(false);
    }





    public void removeMember(User admin, User member, boolean substitute) {

        if(this.getState() == TaskState.FINISHED || this.getState() == TaskState.TODO){
            throw new IllegalStateException("you can't remove members now");
        }
        if(this.getUser().equals(member)){
            throw new IllegalArgumentException("Only admin users can remove members");
        }
        if (!members.contains(member) || !members.contains(admin)) {
            throw new IllegalArgumentException("The member is not part of the group");
        }
        if (substitute) {
            this.setIsInProgress(false);
            this.setState(TaskState.FREEZED);
            this.setIsComplete(false);
            this.setIsOnFeed(true);

            TakenSubtask takenSubtask = getTakenSubtaskForMember(member);
            getAvailableSubtasks().add(takenSubtask.getSubtask());
            takenSubtasks.remove(takenSubtask);

            this.getCalendar().removeSessions(member);

            member.getCalendar().removeSubtaskSessionsForGroups(takenSubtask.getSubtask());
            this.getMembers().remove(member);

            for (User user : this.getMembers()) {
                this.getCalendar().removeSessions(user);
                TakenSubtask subtaskOfCompetence = getTakenSubtaskForMember(user);
                user.getCalendar().removeSubtaskSessionsForGroups(subtaskOfCompetence.getSubtask());
            }
            actualMembers--;

        }
        else {

        TakenSubtask takenSubtask = getTakenSubtaskForMember(member);
        Subtask subtaskToRemove = takenSubtask.getSubtask();
        member.getCalendar().removeSubtaskSessionsForGroups(subtaskToRemove);

        TaskCalendar groupCalendar = this.getCalendar();
        if (groupCalendar != null) {
            for (Session subSession : subtaskToRemove.getSessions()) {
                groupCalendar.getUserSessions().removeIf(userSession ->
                        userSession.getUser().equals(member) && userSession.getSession().equals(subSession)
                );
            }
        }

        this.getMembers().remove(member);

        int subtaskTimeHours = 0;
        for (Session subSession : subtaskToRemove.getSessions()) {
            int hours = subSession.getDurationMinutes() / 60;
            subtaskTimeHours += hours;

            this.getSessions().remove(subSession);
        }
        int newTotalTime = this.getTotalTime() - subtaskTimeHours;
        if (newTotalTime < 0) {
            newTotalTime = 0;
        }
        this.setTotalTime(newTotalTime);
        this.getSubtasks().remove(subtaskToRemove);
        takenSubtasks.remove(takenSubtask);
        this.setIsOnFeed(false);
        actualMembers--;
        numUsers--;
        this.setIsComplete(true);
        }
    }

    private TakenSubtask getTakenSubtaskForMember(User member) {

        for (TakenSubtask ts : takenSubtasks) {
            if (ts.getUser().equals(member)) {
                return ts;
            }
        }
        throw new IllegalArgumentException("The user does not have a subtask assigned.");
    }



}



