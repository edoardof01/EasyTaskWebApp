package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

import static domain.SubfolderType.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    @Enumerated(EnumType.STRING)
    private SubfolderType currentSubfolderType;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private ArrayList<Subtask> subtasks = new ArrayList<>();
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ArrayList<Session> sessions = new ArrayList<>();
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ArrayList<Resource> resources = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    private Topic topic;
    @Enumerated(EnumType.STRING)
    private TaskState state;
    @Enumerated(EnumType.STRING)
    private Timetable timetable;
    @ElementCollection(targetClass = DefaultStrategy.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DefaultStrategy> strategies = new HashSet<>();

    public Task() {}

    public Task(String name, int complexity, String description,
                @Nullable LocalDateTime deadline, int percentageOfCompletion, int priority, int totalTime, Topic topic,
                TaskState state, Timetable timetable, DefaultStrategy strategy, ArrayList<Resource> resources) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.percentageOfCompletion = percentageOfCompletion;
        this.priority = priority;
        setStrategies(strategies);
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {}
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
    public void setTimetable(Timetable timetable) {}
    public Set<DefaultStrategy> getStrategies() {
        return strategies;
    }
    public int getConsecutiveSkippedSessions() {
        return consecutiveSkippedSessions;
    }
    public ArrayList<Subtask> getSubtasks() {
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
    public ArrayList<Session> getSessions() {
        return sessions;
    }
    public ArrayList<Resource> getResources() {
        return resources;
    }
    public boolean isInProgress() {
        return isInProgress;
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
        this.updateIsInProgress(true);
        user.getCalendar().addSessions(this.getSessions());

        ArrayList<Folder> folders = user.getFolders();
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
        user.getCalendar().removeSessions(this);
        ArrayList<Folder> folders = user.getFolders();
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
        this.setState(TaskState.FINISHED);
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
    public void commonForcedCompletionLogic(User user){
        for(Session session : this.getSessions()){
            if(session.getState() != SessionState.COMPLETED){
                session.setState(SessionState.COMPLETED);
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

    public void skipSession(Session session, User user) {
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
                handleLimitExceeded(user);
            }
        });

        // Verifica e applica le strategie relative al numero di sessioni saltate consecutivamente
        OptionalInt maxConsecSkippedSessions = strategies.stream()
                .filter(DefaultStrategy::requiresMaxConsecSkipped)
                .mapToInt(DefaultStrategy::getMaxConsecSkipped)
                .max();

        maxConsecSkippedSessions.ifPresent(max -> {
            if (consecutiveSkippedSessions > max) {
                handleLimitExceeded(user);
            }
        });
    } // LA PIÙ COMPLESSA GESTIONE DELLE SESSIONI NEL CALENDARIO È NEL SERVICE
    public void resetConsecutiveSkippedSessions() {
        consecutiveSkippedSessions = 0;
    }
    public abstract void handleLimitExceeded(User user);

    protected void removeAndFreezeTask(User user, Task task) {
        user.getCalendar().removeSessions(task);
        this.setState(TaskState.FREEZED);
        ArrayList<Folder> folders = user.getFolders();
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

    public abstract void toCalendar(User user);

    protected void updateIsInProgress(boolean b) {
        this.isInProgress = b;
        this.state = TaskState.INPROGRESS;
    }

    public abstract void deleteTask(User user);
    public abstract void modifyTask(User user);

    public abstract void completeTaskBySessions(User user);

    public abstract void forcedCompletion(User user);

}







// OSSERVAZIONE: I subtask da mettere nella fase di creazione del task saranno gestiti nel taskService
/*
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task createTaskWithSubtasks(Task task, List<Subtask> subtasks) {
        task.getSubtasks().addAll(subtasks);
        return taskRepository.save(task);
    }

    public Task updateTask(Task task, String name, String description, LocalDateTime deadline, List<Subtask> updatedSubtasks) {
        task.setName(name);
        task.setDescription(description);
        task.setDeadline(deadline);

        // Aggiorna i subtasks del task
        task.getSubtasks().clear();
        task.getSubtasks().addAll(updatedSubtasks);

        return taskRepository.save(task);
    }

    public void addSubtask(Task task, Subtask subtask) {
        task.getSubtasks().add(subtask);
        taskRepository.save(task);
    }

    public void removeSubtask(Task task, Subtask subtask) {
        task.getSubtasks().remove(subtask);
        taskRepository.save(task);
    }

    public List<Subtask> getSubtasks(Task task) {
        return new ArrayList<>(task.getSubtasks());
    }
}
*/








