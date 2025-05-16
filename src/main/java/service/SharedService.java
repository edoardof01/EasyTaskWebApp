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



    public SharedDTO getSharedById(long id) {
        Shared shared = sharedDAO.findById(id);
        if (shared == null) {
            throw new EntityNotFoundException("Shared with id " + id + " not found");
        }
        return sharedMapper.toSharedDTO(shared);
    }

    public List<SharedDTO> getAllShared() {
        return sharedDAO.findAll().stream()
                .map(sharedMapper::toSharedDTO)
                .toList();
    }



    public SharedDTO createShared(String name, long userId, Topic topic, @Nullable LocalDateTime deadline, Integer totalTime,
                                  Timetable timeSlots, List<StrategyInstance> strategies, int priority,
                                  String description, List<Resource> resources, @Nullable List<Subtask> subtasks, List<Session> sessions, String userGuidance) {

        validateRequiredFields(name, topic, totalTime, timeSlots, strategies, priority, description);

        validateCoreFields(deadline, strategies, subtasks, sessions);

        User existingUser = userDAO.findById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("User with userId " +userId+ " does not exist.");
        }

        validateSessionOverlap(existingUser, sessions);

        validateSubtaskResources(resources, subtasks);

        validateSubtaskSessions(sessions, subtasks);

        validateSessions(sessions, timeSlots, totalTime);


        Shared sharedTask = new Shared(name, existingUser, topic, deadline, description, subtasks, sessions,0,
                priority, timeSlots, totalTime, strategies, resources, userGuidance);

        sharedTask.setComplexity(calculateComplexity(subtasks,resources));


        sharedDAO.save(sharedTask);
        userDAO.update(existingUser);
        calendarDAO.update(existingUser.getCalendar());

        return sharedMapper.toSharedDTO(sharedTask);
    }

    private void validateRequiredFields(String name, Topic topic, Integer totalTime, Timetable timeSlots,
                                        List<StrategyInstance> strategies, int priority, String description) {
        if (name == null || topic == null || totalTime == null || totalTime <= 0 ||
                timeSlots == null || strategies == null || strategies.isEmpty() ||
                priority <= 0 || priority > 5 || description == null) {
            throw new IllegalArgumentException("Mandatory fields missing or invalid fields");
        }
    }

    private void validateCoreFields(@Nullable LocalDateTime deadline, List<StrategyInstance> strategies, @Nullable List<Subtask> subtasks, List<Session> sessions) {
        if(subtasks != null){
            for(Subtask subtask:subtasks){
                if (subtask.getName()==null || subtask.getLevel()==null || subtask.getDescription()==null) throw new IllegalArgumentException("you must fill this fields");
            }
        }
        if(sessions != null){
            for(Session session:sessions){
                if(session.getStartDate()==null || session.getEndDate()==null) throw new IllegalArgumentException("you must fill this fields");
            }
        }
        if(sessions==null || sessions.isEmpty()){
            throw new IllegalArgumentException("you must specify Personal's sessions");
        }

        if(deadline != null){
            if(strategies.stream().anyMatch(strategy ->
                    strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)){
                throw new IllegalArgumentException("you cannot choose this strategy if a deadline is set");
            }
        }

        if(strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)){
            if(strategies.size()>1){
                throw new IllegalArgumentException("If this strategy is set, an other strategy can't be selected");
            }
        }
    }

    private void validateSessionOverlap(User user, List<Session> sessions) {
        if (!sessions.isEmpty()) {
            for (Session newSession : sessions) {
                for (Session existingSession : user.getCalendar().getSessions()) {
                    if (newSession.overlaps(existingSession)) {
                        throw new IllegalArgumentException("Session " + newSession + " overlaps with an existing session.");
                    }
                }
            }
        }
    }

    private void validateSubtaskResources(List<Resource> resources, @Nullable List<Subtask> subtasks) {
        if (subtasks == null) return;

        int totalMoney = 0;
        Map<Resource, Boolean> resourceUsage = new HashMap<>();
        Map<String, Resource> resourceMap = new HashMap<>();

        for (Resource resource : resources) {
            resourceMap.put(resource.getName(), resource);
            if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                resourceUsage.put(resource, false);
            }
        }

        for (Subtask subtask : subtasks) {
            for (Resource resource : subtask.getResources()) {
                if (resource.getType() == ResourceType.MONEY) {
                    totalMoney += resource.getMoney();
                    Resource moneyResource = resources.stream()
                            .filter(r -> ResourceType.MONEY.equals(r.getType()))
                            .findFirst()
                            .orElse(null);
                    if (moneyResource == null || totalMoney > moneyResource.getMoney()) {
                        throw new IllegalArgumentException("The sum of the money of the subtasks can't exceed the task one");
                    }
                } else if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                    Resource mainTaskResource = resourceMap.get(resource.getName());
                    if (mainTaskResource == null || !mainTaskResource.equals(resource)) {
                        throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName() + " not present in the task");
                    }
                    if (Boolean.TRUE.equals(resourceUsage.get(mainTaskResource))) {
                        throw new IllegalArgumentException("Resource " + resource.getName() + " has already been used by another subtask");
                    }
                    resourceUsage.put(mainTaskResource, true);
                } else {
                    if (!resources.contains(resource)) {
                        throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName());
                    }
                }
            }
        }
    }

    private void validateSubtaskSessions(List<Session> sessions, @Nullable List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) return;

        Set<Session> allAssignedSessions = new HashSet<>();

        for (Subtask subtask : subtasks) {
            for (Session session : subtask.getSessions()) {
                boolean sessionExistsInTask = sessions.stream().anyMatch(taskSession -> taskSession.equals(session));
                if (!sessionExistsInTask) {
                    throw new IllegalArgumentException("Session " + session + " in subtask does not exist in the main task.");
                }
                boolean sessionAlreadyAssigned = allAssignedSessions.contains(session);
                if (sessionAlreadyAssigned) {
                    throw new IllegalArgumentException("Session " + session + " is already assigned to another subtask.");
                }
                allAssignedSessions.add(session);
            }
        }
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

            long sessionDuration = Duration.between(session.getStartDate(), session.getEndDate()).toHours();
            if (sessionDuration <= 0) {
                throw new IllegalArgumentException("Session duration must be greater than 0");
            }
            totalScheduledTime += sessionDuration;

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
                slotEnd = LocalTime.of(23, 59);
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
                slotEnd = LocalTime.of(18, 0);
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


        return sessionStartTime.isBefore(slotEnd) && sessionEndTime.isAfter(slotStart);
    }



    private int calculateComplexity(@Nullable List<Subtask> subtasks, List<Resource> resources) {
        if (subtasks == null || subtasks.isEmpty()) {

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

    public SharedDTO modifyShared(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, Integer totalTime, Timetable timeSlots,
                                  List<StrategyInstance> strategies, Integer priority, String description, List<Resource> resources,
                                  List<Subtask> subtasks, @Nullable String userGuidance, List<Session> sessions, @Nullable Integer requiredUsers ) {


        if (requiredUsers != null) {
            throw new IllegalArgumentException("Users number must be set  for shared tasks");
        }

        validateRequiredFields(name, topic, totalTime, timeSlots, strategies, priority, description);

        validateCoreFields(deadline, strategies, subtasks, sessions);

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


        validateSessionOverlap(user, sessions);

        syncSessionsWithCalendar(sharedTask, sessions);

        // Validazione delle sessioni aggiornate
        validateSessions(sharedTask.getSessions(), timeSlots, totalTime);


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


        validateAndAssignSubtasks(sharedTask, subtasks);

        validateAndUpdateSubtasksResources(sharedTask, subtasks, resources);

        userDAO.update(user);
        calendarDAO.update(sharedTask.getUser().getCalendar());
        int complexity = calculateComplexity(subtasks, resources);
        sharedTask.setComplexity(complexity);
        sharedTask.setIsOnFeed(true);
        sharedDAO.update(sharedTask);
        return sharedMapper.toSharedDTO(sharedTask);
    }


    private void syncSessionsWithCalendar(Shared sharedTask, List<Session> newSessions) {
        List<Session> existingSessions = new ArrayList<>(sharedTask.getSessions());
        existingSessions.forEach(existing -> {
            if (newSessions.stream().noneMatch(session -> session.equals(existing))) {
                sharedTask.getSessions().remove(existing);
            }
        });
        newSessions.forEach(session -> {
            if (sharedTask.getSessions().stream().noneMatch(existing -> existing.equals(session))) {
                sharedTask.getSessions().add(session);
            }
        });
    }

    private void validateAndAssignSubtasks(Shared sharedTask, @Nullable List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) return;

        Set<Session> allAssignedSessions = new HashSet<>();

        for (Subtask subtask : subtasks) {
            List<Session> assignedSessions = subtask.getSessions();
            List<Session> reconciledSessions = new ArrayList<>();

            for (Session session : assignedSessions) {
                if (sharedTask.getSessions().contains(session)) {
                    if (allAssignedSessions.contains(session)) {
                        throw new IllegalArgumentException("Session " + session + " is already assigned to another subtask");
                    }
                    reconciledSessions.add(session);
                    allAssignedSessions.add(session);
                }
            }
            subtask.setSessions(reconciledSessions);
        }

        if (!allAssignedSessions.containsAll(sharedTask.getSessions())) {
            throw new IllegalArgumentException("Not all sessions from the main task have been assigned to subtasks");
        }

        sharedTask.getSubtasks().clear();
        sharedTask.getSubtasks().addAll(subtasks);
    }

    private void validateAndUpdateSubtasksResources(Shared sharedTask, List<Subtask> subtasks, List<Resource> resources) {
        if (subtasks == null || subtasks.isEmpty() || compareSubtasks(subtasks, sharedTask.getSubtasks())) {
            return; // Nessuna modifica necessaria
        }

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
                    totalMoney += resource.getMoney();
                    Resource moneyResource = resources.stream()
                            .filter(r -> ResourceType.MONEY.equals(r.getType()))
                            .findFirst()
                            .orElse(null);
                    if (moneyResource == null || totalMoney > moneyResource.getMoney()) {
                        throw new IllegalArgumentException("The sum of the money of the subtasks can't exceed the task one");
                    }
                } else if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                    Resource mainTaskResource = resourceMap.get(resource.getName());
                    if (mainTaskResource == null || !mainTaskResource.equals(resource)) {
                        throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName() + " not present in the task");
                    }
                    if (Boolean.TRUE.equals(resourceUsage.get(mainTaskResource))) {
                        throw new IllegalArgumentException("Resource " + resource.getName() + " has already been used by another subtask");
                    }
                    resourceUsage.put(mainTaskResource, true);
                } else {
                    if (!resources.contains(resource)) {
                        throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName());
                    }
                }
            }
        }
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
