package service;

import domain.*;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import orm.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


import static org.hibernate.query.sqm.tree.SqmNode.log;

@ApplicationScoped
public class SharedService {

    @Inject
    private SharedDAO sharedDAO;

    @Inject
    private SharedMapper sharedMapper;

    @Inject
    private SessionDAO sessionDAO;

    @Inject
    private CommentDAO commentDAO;

    @Inject
    private CalendarDAO calendarDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private SubtaskDAO subtaskDAO;



    // Restituisce un SharedDTO per ID
    public SharedDTO getSharedById(long id) {
        Shared shared = sharedDAO.findById(id);
        if (shared == null) {
            throw new EntityNotFoundException("Shared with id " + id + " not found");
        }
        return sharedMapper.toSharedDTO(shared);
    }

    // Restituisce tutti i task Shared come DTO
    public List<SharedDTO> getAllShared() {
        return sharedDAO.findAll().stream()
                .map(sharedMapper::toSharedDTO)
                .toList();
    }



    public SharedDTO createShared(String name, long userId, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                  Timetable timeSlots, List<StrategyInstance> strategies, int priority,
                                  String description, List<Resource> resources, @Nullable List<Subtask> subtasks, List<Session> sessions, String userGuidance) {

        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null || sessions == null || priority<=0 || priority>5
                || description == null || strategies.isEmpty() || sessions.isEmpty()|| userGuidance == null ) {
            throw new IllegalArgumentException("mandatory fields missing or invalid fields");
        }

        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)) {
            if (deadline != null) {
                throw new IllegalArgumentException("If this strategy is set, a deadline can't be selected");
            }
        }

        if(strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS)){
            if(strategies.size()>1){
                for(StrategyInstance strategy : strategies){
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS){
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }
        if(strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS)){
            if(strategies.size()>1){
                for(StrategyInstance strategy : strategies){
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS){
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }

        User existingUser = userDAO.findById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("User with userId " +userId+ " does not exist.");
        }
        if(!sessions.isEmpty()) {
            for (Session newSession : sessions) {
                // Verifica se la nuova sessione si sovrappone a quelle già nel calendario
                for (Session existingSession : existingUser.getCalendar().getSessions()) {
                    if (newSession.overlaps(existingSession)) {
                        throw new IllegalArgumentException("Session " + newSession + " overlaps with an existing session. CLASS SHARED SERVICE createShared");
                    }
                }
            }
        }


        if (subtasks != null) {
            int totalMoney = 0;
            Map<Resource, Boolean> resourceUsage = new HashMap<>();
            Map<String, Resource> resourceMap = new HashMap<>();

            // Inizializza la mappa delle risorse del task principale
            for (Resource resource : resources) {
                resourceMap.put(resource.getName(), resource);
                if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                    resourceUsage.put(resource, false); // Risorsa non ancora usata
                }
            }

            for (Subtask subtask : subtasks) {
                for (Resource resource : subtask.getResources()) {
                    if (resource.getType() == ResourceType.MONEY) {
                        // Gestione risorsa MONEY
                        totalMoney += resource.getMoney();
                        Resource moneyResource = resources.stream()
                                .filter(r -> ResourceType.MONEY.equals(r.getType()))
                                .findFirst()
                                .orElse(null);
                        if (moneyResource == null || totalMoney > moneyResource.getMoney()) {
                            throw new IllegalArgumentException("The sum of the money of the subtasks can't exceed the task one");
                        }
                    } else if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                        // Gestione risorse COMPETENCE e EQUIPMENT
                        Resource mainTaskResource = resourceMap.get(resource.getName());
                        if (mainTaskResource == null || !mainTaskResource.equals(resource)) {
                            throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName() + " not present in the task");
                        }
                        if (Boolean.TRUE.equals(resourceUsage.get(mainTaskResource))) {
                            throw new IllegalArgumentException("Resource " + resource.getName() + " has already been used by another subtask");
                        }
                        // Segna la risorsa come usata
                        resourceUsage.put(mainTaskResource, true);
                    } else {
                        if (!resources.contains(resource)) {
                            throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName());
                        }
                    }
                }
            }
        }

        // CONTROLLO SESSIONI SUBTASK
        if (subtasks != null && !subtasks.isEmpty()) {
            Set<Session> allAssignedSessions = new HashSet<>(); // Per tenere traccia delle sessioni assegnate
            // Verifica che le sessioni dei subtasks combacino con quelle del task principale
            for (Subtask subtask : subtasks) {
                List<Session> subtaskSessions = subtask.getSessions();
                // Verifica che ogni sessione del subtask sia presente nel task principale
                for (Session session : subtaskSessions) {
                    // Controlla se la sessione corrente è uguale a una delle sessioni del task principale
                    boolean sessionExistsInTask = sessions.stream().anyMatch(taskSession -> taskSession.equals(session));
                    if (!sessionExistsInTask) {
                        throw new IllegalArgumentException("Session " + session + " in subtask does not exist in the main task. CLASS PERSONAL SERVICE createPersonal");
                    }
                    // Controlla se la sessione è già stata assegnata a un altro subtask
                    boolean sessionAlreadyAssigned = allAssignedSessions.stream().anyMatch(assignedSession -> assignedSession.equals(session));
                    if (sessionAlreadyAssigned) {
                        throw new IllegalArgumentException("Session " + session + " is already assigned to another subtask.");
                    }
                    // Aggiungi la sessione alla lista delle sessioni assegnate
                    allAssignedSessions.add(session);
                }
            }
        }
        // Validazione finale delle sessioni
        validateSessions(sessions, timeSlots, totalTime);

        assert subtasks != null;
        Shared sharedTask = new Shared(name, existingUser, topic, deadline, description, subtasks, sessions,
                0 , priority, timeSlots, totalTime, strategies, resources, userGuidance);

        sharedTask.setComplexity(calculateComplexity(subtasks,resources));


        sharedDAO.save(sharedTask);
        userDAO.update(existingUser);
        calendarDAO.update(existingUser.getCalendar());

        return sharedMapper.toSharedDTO(sharedTask);
    }


    private void validateSessions(List<Session> sessions, Timetable timeSlots, int totalTime) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        long totalScheduledTime = 0;

        for (Session session : sessions) {
            if (session.getStartDate() == null || session.getEndDate() == null) {
                throw new IllegalArgumentException("Session startDate and endDate cannot be null");
            }

            if (!session.getStartDate().isBefore(session.getEndDate())) {
                throw new IllegalArgumentException("Session startDate must be before endDate");
            }

            // Calcola la durata della sessione in ore
            long sessionDuration = Duration.between(session.getStartDate(), session.getEndDate()).toHours();
            if (sessionDuration <= 0) {
                throw new IllegalArgumentException("Session duration must be greater than 0");
            }
            totalScheduledTime += sessionDuration;

            // Verifica che la sessione sia all'interno delle fasce orarie permesse
            if (!isWithinAllowedTimeSlot(timeSlots, session)) {
                throw new IllegalArgumentException("Session with start time " + session.getStartDate() +
                        " and end time " + session.getEndDate() + " is not within the allowed time slots");
            }
        }

        if (totalScheduledTime > totalTime) {
            throw new IllegalArgumentException("The total duration of sessions exceeds the task's total time");
        }
    }


    private static boolean isWithinAllowedTimeSlot(Timetable timeSlots, Session session) {
        LocalTime sessionStartTime = session.getStartDate().toLocalTime();
        LocalTime sessionEndTime = session.getEndDate().toLocalTime();

        return doesSessionOverlapWithTimeSlot(sessionStartTime, sessionEndTime, timeSlots);
    }

    private static boolean doesSessionOverlapWithTimeSlot(LocalTime sessionStartTime, LocalTime sessionEndTime, Timetable timeSlot) {
        LocalTime slotStart;
        LocalTime slotEnd;

        // Definiamo i limiti dei vari time slot
        switch (timeSlot) {
            case MORNING -> {
                slotStart = LocalTime.of(6, 0);
                slotEnd = LocalTime.of(12, 0);
            }
            case AFTERNOON -> {
                slotStart = LocalTime.of(12, 0);
                slotEnd = LocalTime.of(18, 0);
            }
            case EVENING -> {
                slotStart = LocalTime.of(18, 0);
                slotEnd = LocalTime.of(23, 59); // Corretto per includere sessioni fino alla fine del giorno
            }
            case NIGHT -> {
                slotStart = LocalTime.of(0, 0);
                slotEnd = LocalTime.of(6, 0);
            }
            case MORNING_AFTERNOON -> {
                // Combina le due fasce orarie
                slotStart = LocalTime.of(6, 0);
                slotEnd = LocalTime.of(18, 0);
            }
            case MORNING_EVENING -> {
                // Combina le due fasce orarie
                slotStart = LocalTime.of(6, 0);
                slotEnd = LocalTime.of(0, 0);
            }
            case AFTERNOON_EVENING -> {
                // Combina le due fasce orarie
                slotStart = LocalTime.of(12, 0);
                slotEnd = LocalTime.of(23, 59);
            }
            case NIGHT_AFTERNOON -> {
                // Combina le due fasce orarie
                slotStart = LocalTime.of(0, 0);
                slotEnd = LocalTime.of(18, 0); // Considera la fine della notte, cioè il mattino
            }
            case NIGHT_MORNING -> {
                // Combina le due fasce orarie
                slotStart = LocalTime.of(0, 0);
                slotEnd = LocalTime.of(12, 0);
            }
            case ALL_DAY -> {
                // Copre tutto il giorno
                slotStart = LocalTime.of(0, 0);
                slotEnd = LocalTime.of(23, 59);
            }
            default -> throw new IllegalArgumentException("Unknown time slot: " + timeSlot);
        }

        // Verifica sovrapposizione tra sessione e slot
        return sessionStartTime.isBefore(slotEnd) && sessionEndTime.isAfter(slotStart);
    }



    private int calculateComplexity(@Nullable List<Subtask> subtasks, List<Resource> resources) {
        if (subtasks == null || subtasks.isEmpty()) {
            // Se subtasks è null o vuota, restituisce subito il calcolo basato solo sulle risorse.
            return calculateResourceScore(resources) / 2;
        }
        int subtaskScore;
        if (subtasks.size() <= 3) subtaskScore = 1;
        else if (subtasks.size() <= 5) subtaskScore = 2;
        else if (subtasks.size() <= 10) subtaskScore = 3;
        else if (subtasks.size() <= 20) subtaskScore = 4;
        else subtaskScore = 5;

        int resourceScore = calculateResourceScore(resources);
        return (subtaskScore + resourceScore) / 2;
    }

    public int calculateResourceScore(List<Resource> resources) {
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

    public SharedDTO modifyShared(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime, Timetable timeSlots,
                                  List<StrategyInstance> strategies, Integer priority, String description, List<Resource> resources,
                                  List<Subtask> subtasks, @Nullable String userGuidance, List<Session> sessions, @Nullable Integer requiredUsers ) {

        if(subtasks != null){
            for(Subtask subtask:subtasks){
                if (subtask.getName()==null || subtask.getLevel()==null || subtask.getDescription()==null) throw new IllegalArgumentException("you must fill this fields");
            }
        }
        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("mandatory fields missing or invalid fields");
        }
        if (requiredUsers != null) {
            throw new IllegalArgumentException("Users number can be set only for shared tasks");
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (deadline != null) {
                throw new IllegalArgumentException("If this strategy is set, a deadline can't be selected");
            }
        }
        if(strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)){
            if(strategies.size()>1){
                throw new IllegalArgumentException("If this strategy is set, an other strategy can't be selected");
            }
        }
        if(strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)){
            if(strategies.size()>1){
                throw new IllegalArgumentException("If this strategy is set, an other strategy can't be selected");
            }
        }
        if(strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS)){
            if(strategies.size()>1){
                for(StrategyInstance strategy : strategies){
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS ||
                            strategy.getStrategy() == DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING){
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }
        if(strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS)){
            if(strategies.size()>1){
                for(StrategyInstance strategy : strategies){
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS ||
                            strategy.getStrategy() == DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING){
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }
        if (deadline != null) {
            if (strategies.stream().anyMatch(strategy ->
                    strategy.getStrategy() == DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
                throw new IllegalArgumentException("If a deadline is set, this strategy can't be selected");
            }
        }


        Shared sharedTask = sharedDAO.findById(taskId);
        if (sharedTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }

        User user = sharedTask.getUser();
        if (user == null) {
            throw new IllegalStateException("no user associated to this task.");
        }

        sharedTask.modifyTask();
        userDAO.update(user);
        sharedDAO.update(sharedTask);


        if(!sessions.isEmpty()) {
            for (Session newSession : sessions) {
                // Verifica se la nuova sessione si sovrappone a quelle già nel calendario
                for (Session existingSession : user.getCalendar().getSessions()) {
                    if (newSession.overlaps(existingSession)) {
                        throw new IllegalArgumentException("Session " + newSession + " overlaps with an existing session. CLASS SHARED SERVICE (modify)");
                    }
                }
            }
        }

        // Sincronizza le sessioni tra il task e il calendario
        List<Session> existingSessions = new ArrayList<>(sharedTask.getSessions());

        // Rimuovi le sessioni non più presenti
        for (Session existing : existingSessions) {
            // Verifica se la sessione non è più presente nel nuovo elenco di sessioni
            boolean sessionExistsInNewList = sessions.stream().anyMatch(session -> session.equals(existing));
            if (!sessionExistsInNewList) {
                sharedTask.getSessions().remove(existing);
            }
        }

        // Aggiungi nuove sessioni, se non già presenti
        for (Session session : sessions) {
            // Verifica se la sessione non è già presente nel task
            boolean sessionExistsInTask = sharedTask.getSessions().stream().noneMatch(taskSession -> taskSession.equals(session));
            if (sessionExistsInTask) {
                sharedTask.getSessions().add(session);
            }
        }

        // Validazione delle sessioni aggiornate
        validateSessions(sharedTask.getSessions(), timeSlots, totalTime);

        //sharedTask.getUser().getCalendar().addSessions(sharedTask.getSessions());  Prima non c'era ATTENTOOOOOOOOOOOOOO


        calendarDAO.update(user.getCalendar());

        sharedTask.setName(name);
        sharedTask.setTopic(topic);
        if (deadline != null) sharedTask.setDeadline(deadline);
        sharedTask.getStrategies().clear(); // Rimuove tutti gli elementi esistenti
        sharedTask.getStrategies().addAll(strategies); // Aggiunge i nuovi elementi
        sharedTask.setTotalTime(totalTime);
        sharedTask.setTimetable(timeSlots);
        sharedTask.setPriority(priority);
        if (description != null) sharedTask.setDescription(description);
        if (resources != null) {
            sharedTask.getResources().clear(); // Rimuove tutti gli elementi esistenti
            sharedTask.getResources().addAll(resources); // Aggiunge i nuovi elementi
        }

        if (userGuidance != null) {
            sharedTask.updateUserGuidance(userGuidance);
        }
        // Suddivisione delle sessioni tra i subtasks
        if (subtasks != null) {
            Set<Session> allAssignedSessions = new HashSet<>();
            // Distribuisci le sessioni tra i subtasks
            for (Subtask subtask : subtasks) {
                List<Session> assignedSessions = subtask.getSessions();

                // Gestisci sessioni preesistenti nel subtask
                List<Session> reconciledSessions = new ArrayList<>();
                for (Session session : assignedSessions) {
                    // Verifica se la sessione è presente nel task principale
                    boolean sessionExistsInTask = sharedTask.getSessions().stream().anyMatch(taskSession -> taskSession.equals(session));
                    if (sessionExistsInTask) {
                        // Verifica se la sessione è già stata assegnata a un altro subtask
                        boolean sessionAlreadyAssigned = allAssignedSessions.stream().anyMatch(assignedSession -> assignedSession.equals(session));
                        if (sessionAlreadyAssigned) {
                            throw new IllegalArgumentException("Session " + session + " is already assigned to another subtask");
                        }
                        reconciledSessions.add(session);
                        allAssignedSessions.add(session);
                    }
                }
                subtask.setSessions(reconciledSessions);
            }


            // Verifica che tutte le sessioni del task siano state assegnate
            if (!allAssignedSessions.containsAll(sharedTask.getSessions())) {
                throw new IllegalArgumentException("Not all sessions from the main task have been assigned to subtasks");
            }

            // Aggiorna i subtasks del task
            sharedTask.getSubtasks().clear();
            sharedTask.getSubtasks().addAll(subtasks);
        }

        if (subtasks!=null && !subtasks.isEmpty() && !compareSubtasks(subtasks,sharedTask.getSubtasks())){
            sharedTask.getSubtasks().clear();
            sharedTask.getSubtasks().addAll(subtasks);
            int totalMoney = 0;
            Map<Resource, Boolean> resourceUsage = new HashMap<>();
            Map<String, Resource> resourceMap = new HashMap<>();
            assert resources != null;
            for (Resource resource : resources) {
                resourceMap.put(resource.getName(), resource);
                if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                    resourceUsage.put(resource, false); // Risorsa non ancora usata
                }
            }
            for (Subtask subtask : subtasks) {
                for (Resource resource : subtask.getResources()) {
                    if (resource.getType() == ResourceType.MONEY) {
                        // Gestione risorsa MONEY
                        totalMoney += resource.getMoney();
                        Resource moneyResource = resources.stream()
                                .filter(r -> ResourceType.MONEY.equals(r.getType()))
                                .findFirst()
                                .orElse(null);
                        if (moneyResource == null || totalMoney > moneyResource.getMoney()) {
                            throw new IllegalArgumentException("The sum of the money of the subtasks can't exceed the task one");
                        }
                    } else if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                        // Gestione risorse COMPETENCE e EQUIPMENT
                        Resource mainTaskResource = resourceMap.get(resource.getName());
                        if (mainTaskResource == null || !mainTaskResource.equals(resource)) {
                            throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName() + " not present in the task");
                        }
                        if (Boolean.TRUE.equals(resourceUsage.get(mainTaskResource))) {
                            throw new IllegalArgumentException("Resource " + resource.getName() + " has already been used by another subtask");
                        }
                        // Segna la risorsa come usata
                        resourceUsage.put(mainTaskResource, true);
                    } else {
                        if (!resources.contains(resource)) {
                            throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName());
                        }
                    }
                }
            }
        }
        userDAO.update(user);
        calendarDAO.update(sharedTask.getUser().getCalendar());
        int complexity = calculateComplexity(subtasks, resources);
        sharedTask.setComplexity(complexity);
        Feed.getInstance().getShared().add(sharedTask);
        Feed.getInstance().getContributors().remove(sharedTask.getUser());
        sharedDAO.update(sharedTask);
        return sharedMapper.toSharedDTO(sharedTask);
    }




    public boolean compareSubtasks(List<Subtask> subtasks, List<Subtask> personalTaskSubtasks) {
        subtasks.sort(Comparator.comparing(Subtask::getName));
        personalTaskSubtasks.sort(Comparator.comparing(Subtask::getName));
        return subtasks.equals(personalTaskSubtasks);
    }

    @Transactional
    public void deleteShared(Long taskId) {
        Shared sharedTask = sharedDAO.findById(taskId);
        if (sharedTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }
        sharedTask.deleteTask();
        sharedDAO.delete(sharedTask.getId());
        calendarDAO.update(sharedTask.getUser().getCalendar());
    }

    @Transactional
    public void addTaskToFeed(Shared sharedTask, @Nullable String guidance) {
        if(guidance != null) {
            sharedTask.updateUserGuidance(guidance);
        }
        Feed.getInstance().getShared().add(sharedTask);
        Feed.getInstance().getContributors().add(sharedTask.getUser());
        sharedDAO.update(sharedTask);
    }

    @Transactional
    public void moveToCalendar(long sharedId) {
        Shared shared = sharedDAO.findById(sharedId);
        if (shared == null) {
            throw new IllegalArgumentException("Task con ID " + sharedId + " not found.");
        }
        shared.toCalendar();


        userDAO.update(shared.getUser());
        sharedDAO.update(shared);
        calendarDAO.update(shared.getUser().getCalendar());
    }

    @Transactional
    public void removeSharedFromFeed(long sharedId){
        Shared shared = sharedDAO.findById(sharedId);
        shared.removeTaskJustFromFeed();
        sharedDAO.update(shared);
    }

    @Transactional
    public void completeSharedBySessionsWithComment(Long commentId, long sharedId) {
        try {
            Shared shared = sharedDAO.findById(sharedId);
            if(shared == null){
                throw new IllegalArgumentException("Shared with ID " + sharedId + " not found. SHARED SERVICE complete...withComment");
            }
            Comment comment = commentDAO.findById(commentId);
           /* shared.bestComment(comment);*/
            shared.completeBySessionsAndChooseBestComment(comment);
            userDAO.update(shared.getUser());
            sharedDAO.update(shared);
            calendarDAO.update(shared.getUser().getCalendar());
        } catch (IllegalArgumentException e) {
            log.error("Error during transaction: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            throw e;
        }
    }



    @Transactional
    public void completeSharedBySessions(long sharedId) {
        Shared shared = sharedDAO.findById(sharedId);
        if(shared == null) {
            throw new IllegalArgumentException("Shared with ID " + sharedId + " not found.");
        }
        shared.completeTaskBySessions();
        userDAO.update(shared.getUser());
        sharedDAO.update(shared);
        calendarDAO.update(shared.getUser().getCalendar());
    }



    @Transactional
    public void completeSession(long sharedId, long sessionId) {
        Shared shared = sharedDAO.findById(sharedId);
        Session session = sessionDAO.findById(sessionId);
        if(shared == null){
            throw new IllegalArgumentException("Task with ID " + sharedId + " not found. SHARED SERVICE completeSession");
        }
        if(session == null){
            throw new IllegalArgumentException("session with ID " + sessionId + " not found. SHARED SERVICE completeSession");
        }
        // Completamento della sessione nei subtasks (se esiste una sessione corrispondente)
        if (shared.getSubtasks() != null) {
            for (Subtask subtask : shared.getSubtasks()) {
                for (Session subSession : subtask.getSessions()) {
                    if (subSession.equals(session)) {
                        subSession.setState(SessionState.COMPLETED);
                        subtaskDAO.update(subtask);
                        break;  // Uscita dal ciclo appena trovata la sessione corrispondente
                    }
                }
            }
        }
        shared.completeSession(session);
        sessionDAO.update(session);
        sharedDAO.update(shared);
    }

    @Transactional
    public void forceCompletion(long sharedId) {
        Shared shared = sharedDAO.findById(sharedId);
        if(shared == null) return;
        shared.forcedCompletion();
        sharedDAO.update(shared);
        calendarDAO.update(shared.getUser().getCalendar());
        for(Session session : shared.getSessions()) {
            sessionDAO.update(session);
        }
    }

    @Transactional
    public void handleLimitExceeded(long sessionId, long sharedId) {
        Shared shared = sharedDAO.findById(sharedId);
        Session session = sessionDAO.findById(sessionId);
        if (shared == null) {
            throw new IllegalArgumentException("Personal task with ID " + sharedId + " not found.");
        }
        if (session == null) {
            throw new IllegalArgumentException("Session with ID " + sharedId + " not found.");
        }
        boolean found = false;
        for (Session oldSession : shared.getSessions()) {
            if (session.equals(oldSession)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("session not found in task. CLASS PERSONALSERVICE handleLimit...");
        }
        shared.autoSkipIfNotCompleted(session);
        sharedDAO.update(shared);
        calendarDAO.update(shared.getUser().getCalendar());
    }

}
