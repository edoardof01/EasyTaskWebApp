package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

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

    @Lob
    private String description;

    private LocalDateTime deadline;
    private int percentageOfCompletion;
    private int priority;
    private int totalTime;
    private int complexity;

    private int skippedSessions = 0;
    private int consecutiveSkippedSessions = 0;
    private boolean isInProgress = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Timetable.class)
    @Enumerated(EnumType.STRING)
    private Set<Timetable> timetable;

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Subtask> subtasks = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Resource> resources = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    private TaskState state;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DefaultStrategy> strategies = new HashSet<>();

    public Task() {}

    public Task(String name, User user, String description,
                @Nullable LocalDateTime deadline, int percentageOfCompletion, int priority, int totalTime, Topic topic,
                TaskState state, Set<Timetable> timetable, Set<DefaultStrategy> strategies, List<Resource> resources) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.percentageOfCompletion = percentageOfCompletion;
        this.priority = priority;
        this.totalTime = totalTime;
        this.topic = topic;
        this.state = state;
        this.timetable = timetable;
        this.user = user;
        this.resources = resources;
        setStrategies(strategies);
        complexity = calculateComplexity();

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
    public Set<DefaultStrategy> getStrategies() {
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
        int score = resources.stream().mapToInt(Resource::getValue).sum();
        if (score <= 10) return 1;
        else if (score <= 20) return 2;
        else if (score <= 30) return 3;
        else if (score <= 40) return 4;
        else return 5;
    }


    // Metodo per impostare le strategie e validare la selezione
    public void setStrategies(Set<DefaultStrategy> strategies) {
        validateStrategySelection(strategies);
        this.strategies = strategies;
    }
    private void validateStrategySelection(Set<DefaultStrategy> strategies) {
        boolean requiresTotStrategy = strategies.stream().anyMatch(DefaultStrategy::requiresTot);
        boolean allHaveTot = strategies.stream().allMatch(strategy -> !strategy.requiresTot() || strategy.hasTot());

        if (requiresTotStrategy && !allHaveTot) {
            throw new IllegalArgumentException("Tutte le strategie che richiedono TOT devono avere un valore specificato.");
        }

        // Controllo per garantire che solo le combinazioni valide siano consentite
        if (strategies.contains(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS) &&
                strategies.contains(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS) &&
                strategies.size() > 2) {
            throw new IllegalArgumentException("La combinazione di strategie non è valida.");
        }
    }

    protected void commonToCalendarLogic(User user) {
        if (this.isInProgress()) {
            throw new UnsupportedOperationException("It's already in calendar");
        }
        if (this.getState() == TaskState.FINISHED || this.getState() == TaskState.FREEZED) { // FORSE ANCHE LO STATO FREEZED VA BENE???
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
                if (subfolder.getType() == SubfolderType.INPROGRESS) {
                    subfolder.getTasks().add(this);
                }
            }
        }
    }
    public void commonModifyLogic(User user){
        if (this.getState() == TaskState.FINISHED) {
            throw new UnsupportedOperationException("It can't be brought to freezed");
        }
        if (this.getState() == TaskState.FREEZED) {
            throw new UnsupportedOperationException("It's already freezed");
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
    public void commonCompleteBySessionsLogic(User user){
        for (Session session : this.getSessions()) {
            if(session.getState() != SessionState.COMPLETED){
                throw new UnsupportedOperationException("the task can't be completed normally");
            }
        }
        user.getCalendar().removeSessions(this);
        this.percentageOfCompletion = 100;
        this.setState(TaskState.FINISHED);
        List<Folder> folders = user.getFolders();
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
    public void commonForcedCompletionLogic(User user){
        for(Session session : this.getSessions()){
            if(session.getState() != SessionState.COMPLETED){
                session.setState(SessionState.COMPLETED);
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
                if (subfolder.getType() == SubfolderType.FINISHED) {
                    subfolder.getTasks().add(this);
                }
            }
        }
    }

    // METODI PER LE SESSIONI SALTATE
    public void skipSession(Session session) {
        if (!sessions.contains(session)) {
            throw new IllegalArgumentException("Session not found in task.");
        }

        skippedSessions++;
        consecutiveSkippedSessions++;

        // Cambia lo stato della sessione specifica
        session.setState(SessionState.SKIPPED);

        // Verifica e applica le strategie relative al numero totale di sessioni saltate
        OptionalInt maxSkippedSessions = strategies.stream()
                .filter(DefaultStrategy::requiresTot)
                .mapToInt(DefaultStrategy::getTot)
                .max();

        maxSkippedSessions.ifPresent(max -> {
            if (skippedSessions > max) {
                handleLimitExceeded();
            }
        });

        // Verifica e applica le strategie relative al numero di sessioni saltate consecutivamente
        OptionalInt maxConsecSkippedSessions = strategies.stream()
                .filter(DefaultStrategy::requiresMaxConsecSkipped)
                .mapToInt(DefaultStrategy::getMaxConsecSkipped)
                .max();

        maxConsecSkippedSessions.ifPresent(max -> {
            if (consecutiveSkippedSessions > max) {
                handleLimitExceeded();
            }
        });
    }
    public void resetConsecutiveSkippedSessions() {
        consecutiveSkippedSessions = 0;
    }
    public abstract void handleLimitExceeded();

    public void autoSkipIfNotCompleted(Session session) {
        if (!sessions.contains(session)) {
            throw new IllegalArgumentException("Session not found in task.");
        }

        // Trova la prossima sessione dello stesso task o subtask
        Session nextSession = findNextSession(session);

        // Verifica se la sessione corrente è non completata e se la prossima è già iniziata
        if (nextSession != null && LocalDateTime.now().isAfter(nextSession.getStartDate())
                && session.getState() != SessionState.COMPLETED) {
            // Chiama skipSession per marcare la sessione come SKIPPED
            skipSession(session);
        }
    }
    private Session findNextSession(Session currentSession) {
        return sessions.stream()
                .filter(s -> s.getTask().equals(currentSession.getTask())
                        && s.getStartDate().isAfter(currentSession.getEndDate()))
                .min(Comparator.comparing(Session::getStartDate))
                .orElse(null);
    }



    protected void removeAndFreezeTask(User user, Task task) {
        user.getCalendar().removeSessions(task);
        this.setState(TaskState.FREEZED);
        isInProgress = false;
        List<Folder> folders = user.getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(task)) {
                    subfolder.getTasks().remove(task);
                    taskRemoved = true;
                }
                if (subfolder.getType() == SubfolderType.FREEZED) {
                    subfolder.getTasks().add(task);
                }
            }
        }
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






