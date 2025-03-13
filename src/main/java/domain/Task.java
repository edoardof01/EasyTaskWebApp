package domain;


import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    private String name;
    @Column(length = 1000)
    private String description;
    private LocalDateTime deadline;
    private int percentageOfCompletion;
    private int priority;
    private int totalTime;
    private int complexity;
    private int skippedSessions = 0;
    private int consecutiveSkippedSessions = 0;
    private boolean isInProgress = false;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Timetable timetable;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Subtask> subtasks = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Resource> resources = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    private TaskState state;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<StrategyInstance> strategies = new ArrayList<>();

    public Task() {}

    public Task(String name,@NotNull User user, String description,@Nullable List<Subtask> subtasks, List<Session> sessions,
                @Nullable LocalDateTime deadline, int priority, int totalTime, Topic topic,
                Timetable timetable, List<StrategyInstance> strategies, List<Resource> resources) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.percentageOfCompletion = 0;
        this.priority = priority;
        this.totalTime = totalTime;
        this.topic = topic;
        this.state = TaskState.TODO;
        this.timetable = timetable;
        this.user = user;
        this.resources = resources;
        this.subtasks = subtasks;
        this.strategies = strategies;
        complexity = calculateComplexity();
        this.sessions = sessions;
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public LocalDateTime getDeadline() {
        return deadline;
    }
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
    public int getPercentageOfCompletion() {
        return percentageOfCompletion;
    }
    public void setPercentageOfCompletion(int percentageOfCompletion) {
        this.percentageOfCompletion = percentageOfCompletion;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public int getTotalTime() {
        return totalTime;
    }
    public Topic getTopic() {
        return topic;
    }
    public void setTopic(Topic topic) {
        this.topic = topic;
    }
    public TaskState getState() {
        return state;
    }
    public void setState(TaskState state) {
        this.state = state;
    }
    public Timetable getTimetable() {
        return timetable;
    }
    public List<StrategyInstance> getStrategies() {
        return strategies;
    }
    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public List<Subtask> getSubtasks() {
        return subtasks;
    }
    public void setSubtasks(List<Subtask> subtasks){
        this.subtasks = subtasks;
    }
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
    public int getComplexity() {
        return complexity;
    }
    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }
    public List<Session> getSessions() {
        return sessions;
    }
    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
    public List<Resource> getResources() {
        return resources;
    }
    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
    public boolean getIsInProgress() {
        return isInProgress;
    }
    public int getConsecutiveSkippedSessions(){
        return consecutiveSkippedSessions;
    }
    public void setConsecutiveSkippedSessions(int consecutiveSkippedSessions) {
        this.consecutiveSkippedSessions = consecutiveSkippedSessions;
    }
    public int getSkippedSessions(){return skippedSessions;}
    public void setSkippedSessions(int skippedSessions){
        this.skippedSessions = skippedSessions;
    }



    public void completeSession(Session session) {
        if(this.getState() != TaskState.INPROGRESS){
            throw new IllegalStateException("The task must be in calendar");
        }
        if(!sessions.contains(session)){
            throw new EntityNotFoundException("The session does not exist.");
        }
        if (session.getState() != SessionState.PROGRAMMED) {
            throw new IllegalStateException("Only sessions programmed can be completed.");
        }
        if (!doExistNextSession(session)) {
            Session lastSession = this.getSessions().stream()
                    .max(Comparator.comparing(Session::getEndDate))
                    .orElseThrow(() -> new IllegalStateException("No sessions available for this task"));


            boolean control = this.getSessions().stream()
                    .filter(s -> !s.equals(lastSession))
                    .anyMatch(s -> s.getState() != SessionState.COMPLETED);

            if(!control) {
                completeTaskBySessions();
            }
            if(control){
                forcedCompletion();}
        } else {
            session.setState(SessionState.COMPLETED);
            resetConsecutiveSkippedSessions();

            long completedCounter = sessions.stream()
                    .filter(s -> s.getState() == SessionState.COMPLETED)
                    .count();

            double percentage = ((double) completedCounter / sessions.size()) * 100;
            int roundedPercentage = (percentage % 1 < 0.5) ? (int) Math.floor(percentage) : (int) Math.ceil(percentage);
            this.setPercentageOfCompletion(roundedPercentage);
        }
    }

    /*public boolean doExistNextSession(Session session) {
        return sessions.stream()
                .filter(s -> !s.getStartDate().isBefore(session.getEndDate()))
                .min(Comparator.comparing(Session::getStartDate))
                .isPresent();
    }*/

    public boolean doExistNextSession(Session current) {
        return this.getSessions().stream()
                .anyMatch(s -> s.getStartDate().isAfter(current.getEndDate()));
    }




    public int calculateComplexity() {
        int subtaskScore;
        if (subtasks == null || subtasks.isEmpty()) {
            // Se non ci sono subtasks, ad esempio usa 0 o un altro valore (o considera soltanto le risorse)
            subtaskScore = 0;
        } else if (subtasks.size() <= 3) {
            subtaskScore = 1;
        } else if (subtasks.size() <= 5) {
            subtaskScore = 2;
        } else if (subtasks.size() <= 10) {
            subtaskScore = 3;
        } else if (subtasks.size() <= 20) {
            subtaskScore = 4;
        } else {
            subtaskScore = 5;
        }
        int resourceScore = calculateResourceScore();
        if (subtasks == null || subtasks.isEmpty()) {
            return resourceScore;
        } else {
            return (subtaskScore + resourceScore) / 2;
        }
    }


    private int calculateResourceScore() {
        int totalScore = resources.stream()
                .mapToInt(resource -> {
                    if (resource.getType() == ResourceType.MONEY) {
                        resource.setValue(resource.calculateValueFromMoney());
                        return resource.calculateValueFromMoney();
                    } else {
                        return resource.getValue();
                    }
                })
                .sum();
        if (totalScore <= 10) return 1;
        else if (totalScore <= 20) return 2;
        else if (totalScore <= 30) return 3;
        else if (totalScore <= 40) return 4;
        else return 5;
    }


    // Metodo per impostare le strategie e validare la selezione
    public void setStrategies(List<StrategyInstance> strategies) {
        validateStrategySelection(strategies);
        this.strategies = strategies;
    }
    private void validateStrategySelection(List<StrategyInstance> strategies) {
        // Controlla che tutte le strategie che richiedono TOT abbiano un valore specificato
        boolean requiresTotStrategy = strategies.stream()
                .anyMatch(strategy -> strategy.getStrategy().requiresTot());
        boolean allHaveTot = strategies.stream()
                .allMatch(strategy -> !strategy.getStrategy().requiresTot() || strategy.getTot() != null);

        if (requiresTotStrategy && !allHaveTot) {
            throw new IllegalArgumentException("Tutte le strategie che richiedono TOT devono avere un valore specificato.");
        }

        // Controlla che tutte le strategie che richiedono max consecutive skipped abbiano un valore specificato
        boolean requiresMaxConsecSkippedStrategy = strategies.stream()
                .anyMatch(strategy -> strategy.getStrategy().requiresMaxConsecSkipped());
        boolean allHaveMaxConsecSkipped = strategies.stream()
                .allMatch(strategy -> !strategy.getStrategy().requiresMaxConsecSkipped() || strategy.getMaxConsecSkipped() != null);

        if (requiresMaxConsecSkippedStrategy && !allHaveMaxConsecSkipped) {
            throw new IllegalArgumentException("Tutte le strategie che richiedono un numero massimo di sessioni consecutive devono avere un valore specificato.");
        }

        // Controlla che la combinazione di strategie sia valida
        boolean hasFreezeTaskAfterTotSkippedSessions = strategies.stream()
                .anyMatch(strategy -> strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS);
        boolean hasFreezeTaskAfterConsecutiveSkippedSessions = strategies.stream()
                .anyMatch(strategy -> strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS);

        if (hasFreezeTaskAfterTotSkippedSessions && hasFreezeTaskAfterConsecutiveSkippedSessions && strategies.size() > 3) {
            throw new IllegalArgumentException("La combinazione di strategie non è valida.");
        }
    }


    public void commonToCalendarLogic(User user) {
        if (this.getIsInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        this.setIsInProgress(true);
        this.setState(TaskState.INPROGRESS);
        user.getCalendar().addSessions(this.getSessions());
    }

    public void commonModifyLogic(User user){
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to freezed");
        }
        if (this.getIsInProgress() && this.getState() == TaskState.INPROGRESS) {
            user.getCalendar().removeSessions(this);
            this.setState(TaskState.FREEZED);
        }
        this.isInProgress = false;
    }

    public void commonCompleteBySessionsLogic(User user) {
        if(!user.equals(this.getUser())) {
            throw new IllegalArgumentException("user is not the owner");
        }
        if(this.state != TaskState.INPROGRESS ){
            throw new IllegalStateException("It can't be finished");
        }
        Session lastSession = this.getSessions().stream()
                .max(Comparator.comparing(Session::getEndDate))
                .orElseThrow(() -> new IllegalStateException("No sessions available for this task"));

        for (Session session : this.getSessions()) {
            if (!session.equals(lastSession) && session.getState() != SessionState.COMPLETED) {
                throw new IllegalStateException("All sessions except the last one must be completed.");
            }
        }

        if (lastSession.getState() != SessionState.PROGRAMMED) {
            throw new IllegalStateException("The last session must be in PROGRAMMED state.");
        }

        lastSession.setState(SessionState.COMPLETED);

        for (Subtask subtask : this.getSubtasks()) {
            for (Session session : subtask.getSessions()) {
                if(session.getState()!=SessionState.COMPLETED) {
                    session.setState(SessionState.COMPLETED);
                }
            }
        }

        for (Session session : this.getSessions()) {
            if (session.getState() != SessionState.COMPLETED) {
                throw new UnsupportedOperationException("The task can't be completed normally");
            }
        }
        this.percentageOfCompletion = 100;
        this.setState(TaskState.FINISHED);
        this.isInProgress = false;
    }


    public void commonForcedCompletionLogic(User user){
        if(!user.equals(this.getUser())) {
            throw new IllegalArgumentException("user is not the owner");
        }
        if(this.getState() != TaskState.INPROGRESS){
            throw new IllegalStateException("It can't be finished");
        }
        for(Session session : this.getSessions()){
            if(session.getState() != SessionState.COMPLETED){
                session.setState(SessionState.COMPLETED);
            }
        }
        for(Subtask subtask : this.getSubtasks()){
            for(Session session : subtask.getSessions()){
                if(session.getState() != SessionState.COMPLETED){
                    session.setState(SessionState.COMPLETED);
                }
            }
        }
        this.setState(TaskState.FINISHED);
        this.percentageOfCompletion = 100;


    }

    // METODI PER LE SESSIONI SALTATE
    public void skipSession(Session session) {
        if(!sessions.contains(session)){
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
        if (!sessionFound && !subtasks.isEmpty()) {
            throw new IllegalStateException("Session not found in any subtask. CLASS TASK skipSession");
        }
        boolean shouldAddAtEnd = strategies.stream()
                .anyMatch(strategyInstance ->
                        strategyInstance.getStrategy() == DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING
                                && !strategyInstance.getStrategy().requiresTot()
                                && !strategyInstance.getStrategy().requiresMaxConsecSkipped()
                );

        if (shouldAddAtEnd) {
            LocalDateTime newStartDate = calculateNewSessionDate(session);
            LocalDateTime newEndDate = newStartDate.plusMinutes(session.getDurationMinutes());

            if (this.getDeadline() != null && (newEndDate.isAfter(this.getDeadline()))) {
                // Rimane SKIPPED definitivamente, non faccio nulla
                System.out.println("La sessione supera la deadline, resta SKIPPED");
            } else {
                sessions.remove(session); // rimuovi dall’elenco
                if(incriminatedSubtask != null) {
                    incriminatedSubtask.getSessions().remove(session);
                }

                session.setStartDate(newStartDate);
                session.setEndDate(newEndDate);
                if(incriminatedSubtask != null) {
                    incriminatedSubtask.getSessions().add(session);
                }
                sessions.add(session);
                session.setState(SessionState.PROGRAMMED);
                sessions.sort(Comparator.comparing(Session::getStartDate));
            }
        }
        skippedSessions++;
        consecutiveSkippedSessions++;

        boolean limitExceeded = strategies.stream()
                .filter(strategyInstance -> strategyInstance.getStrategy().requiresTot()) // Filtra strategie che richiedono TOT
                .map(StrategyInstance::getTot)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .map(max -> skippedSessions > max)
                .orElse(false);

        limitExceeded = limitExceeded || strategies.stream()
                .filter(strategyInstance -> strategyInstance.getStrategy().requiresMaxConsecSkipped())
                .map(StrategyInstance::getMaxConsecSkipped)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .map(max -> consecutiveSkippedSessions > max)
                .orElse(false);

        if (limitExceeded) {
            handleLimitExceeded();
        }
    }

    private LocalDateTime calculateNewSessionDate(Session skippedSession) {
        LocalDateTime lastSessionEnd = sessions.stream()
                .map(Session::getEndDate)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        LocalTime timetableStart = getTimetableStart();
        LocalTime timetableEnd = getTimetableEnd();

        LocalDateTime candidateDate = lastSessionEnd.plusDays(2)
                .withHour(timetableStart.getHour())
                .withMinute(timetableStart.getMinute())
                .withSecond(0)
                .withNano(0);

        while (!isValidSessionTime(candidateDate, skippedSession.getDurationMinutes())) {
            candidateDate = candidateDate.plusMinutes(30);

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

    private LocalTime getTimetableStart() {
        return switch (timetable) {
            case MORNING, MORNING_AFTERNOON, MORNING_EVENING -> LocalTime.of(6, 0);
            case AFTERNOON, AFTERNOON_EVENING -> LocalTime.of(12, 0);
            case EVENING -> LocalTime.of(18, 0);
            case NIGHT, NIGHT_AFTERNOON, NIGHT_MORNING, ALL_DAY -> LocalTime.of(0, 0);

        };
    }

    private LocalTime getTimetableEnd() {
        return switch (timetable) {
            case MORNING, NIGHT_MORNING -> LocalTime.of(12, 0);
            case AFTERNOON, MORNING_AFTERNOON, NIGHT_AFTERNOON -> LocalTime.of(18, 0);
            case EVENING, AFTERNOON_EVENING, MORNING_EVENING, ALL_DAY -> LocalTime.of(23, 59);
            case NIGHT -> LocalTime.of(6, 0);
        };
    }


    private boolean isValidSessionTime(LocalDateTime startDate, int durationMinutes) {
        LocalDateTime endDate = startDate.plusMinutes(durationMinutes);

        boolean isWithinTimetable = isWithinTimeSlot(startDate.toLocalTime(), endDate.toLocalTime());

        boolean hasNoConflict = sessions.stream().noneMatch(session ->
                startDate.isBefore(session.getEndDate()) && endDate.isAfter(session.getStartDate()));

        return isWithinTimetable && hasNoConflict;
    }

    private boolean isWithinTimeSlot(LocalTime startTime, LocalTime endTime) {
        return switch (timetable) {
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





    public void resetConsecutiveSkippedSessions() {
        consecutiveSkippedSessions = 0;
    }



    public void autoSkipIfNotCompleted(Session session) {
        boolean found = false;
        for (Session oldSession : sessions) {
            if (session.equals(oldSession)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("session not found in task. CLASS TASK autoSkip...");
        }
        Session nextSession = findNextSession(session);
        if (nextSession != null && LocalDateTime.now().isAfter(nextSession.getStartDate())) {
            skipSession(session);
        }
        if(nextSession == null && LocalDateTime.now().isAfter(session.getEndDate().plusDays(1))){
            skipSession(session);
        }
    }

    private Session findNextSession(Session currentSession) {
        return sessions.stream()
                .filter(s -> !s.getStartDate().isBefore(currentSession.getEndDate()))
                .min(Comparator.comparing(Session::getStartDate))
                .orElse(null);
    }

    public void removeAndFreezeTask(User user) {
        user.getCalendar().removeSessions(this);
        this.setState(TaskState.FREEZED);
        this.isInProgress = false;
    }

    public void setIsInProgress(boolean isIt){
        this.isInProgress = isIt;
    }

    public abstract void toCalendar();
    public abstract void deleteTask();
    public abstract void modifyTask();
    public abstract void completeTaskBySessions();
    public abstract void forcedCompletion();
    public abstract void handleLimitExceeded();

    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(this.getName(), task.getName()) && this.getTopic() == task.getTopic() && this.getTotalTime() == task.getTotalTime();
    }

}






