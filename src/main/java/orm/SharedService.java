package orm;

import domain.*;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.eclipse.persistence.expressions.ExpressionOperator.sum;

@ApplicationScoped
public class SharedService {

    @Inject
    private SharedDAO sharedDAO;

    // Restituisce un task Shared per ID
    public Shared getSharedById(long id) {
        if(sharedDAO.findById(id)==null){
            throw new EntityNotFoundException("Shared with id " + id + " not found");
        }
        return sharedDAO.findById(id);
    }

    // Restituisce tutti i task Shared
    public List<Shared> getAllShared() {
        return sharedDAO.findAll();
    }


    @Transactional
    public void addTaskToFeed(Shared sharedTask, String guidance) {
        // Imposta la guida dell'utente
        sharedTask.updateUserGuidance(guidance);

        // Cambia lo stato del task a INPROGRESS se non lo è già
        sharedTask.setState(TaskState.INPROGRESS);

        // Aggiungi il task al feed
        Feed.getInstance().addTask(sharedTask);

        // Salva le modifiche nel database
        sharedDAO.update(sharedTask);
    }

    public Shared createShared(String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                               Set<Timetable> timeSlots, Set<DefaultStrategy> strategies, int priority,
                               String description, ArrayList<Resource> resources,@Nullable List<Subtask> subtasks,
                               @Nullable Integer requiredUsers, @Nullable String userGuidance) {

        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("Campi obbligatori mancanti o non validi.");
        }

        if (requiredUsers != null) {
            throw new IllegalArgumentException("Il numero utenti può essere impostato solo per task di gruppo.");
        }

        if (strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (deadline != null) {
                throw new IllegalArgumentException("if this strategy is set, a deadline can't be selected");
            }
        }
        if(deadline != null){
            if(strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)){
                throw new IllegalArgumentException("if a deadline is set, this strategy can't be selected");
            }

        }
        if(subtasks != null){
            int totalMoney = 0;
            for(Subtask subtask : subtasks){
                for(Resource resource : subtask.getResources()){
                    if(resource.getType()!=ResourceType.MONEY){
                        if(!resources.contains(resource)){
                            throw new IllegalArgumentException("subtasks can't contain resource " + resource);
                        }
                    }
                    else{
                        totalMoney += resource.getMoney();
                        Optional<Resource> moneyResource = resources.stream()
                                .filter(resourceMoney -> resourceMoney.getMoney() != null)
                                .findFirst();
                        if(moneyResource.isEmpty()){
                            throw new IllegalArgumentException("subtasks can't contain the resource of type MONEY");
                        }
                        if (totalMoney > moneyResource.get().getMoney()) {
                            subtasks.remove(subtask);
                            throw new IllegalArgumentException("the sum of the money of the subtasks can't exceed the task one");
                        }
                    }

                }
            }
        }


        assert subtasks != null;
        int complexity = calculateComplexity(subtasks, resources);

        // 6. Creazione e salvataggio del task Shared
        Shared sharedTask = new Shared(name, topic, TaskState.INPROGRESS, deadline, description,
                0, complexity, priority, timeSlots, totalTime, strategies, resources);

        if (userGuidance != null) {
            sharedTask.updateUserGuidance(userGuidance);
        }

        // Aggiunta del task al feed e ritorno dell'istanza
        Feed.getInstance().addTask(sharedTask);
        return sharedTask;
    }

    // Metodo per calcolare la complessità in base ai subtask e alle risorse
    private int calculateComplexity(List<Subtask> subtasks, List<Resource> resources) {
        int subtaskScore = 0;
        if (subtasks.size() <= 3) subtaskScore = 1;
        else if (subtasks.size() <= 5) subtaskScore = 2;
        else if (subtasks.size() <= 10) subtaskScore = 3;
        else if (subtasks.size() <= 20) subtaskScore = 4;
        else subtaskScore = 5;

        int resourceScore = calculateResourceScore(resources);
        return (subtaskScore + resourceScore) / 2;
    }

    private int calculateResourceScore(List<Resource> resources) {
        int score = resources.stream().mapToInt(Resource::getValue).sum();
        if (score <= 10) return 1;
        else if (score <= 20) return 2;
        else if (score <= 30) return 3;
        else if (score <= 40) return 4;
        else return 5;
    }


    public Shared modifyShared(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                               Set<Timetable> timeSlots, DefaultStrategy strategy, int priority, String description,
                               ArrayList<Resource> resources, List<Subtask> subtasks, @Nullable String userGuidance) {

        // Recupero il task esistente dal feed o dal database
        Shared sharedTask = getSharedById(taskId);
        if (sharedTask== null) {
            throw new IllegalArgumentException("Task con ID " + taskId + " non trovato.");
        }

        User user = sharedTask.getUser();
        if (user == null) {
            throw new IllegalStateException("Nessun utente associato al task.");
        }
        // 1. Cambiare lo stato del task a FREEZED e spostarlo nell'apposita sottoCartella
        sharedTask.setState(TaskState.FREEZED);
        // Presumibilmente spostiamo il task nelle sottocartelle appropriate (questa logica dipende dalla tua implementazione)
        sharedTask.modifyTask(user);

        // 2. Modificare i campi del task
        if (name != null) sharedTask.setName(name);
        if (topic != null) sharedTask.setTopic(topic);
        if (deadline != null) sharedTask.setDeadline(deadline);
        sharedTask.setTotalTime(totalTime);
        if (timeSlots != null) sharedTask.setTimetable(timeSlots);
        sharedTask.setPriority(priority);
        if (description != null) sharedTask.setDescription(description);
        if (resources != null) sharedTask.setResources(resources);

        // Ricalcolare la complessità
        assert subtasks != null;
        int complexity = calculateComplexity(subtasks, resources);
        sharedTask.setComplexity(complexity);

        // Aggiornamento di userGuidance (se presente)
        if (userGuidance != null) {
            sharedTask.updateUserGuidance(userGuidance);
        }

        // 3. Aggiungere di nuovo il task al feed
        Feed.getInstance().addTask(sharedTask);

        // 4. Ritorno del task modificato
        return sharedTask;
    }
    @Transactional
    public void deleteShared(Long taskId) {
        // Recupero del task Shared esistente
        Shared sharedTask = getSharedById(taskId);
        if (sharedTask == null) {
            throw new IllegalArgumentException("Task con ID " + taskId + " non trovato.");
        }

        // Recupero dell'utente associato
        User user = sharedTask.getUser();
        if (user == null) {
            throw new IllegalStateException("Nessun utente associato al task.");
        }


        sharedTask.deleteTask(user);  // Chiama il metodo per rimuovere il task dalle sottocartelle, dal calendario e dal feed

        sharedDAO.delete(sharedTask.getId());
    }








}

