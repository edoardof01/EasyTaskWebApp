package service;

import domain.*;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import orm.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class PersonalService {
    @Inject
    private PersonalDAO personalDAO;

    @Inject
    CalendarDAO calendarDAO;

    @Inject
    private PersonalMapper personalMapper;

    @Inject
    private SessionDAO sessionDAO;

    @Inject
    private SessionMapper sessionMapper;

    @Inject
    UserDAO userDAO;



    public PersonalDTO getPersonalById(long id) {
        Personal personal = personalDAO.findById(id);
        if (personal == null) {
            throw new EntityNotFoundException("Personal with id " + id + " not found");
        }
        return personalMapper.toPersonalDTO(personal);
    }


    public List<PersonalDTO> getAllPersonal() {
        return personalDAO.findAll().stream()
                .map(personalMapper::toPersonalDTO)
                .toList();
    }

    public PersonalDTO createPersonal(String name, long userId, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                      Set<Timetable> timeSlots, Set<DefaultStrategy> strategies, int priority,
                                      String description, List<Resource> resources, @Nullable List<Subtask> subtasks, List<Session> sessions,
                                      @Nullable Integer requiredUsers, @Nullable String userGuidance) {

        if(subtasks != null){
            for(Subtask subtask:subtasks){
                if (subtask.getName()==null || subtask.getLevel()==null || subtask.getDescription()==null) throw new IllegalArgumentException("you must fill this fields");
            }
        }
        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("Mandatory fields missing or invalid fields");
        }

        if (requiredUsers != null || userGuidance != null) {
            throw new IllegalArgumentException("Users number can be set only for group tasks and teh userGuidance for shared tasks");
        }

        if (strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (deadline != null) {
                throw new IllegalArgumentException("If this strategy is set, a deadline can't be selected");
            }
        }
        if (deadline != null) {
            if (strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
                throw new IllegalArgumentException("If a deadline is set, this strategy can't be selected");
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
        // CONTROLLO SULLE SESSIONI
        validateSessions(sessions, timeSlots, totalTime);


        User existingUser = userDAO.findById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("User with userName " +userId+ " does not exist.");
        }
        Personal personalTask = new Personal(name, existingUser, topic, deadline, description, subtasks, sessions, 0, priority, timeSlots, totalTime, strategies, resources);
        personalTask.setComplexity(calculateComplexity(subtasks,resources));
        existingUser.getCalendar().addSessions(sessions);
        calendarDAO.update(existingUser.getCalendar());
        personalDAO.save(personalTask);
        userDAO.update(existingUser);
        return personalMapper.toPersonalDTO(personalTask);
    }

    private void validateSessions(List<Session> sessions, Set<Timetable> timeSlots, int totalTime) {
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

    private static boolean isWithinAllowedTimeSlot(Set<Timetable> timeSlots, Session session) {
        LocalTime sessionStartTime = session.getStartDate().toLocalTime();
        LocalTime sessionEndTime = session.getEndDate().toLocalTime();

        for (Timetable timeSlot : timeSlots) {
            if (isSessionInTimeSlot(sessionStartTime, sessionEndTime, timeSlot)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSessionInTimeSlot(LocalTime sessionStartTime, LocalTime sessionEndTime, Timetable timeSlot) {
        return switch (timeSlot) {
            case MORNING ->
                    sessionStartTime.isAfter(LocalTime.of(6, 0)) && sessionEndTime.isBefore(LocalTime.of(12, 0));
            case AFTERNOON ->
                    sessionStartTime.isAfter(LocalTime.of(12, 0)) && sessionEndTime.isBefore(LocalTime.of(18, 0));
            case EVENING ->
                    sessionStartTime.isAfter(LocalTime.of(18, 0)) && sessionEndTime.isBefore(LocalTime.of(23, 59));
            case NIGHT ->
                    (sessionStartTime.isAfter(LocalTime.of(0, 0)) && sessionEndTime.isBefore(LocalTime.of(6, 0))) ||
                            (sessionStartTime.isBefore(LocalTime.of(6, 0)) && sessionEndTime.isAfter(LocalTime.of(0, 0)));
        };
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

    public PersonalDTO modifyPersonal(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                    Set<Timetable> timeSlots, Set<DefaultStrategy> strategies, int priority, String description,
                                    List<Resource> resources,@Nullable List<Subtask> subtasks, List<Session> sessions, @Nullable Integer requiredUsers, @Nullable String userGuidance) {

        if(subtasks != null){
            for(Subtask subtask:subtasks){
                if (subtask.getName()==null || subtask.getLevel()==null || subtask.getDescription()==null) throw new IllegalArgumentException("you must fill this fields");
            }
        }
        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("Mandatory fields missing or invalid fields");
        }

        if (requiredUsers != null || userGuidance != null) {
            throw new IllegalArgumentException("Users number can be set only for group tasks and teh userGuidance for shared tasks");
        }

        if (strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (deadline != null) {
                throw new IllegalArgumentException("If this strategy is set, a deadline can't be selected");
            }
        }
        if (deadline != null) {
            if (strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
                throw new IllegalArgumentException("If a deadline is set, this strategy can't be selected");
            }
        }
        Personal personalTask = personalDAO.findById(taskId);
        if (personalTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }

        User user = personalTask.getUser();
        if (user == null) {
            throw new IllegalStateException("no user associated to this task");
        }
        personalTask.modifyTask();

        personalTask.setName(name);
        personalTask.setTopic(topic);
        if (deadline != null) personalTask.setDeadline(deadline);
        personalTask.setTotalTime(totalTime);
        personalTask.setTimetable(timeSlots);
        personalTask.setPriority(priority);
        personalTask.setStrategies(strategies);
        if (description != null) personalTask.setDescription(description);
        if (resources != null) {
            personalTask.getResources().clear();
            personalTask.getResources().addAll(resources);
        }
        if (subtasks!=null && !compareSubtasks(subtasks,personalTask.getSubtasks())){
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
        if(sessions!=null && compareSessions(sessions,personalTask.getSessions())){
            personalTask.getSessions().clear();
            personalTask.getSessions().addAll(sessions);
            validateSessions(personalTask.getSessions(),timeSlots,totalTime);
        }
        int complexity = calculateComplexity(subtasks, resources);
        personalTask.setComplexity(complexity);
        calendarDAO.update(personalTask.getUser().getCalendar());
        personalDAO.update(personalTask);
        return personalMapper.toPersonalDTO(personalTask);
    }

    public boolean compareSubtasks(List<Subtask> subtasks, List<Subtask> personalTaskSubtasks) {
        subtasks.sort(Comparator.comparing(Subtask::getName));
        personalTaskSubtasks.sort(Comparator.comparing(Subtask::getName));
        return subtasks.equals(personalTaskSubtasks);
    }

    public boolean compareSessions(List<Session> sessions, List<Session> personalTaskSessions) {
        // Ordina la lista delle sessioni in base a startDate
        sessions.sort(Comparator.comparing(Session::getStartDate));
        personalTaskSessions.sort(Comparator.comparing(Session::getStartDate));

        // Confronta le due liste ordinate
        return sessions.equals(personalTaskSessions);
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
    }

    @Transactional
    public void completeSession(long personalId, long sessionId) {
        Personal personal = personalDAO.findById(personalId);
        Session session = sessionDAO.findById(sessionId);
        if (personal == null || session == null) return;
        personal.completeSession(session);
        sessionDAO.update(session);
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
    public void handleLimitExceeded(SessionDTO sessionDTO, long personalId) {
        Personal personal = personalDAO.findById(personalId);
        if (personal == null) {
            throw new IllegalArgumentException("Personal task with ID " + personalId + " not found.");
        }
        Session session = sessionMapper.toSessionEntity(sessionDTO);
        if (session == null) {
            throw new IllegalArgumentException("Session with ID " + personalId + " not found.");
        }
        personal.autoSkipIfNotCompleted(session);
        sessionDAO.update(session);
        personalDAO.update(personal);
        calendarDAO.update(personal.getUser().getCalendar());
    }
}


