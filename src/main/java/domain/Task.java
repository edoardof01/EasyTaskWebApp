package domain;


import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.*;

import static domain.SubfolderType.*;



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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Timetable.class)
    @Enumerated(EnumType.STRING)
    private Set<Timetable> timetable;

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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
                @Nullable LocalDateTime deadline, int percentageOfCompletion, int priority, int totalTime, Topic topic,
               Set<Timetable> timetable, List<StrategyInstance> strategies, List<Resource> resources) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.percentageOfCompletion = percentageOfCompletion;
        this.priority = priority;
        this.totalTime = totalTime;
        this.topic = topic;
        this.state = TaskState.TODO;
        this.timetable = timetable;
        this.user = user;
        this.resources = resources;
        this.subtasks = subtasks;
        setStrategies(strategies);
        complexity = calculateComplexity();
        this.sessions = sessions;
    }


    public Long getId() {
        return id;
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
    public Set<Timetable> getTimetable() {
        return timetable;
    }
    public List<StrategyInstance> getStrategies() {
        return strategies;
    }
    public void setTimetable(Set<Timetable> timetable) {
        this.timetable = timetable;
    }
    public User getUser() {
        return user;
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
    public boolean isInProgress() {
        return isInProgress;
    }


    public void completeSession(Session session) {
        if (session.getState() != SessionState.PROGRAMMED) {
            throw new IllegalStateException("Only sessions programmed can be completed.");
        }
        session.setState(SessionState.COMPLETED);
        resetConsecutiveSkippedSessions();
        int completedCounter = 0;
        for(Session taskSession : sessions) {
            if(taskSession.getState() != SessionState.COMPLETED) {
                completedCounter += 1;
            }
        }
        percentageOfCompletion = (completedCounter / sessions.size())*100;
        this.setPercentageOfCompletion(percentageOfCompletion);
    }

    public int calculateComplexity() {
        int subtaskScore;
        if (subtasks.size() <= 3) subtaskScore = 1;
        else if (subtasks.size() <= 5) subtaskScore = 2;
        else if (subtasks.size() <= 10) subtaskScore = 3;
        else if (subtasks.size() <= 20) subtaskScore = 4;
        else subtaskScore = 5;

        int resourceScore = calculateResourceScore();
        return (subtaskScore + resourceScore) / 2;
    }

    public int calculateResourceScore() {
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

        if (hasFreezeTaskAfterTotSkippedSessions && hasFreezeTaskAfterConsecutiveSkippedSessions && strategies.size() > 2) {
            throw new IllegalArgumentException("La combinazione di strategie non è valida.");
        }
    }


    protected void commonToCalendarLogic(User user) {
        if (this.isInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to inProgress");
        }
        this.updateIsInProgress();
        user.getCalendar().addSessions(this.getSessions());
        List<Folder> folders = user.getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(this)) {
                    subfolder.getTasks().remove(this);
                    taskRemoved = true;
                }
                if (subfolder.getType() == INPROGRESS) {
                    subfolder.getTasks().add(this);
                }
            }
        }
    }
    public void commonModifyLogic(User user){
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to freezed");
        }
        this.setState(TaskState.FREEZED);
        this.isInProgress = false;

        user.getCalendar().removeSessions(this);

        List<Folder> folders = user.getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(this)) {
                    subfolder.getTasks().remove(this);
                    taskRemoved = true;
                }
                if (subfolder.getType() == FREEZED) {
                    subfolder.getTasks().add(this);
                }
            }
        }

    }

    public void commonCompleteBySessionsLogic(User user) {
        // Ottieni la sessione con l'ultima endDate
        Session lastSession = this.getSessions().stream()
                .max((s1, s2) -> s1.getEndDate().compareTo(s2.getEndDate()))
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

        // Completa l'ultima sessione
        lastSession.setState(SessionState.COMPLETED);

        for (Subtask subtask : this.getSubtasks()) {
            boolean found = false; // Variabile per tracciare se troviamo la sessione finale nel subtask
            for (Session session : subtask.getSessions()) {
                if(session.getState()!=SessionState.COMPLETED) {
                    session.setState(SessionState.COMPLETED);
                }
            }
        }

        // Verifica che tutte le sessioni siano ora COMPLETED
        for (Session session : this.getSessions()) {
            if (session.getState() != SessionState.COMPLETED) {
                throw new UnsupportedOperationException("The task can't be completed normally");
            }
        }
        // Rimuovi le sessioni dal calendario
        user.getCalendar().removeSessions(this);
        this.percentageOfCompletion = 100;
        this.setState(TaskState.FINISHED);

        // Aggiorna le cartelle
        List<Folder> folders = user.getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(this)) {
                    subfolder.getTasks().remove(this);
                    taskRemoved = true;
                }
                if (subfolder.getType() == FINISHED) {
                    subfolder.getTasks().add(this);
                }
            }
        }
    }


    public void commonForcedCompletionLogic(User user){
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
        user.getCalendar().removeSessions(this);
        List<Folder> folders = user.getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(this)) {
                    subfolder.getTasks().remove(this);
                    taskRemoved = true;
                }
                if (subfolder.getType() == FINISHED) {
                    subfolder.getTasks().add(this);
                }
            }
        }
    }

    // METODI PER LE SESSIONI SALTATE
    public void skipSession(Session session) {

        // Cambia lo stato della sessione specifica
        session.setState(SessionState.SKIPPED);

        boolean sessionFound = false;
        for (Subtask subtask : this.getSubtasks()) {
            for (Session subSession : subtask.getSessions()) {
                if (subSession.equals(session)) {
                    subSession.setState(SessionState.SKIPPED);
                    sessionFound = true;
                    break;
                }
            }
        }
        if (!sessionFound) {
            throw new IllegalStateException("Session not found in any subtask. CLASS TASK skipSession");
        }

        skippedSessions++;
        consecutiveSkippedSessions++;

        boolean limitExceeded = strategies.stream()
                .filter(strategyInstance -> strategyInstance.getStrategy().requiresTot()) // Filtra strategie che richiedono TOT
                .map(StrategyInstance::getTot) // Ottieni il valore di TOT
                .filter(Objects::nonNull) // Evita valori null
                .max(Integer::compare) // Trova il valore massimo di TOT
                .map(max -> skippedSessions > max) // Verifica se il limite è stato superato
                .orElse(false);

        // Verifica e applica le strategie relative al numero di sessioni consecutive saltate
        limitExceeded = limitExceeded || strategies.stream()
                .filter(strategyInstance -> strategyInstance.getStrategy().requiresMaxConsecSkipped()) // Filtra strategie che richiedono max consecutive skipped
                .map(StrategyInstance::getMaxConsecSkipped) // Ottieni il valore di max consecutive skipped
                .filter(Objects::nonNull) // Evita valori null
                .max(Integer::compare) // Trova il valore massimo di max consecutive skipped
                .map(max -> consecutiveSkippedSessions > max) // Verifica se il limite è stato superato
                .orElse(false);

        // Gestisce il limite superato
        if (limitExceeded) {
            handleLimitExceeded();
        }
    }

    public void resetConsecutiveSkippedSessions() {
        consecutiveSkippedSessions = 0;
    }
    public abstract void handleLimitExceeded();

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

        // Trova la prossima sessione dello stesso task o subtask
        Session nextSession = findNextSession(session);

        // Verifica se la sessione corrente è non completata e se la prossima è già iniziata
        if (nextSession != null && LocalDateTime.now().isAfter(nextSession.getStartDate())) {
            // Chiama skipSession per marcare la sessione come SKIPPED
            skipSession(session);
        }
    }

    private Session findNextSession(Session currentSession) {
        return sessions.stream()
                .filter(s -> !s.getStartDate().isBefore(currentSession.getEndDate()))
                .min(Comparator.comparing(Session::getStartDate))
                .orElse(null);
    }



    protected void removeAndFreezeTask(User user, Task task) {
        // Rimuove le sessioni del task dal calendario
        user.getCalendar().removeSessions(task);

        // Imposta lo stato del task a FREEZED
        task.setState(TaskState.FREEZED);
        this.isInProgress = false;

        // Trova la subfolder corrente del task e lo rimuove
        List<Folder> folders = user.getFolders();
        boolean taskMoved = false;

        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (subfolder.getTasks().remove(task)) {
                    taskMoved = true;
                    break; // Una volta rimosso, possiamo interrompere il ciclo
                }
            }
            if (taskMoved) {
                break; // Una volta trovato il folder, possiamo interrompere il ciclo
            }
        }

        // Aggiunge il task alla subfolder FREEZED
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (subfolder.getType() == SubfolderType.FREEZED) {
                    subfolder.getTasks().add(task);
                    return; // Task aggiunto, fine del metodo
                }
            }
        }

        // Se non esiste una subfolder FREEZED, lancia un'eccezione
        throw new IllegalStateException("No FREEZED subfolder found to move the task.");
    }



    public abstract void toCalendar();

    protected void updateIsInProgress() {
        this.isInProgress = true;
        this.state = TaskState.INPROGRESS;
    }

    public abstract void deleteTask();
    public abstract void modifyTask();

    public abstract void completeTaskBySessions();

    public abstract void forcedCompletion();
}






