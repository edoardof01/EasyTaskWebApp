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

@ApplicationScoped
public class PersonalService {
    @Inject
    private PersonalDAO personalDAO;

    @Inject
    private CalendarDAO calendarDAO;

    @Inject
    private PersonalMapper personalMapper;

    @Inject
    private SessionDAO sessionDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private SubtaskDAO subtaskDAO;


    public PersonalDTO getPersonalById(long id) {
        Personal personal = personalDAO.findById(id);
        if (personal == null) {
            throw new EntityNotFoundException("Personal with id " + id + " not found");
        }
        return personalMapper.toPersonalDTO(personal);
    }

    public List<Personal> getAllPersonalEntities() {
        return personalDAO.findAll();
    }


    public List<PersonalDTO> getAllPersonal() {
        return personalDAO.findAll().stream()
                .map(personalMapper::toPersonalDTO)
                .toList();
    }

    public PersonalDTO createPersonal(String name, long userId, Topic topic, @Nullable LocalDateTime deadline,
                                      Integer totalTime, Timetable timeSlots, List<StrategyInstance> strategies,
                                      int priority, String description, List<Resource> resources,
                                      @Nullable List<Subtask> subtasks, List<Session> sessions) {


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

        Personal personalTask = new Personal(name, existingUser, topic, deadline, description, subtasks, sessions,
                0, priority, timeSlots, totalTime, strategies, resources);
        personalTask.setComplexity(calculateComplexity(subtasks,resources));
        personalDAO.save(personalTask);
        userDAO.update(existingUser);
        return personalMapper.toPersonalDTO(personalTask);
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

            if(session.getStartDate().isBefore(LocalDateTime.now())){
                throw new IllegalArgumentException("Session startDate cannot be before current date");
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

        if (totalScheduledTime != totalTime) {
            throw new IllegalArgumentException("The total duration of sessions must be the total time");
        }
    }


    private static boolean isWithinAllowedTimeSlot(Timetable timeSlots, Session session) {
        LocalTime sessionStartTime = session.getStartDate().toLocalTime();
        LocalTime sessionEndTime = session.getEndDate().toLocalTime();
        return doesSessionOverlapsWithTimeSlot(sessionStartTime, sessionEndTime, timeSlots);
    }

    private static boolean doesSessionOverlapsWithTimeSlot(LocalTime sessionStartTime,
                                                           LocalTime sessionEndTime,
                                                           Timetable timeSlot) {
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
            // per motivi di spazio nell'immagine sono omessi MORNING_AFTERNOON,
            // MORNING_EVENING, AFTERNOON_EVENING, NIGHT_MORNING
            case ALL_DAY -> {
                slotStart = LocalTime.of(0, 0);
                slotEnd = LocalTime.of(23, 59);
            }
            default -> throw new IllegalArgumentException("Unknown time slot: " + timeSlot);
        }
        return sessionStartTime.isBefore(slotEnd) && sessionEndTime.isAfter(slotStart);
    }




    private int calculateComplexity(List<Subtask> subtasks, List<Resource> resources) {
        int subtaskScore;
        if (subtasks == null || subtasks.isEmpty()) {
            // Se non ci sono subtasks, usa solo il punteggio delle risorse
            subtaskScore = 0; // oppure imposta un valore che rifletta l'assenza di suddivisione
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
        int resourceScore = calculateResourceScore(resources); // Supponiamo di avere questo metodo aggiornato
        if (subtasks == null || subtasks.isEmpty()) {
            return resourceScore;
        } else {
            return (subtaskScore + resourceScore) / 2;
        }
    }


    private int calculateResourceScore(List<Resource> resources) {
        int totalScore = resources.stream()
                .mapToInt(resource -> {
                    if (resource.getType() == ResourceType.MONEY) {
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

    public PersonalDTO modifyPersonal(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, Integer totalTime,
                                      Timetable timeSlots, List<StrategyInstance> strategies, int priority, String description,
                                      List<Resource> resources,@Nullable List<Subtask> subtasks, List<Session> sessions) {

       validateRequiredFields(name,topic,totalTime,timeSlots,strategies,priority,description);
        validateCoreFields(deadline, strategies, subtasks, sessions);
        Personal personalTask = personalDAO.findById(taskId);
        if (personalTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }
        User user = personalTask.getUser();
        if (user == null) {
            throw new IllegalStateException("no user associated to this task");
        }
        personalTask.modifyTask();
        validateSessionOverlap(user, sessions);
        syncSessionsWithCalendar(personalTask, sessions);

        validateSessions(personalTask.getSessions(), timeSlots, totalTime);
        userDAO.update(user);
        calendarDAO.update(user.getCalendar());
        personalTask.setName(name);
        personalTask.setTopic(topic);
        if (deadline != null) personalTask.setDeadline(deadline);
        personalTask.setTotalTime(totalTime);
        personalTask.setTimetable(timeSlots);
        personalTask.setPriority(priority);
        personalTask.getStrategies().clear();
        personalTask.getStrategies().addAll(strategies);
        personalTask.setDescription(description);
        if (resources != null) {
            personalTask.getResources().clear();
            personalTask.getResources().addAll(resources);
        }
        validateAndAssignSubtasks(personalTask, subtasks);
        validateAndUpdateSubtasksResources(personalTask,subtasks,resources);
        validateSessions(personalTask.getSessions(),timeSlots,totalTime);
        int complexity = calculateComplexity(subtasks, resources);
        personalTask.setComplexity(complexity);
        calendarDAO.update(personalTask.getUser().getCalendar());
        personalDAO.update(personalTask);
        return personalMapper.toPersonalDTO(personalTask);
    }

    private void validateAndUpdateSubtasksResources(Personal personalTask, List<Subtask> subtasks, List<Resource> resources) {
        if (subtasks == null || compareSubtasks(subtasks, personalTask.getSubtasks())) {
            return;
        }
        personalTask.getSubtasks().clear();
        personalTask.getSubtasks().addAll(subtasks);

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


    private void syncSessionsWithCalendar(Personal personalTask, List<Session> newSessions) {
        List<Session> existingSessions = new ArrayList<>(personalTask.getSessions());

        existingSessions.forEach(existing -> {
            if (newSessions.stream().noneMatch(session -> session.equals(existing))) {
                personalTask.getSessions().remove(existing);
            }
        });

        newSessions.forEach(session -> {
            if (personalTask.getSessions().stream().noneMatch(existing -> existing.equals(session))) {
                personalTask.getSessions().add(session);
            }
        });
    }

    private void validateAndAssignSubtasks(Personal personalTask, @Nullable List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) return;

        Set<Session> allAssignedSessions = new HashSet<>();
        for (Subtask subtask : subtasks) {
            List<Session> assignedSessions = subtask.getSessions();
            List<Session> reconciledSessions = new ArrayList<>();

            for (Session session : assignedSessions) {
                if (personalTask.getSessions().contains(session) && !allAssignedSessions.contains(session)) {
                    reconciledSessions.add(session);
                    allAssignedSessions.add(session);
                } else {
                    throw new IllegalArgumentException("Session " + session + " is already assigned to another subtask");
                }
            }
            subtask.setSessions(reconciledSessions);
        }

        if (!allAssignedSessions.containsAll(personalTask.getSessions())) {
            throw new IllegalArgumentException("Not all sessions from the main task have been assigned to subtasks");
        }

        personalTask.getSubtasks().clear();
        personalTask.getSubtasks().addAll(subtasks);
    }






    public boolean compareSubtasks(List<Subtask> subtasks, List<Subtask> personalTaskSubtasks) {
        subtasks.sort(Comparator.comparing(Subtask::getName));
        personalTaskSubtasks.sort(Comparator.comparing(Subtask::getName));
        return subtasks.equals(personalTaskSubtasks);
    }




    @Transactional
    public void deletePersonal(Long taskId) {
        Personal personalTask = personalDAO.findById(taskId);
        if (personalTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }
        personalTask.deleteTask();
        calendarDAO.update(personalTask.getUser().getCalendar());
        personalDAO.delete(personalTask.getId());
    }

    @Transactional
    public void moveToCalendar(long personalId) {
        Personal personal = personalDAO.findById(personalId);
        if (personal == null) {
            throw new IllegalArgumentException("Task with ID " + personalId + " not found.");
        }
        personal.toCalendar();
        calendarDAO.update(personal.getUser().getCalendar());
        personalDAO.update(personal);
        userDAO.update(personal.getUser());
    }

    @Transactional
    public void completeSession(long personalId, long sessionId) {
        Personal personal = personalDAO.findById(personalId);
        Session session = sessionDAO.findById(sessionId);
        if(personal == null){
            throw new IllegalArgumentException("Task with ID " + personalId + " not found. PERSONAL SERVICE completeSession");
        }
        if(session == null){
            throw new IllegalArgumentException("session with ID " + sessionId + " not found. PERSONAL SERVICE completeSession");
        }
        if (personal.getSubtasks() != null) {
            for (Subtask subtask : personal.getSubtasks()) {
                for (Session subSession : subtask.getSessions()) {
                    if (subSession.equals(session)) {
                        subSession.setState(SessionState.COMPLETED);
                        subtaskDAO.update(subtask);
                        break;
                    }
                }
            }
        }
        personal.completeSession(session);
        personalDAO.update(personal);
    }


    @Transactional
    public void completePersonalBySessions(long personalId) {
        Personal personal = personalDAO.findById(personalId);
        if (personal == null) return;
        personal.completeTaskBySessions();
        calendarDAO.update(personal.getUser().getCalendar());
        personalDAO.update(personal);
    }

    @Transactional
    public void forceCompletion(long personalId) {
        Personal personal = personalDAO.findById(personalId);
        if(personal == null) return;
        personal.forcedCompletion();
        personalDAO.update(personal);
        calendarDAO.update(personal.getUser().getCalendar());
        for(Session session : personal.getSessions()) {
            sessionDAO.update(session);
        }
    }

    @Transactional
    public void freezeTask(long personalId){
        Personal personal = personalDAO.findById(personalId);
        if (personal == null) {
            throw new IllegalArgumentException("Personal task with ID " + personalId + " not found.");
        }
        personal.handleLimitExceeded();
        personalDAO.update(personal);
        calendarDAO.update(personal.getUser().getCalendar());
    }

    @Transactional
    public void handleLimitExceeded(long sessionId, long personalId) {
        Personal personal = personalDAO.findById(personalId);
        Session session = sessionDAO.findById(sessionId);
        if (personal == null) {
            throw new IllegalArgumentException("Personal task with ID " + personalId + " not found.");
        }
        if (session == null) {
            throw new IllegalArgumentException("Session with ID " + personalId + " not found.");
        }
        boolean found = false;
        for (Session oldSession : personal.getSessions()) {
            if (session.equals(oldSession)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("session not found in task. CLASS PERSONALSERVICE handleLimit...");
        }
        personal.autoSkipIfNotCompleted(session);
        personalDAO.update(personal);
        calendarDAO.update(personal.getUser().getCalendar());
    }
}


