package service;

import domain.*;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import orm.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupService {

    @Inject
    GroupDAO groupDAO;

    @Inject
    GroupMapper groupMapper;

    @Inject
    RequestMapper requestMapper;

    @Inject
    UserDAO userDAO;

    @Inject
    SessionDAO sessionDAO;

    @Inject
    CalendarDAO calendarDAO;

    @Inject
    SubtaskDAO subtaskDAO;

    @Inject
    TaskCalendarDAO taskCalendarDAO;

    @Inject
    TakenSubtaskDAO takenSubtaskDAO;

    @Inject
    RequestDAO requestDAO;


    public GroupDTO getGroupById(long id) {
        Group group = groupDAO.findById(id);
        if (group == null) {
            throw new EntityNotFoundException("Group" + id + "not found");
        }
        return groupMapper.toGroupDTO(group);
    }

    public List<GroupDTO> getAllGroups() {
        List<Group> groups = groupDAO.findAll();
        return groups.stream().
                map(groupMapper::toGroupDTO).
                toList();
    }

    public GroupDTO createGroup(String name, long userId, Topic topic, @Nullable LocalDateTime deadline, LocalDateTime dateOnFeed, int totalTime,
                                Timetable timeSlots, List<StrategyInstance> strategies, int priority,
                                String description, List<Resource> resources, @NotNull List<Subtask> subtasks, Subtask chosenSubtask, List<Session> sessions,
                                @Nullable String userGuidance, Integer requiredUsers) {

        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("mandatory fields missing or invalid fields");
        }
        if(requiredUsers != subtasks.size()){
            throw new IllegalArgumentException("there must be  " + requiredUsers + " required subtasks");
        }
        if (userGuidance != null) {
            throw new IllegalArgumentException("User guidance can be set only for shared tasks");
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (deadline != null) {
                throw new IllegalArgumentException("If this strategy is set, a deadline can't be selected");
            }
        }
        if (deadline != null) {
            if (strategies.stream().anyMatch(strategy ->
                    strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
                throw new IllegalArgumentException("If a deadline is set, this strategy can't be selected");
            }
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS)) {
            if (strategies.size() > 1) {
                for (StrategyInstance strategy : strategies) {
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS ||
                            strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING) {
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS)) {
            if (strategies.size() > 1) {
                for (StrategyInstance strategy : strategies) {
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS ||
                            strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING) {
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }


        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with userId " + userId + " does not exist.");
        }
        if (!sessions.isEmpty()) {
            for (Session newSession : sessions) {
                // Verifica se la nuova sessione si sovrappone a quelle già nel calendario
                for (Session existingSession : user.getCalendar().getSessions()) {
                    if (newSession.overlaps(existingSession)) {
                        throw new IllegalArgumentException("Session " + newSession + " overlaps with an existing session. CLASS SHARED GROUP createShared");
                    }
                }
            }
        }

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

        // CONTROLLO SESSIONI SUBTASK
        if (!subtasks.isEmpty()) {
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

        validateSessions(sessions, timeSlots, totalTime);

        Group group = new Group(requiredUsers, user, dateOnFeed, name, topic, deadline, description, subtasks, sessions, 0, priority, timeSlots,
                totalTime, strategies, resources);

        for(Subtask subtask : subtasks) {
            if(subtask.equals(chosenSubtask)) {
                group.assignSubtaskToUser(user, subtask);
            }
        }


        group.setComplexity(calculateComplexity(subtasks, resources));
        Feed.getInstance().getGroup().add(group);
        for (User member : group.getMembers()) Feed.getInstance().getContributors().add(member);
        taskCalendarDAO.save(group.getCalendar());
        groupDAO.save(group);
        userDAO.update(user);

        return groupMapper.toGroupDTO(group);
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
        int subtaskScore;
        assert subtasks != null;
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
                        resource.setValue(resource.calculateValueFromMoney()); // NON SONO SICUROOOOOOOOOOOOOOOO
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


    public GroupDTO modifyGroup(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                Timetable timeSlots, List<StrategyInstance> strategies, int priority, String description,
                                List<Resource> resources, List<Subtask> subtasks, List<Session> sessions, Integer numUsers) {

        if (numUsers == null) {
            throw new IllegalArgumentException("numUser must be passed");
        }
        if (numUsers <= 0) {
            throw new IllegalArgumentException("numUser must be greater than 0");
        }
        if(subtasks==null){
            throw new IllegalArgumentException("subtasks must be passed");
        }
        for (Subtask subtask : subtasks) {
            if (subtask.getName() == null || subtask.getLevel() == null || subtask.getDescription() == null)
                throw new IllegalArgumentException("you must fill this fields");
        }
        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("mandatory fields missing or invalid fields");
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (deadline != null) {
                throw new IllegalArgumentException("If this strategy is set, a deadline can't be selected");
            }
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (strategies.size() > 1) {
                throw new IllegalArgumentException("If this strategy is set, an other strategy can't be selected");
            }
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)) {
            if (strategies.size() > 1) {
                throw new IllegalArgumentException("If this strategy is set, an other strategy can't be selected");
            }
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS)) {
            if (strategies.size() > 1) {
                for (StrategyInstance strategy : strategies) {
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS ||
                            strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING) {
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }
        if (strategies.stream().anyMatch(strategy ->
                strategy.getStrategy() == DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS)) {
            if (strategies.size() > 1) {
                for (StrategyInstance strategy : strategies) {
                    if (strategy.getStrategy() == DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS ||
                            strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING) {
                        throw new IllegalArgumentException("If this strategy is set, just an other type strategy can be selected");
                    }
                }
            }
        }

        if (deadline != null) {
            if (strategies.stream().anyMatch(strategy ->
                    strategy.getStrategy() == DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
                throw new IllegalArgumentException("If a deadline is set, this strategy can't be selected");
            }
        }
        Group group = groupDAO.findById(taskId);
        if (group == null) {
            throw new IllegalArgumentException("Task con ID " + taskId + " non trovato.");
        }

        if (numUsers < group.getNumUsers()) {
            throw new IllegalArgumentException("the new numUsers must be more than or equal to the old numUsers");
        }
        else if(numUsers > group.getNumUsers()) {
            if(numUsers!= subtasks.size()){
                throw new IllegalArgumentException("the new numUser must be still equals to the number of subtasks");
            }
        }

        User user = group.getUser();
        if (user == null) {
            throw new IllegalStateException("Nessun utente associato al task.");
        }


        // Sincronizza le sessioni tra il task e il calendario
        List<Session> existingSessions = new ArrayList<>(group.getSessions());

        // Rimuovi le sessioni non più presenti
        for (Session existing : existingSessions) {
            // Verifica se la sessione non è più presente nel nuovo elenco di sessioni
            boolean sessionExistsInNewList = sessions.stream().anyMatch(session -> session.equals(existing));
            if (!sessionExistsInNewList) {
                group.getSessions().remove(existing);
            }
        }

        // Aggiungi nuove sessioni, se non già presenti
        for (Session session : sessions) {
            // Verifica se la sessione non è già presente nel task
            boolean sessionExistsInTask = group.getSessions().stream().noneMatch(taskSession -> taskSession.equals(session));
            if (sessionExistsInTask) {
                group.getSessions().add(session);
            }
        }

        if (!sessions.isEmpty()) {
            for (Session newSession : sessions) {
                for (User member : group.getMembers()) {
                    // Verifica se la nuova sessione si sovrappone a quelle già nel calendario
                    for (Session existingSession : member.getCalendar().getSessions()) {
                        if (newSession.overlaps(existingSession)) {
                            throw new IllegalArgumentException("Session " + newSession + " overlaps with an existing session. CLASS GROUP SERVICE (modify)");
                        }
                    }
                }
            }
        }
       /* for(Subtask subtask: subtasks){
            subtaskDAO.save(subtask);
        }

        group.modifyTask();*/

        if (numUsers > group.getNumUsers()) {
            Feed.getInstance().getGroup().add(group); /// INCOMPLETOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO ????
            group.setNumUsers(numUsers);
            group.setIsComplete(false);
        }

        for (User member : group.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
        group.setName(name);
        group.setTopic(topic);
        group.getStrategies().clear(); // Rimuove tutti gli elementi esistenti
        group.getStrategies().addAll(strategies); // Aggiunge i nuovi elementi
        if (deadline != null) group.setDeadline(deadline);
        group.setTotalTime(totalTime);
        group.setTimetable(timeSlots);
        group.setPriority(priority);
        if (description != null) group.setDescription(description);
        if (resources != null) {
            group.getResources().clear(); // Rimuove tutti gli elementi esistenti
            group.getResources().addAll(resources); // Aggiunge i nuovi elementi
        }
        group.setNumUsers(numUsers);
        group.setComplexity(group.calculateComplexity());

        Set<Session> allAssignedSessions = new HashSet<>();
        // Distribuisci le sessioni tra i subtasks
        for (Subtask subtask : subtasks) {
            List<Session> assignedSessions = subtask.getSessions();

            // Gestisci sessioni preesistenti nel subtask
            List<Session> reconciledSessions = new ArrayList<>();
            for (Session session : assignedSessions) {
                // Verifica se la sessione è presente nel task principale
                boolean sessionExistsInTask = group.getSessions().stream().anyMatch(taskSession -> taskSession.equals(session));
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
        if (!allAssignedSessions.containsAll(group.getSessions())) {
            throw new IllegalArgumentException("Not all sessions from the main task have been assigned to subtasks");
        }
        // Aggiorna i subtasks del task
        group.getSubtasks().clear();
        group.getSubtasks().addAll(subtasks);

        for(Subtask subtask: subtasks){
            subtaskDAO.save(subtask);
        }
        groupDAO.update(group);

        group.modifyTask();

        if (!compareSubtasks(subtasks, group.getSubtasks())) {
            // Mappe di supporto per la gestione delle risorse
            Map<Resource, Boolean> resourceUsage = new HashMap<>();
            Map<String, Resource> resourceMap = new HashMap<>();
            int totalMoney = 0;

            // Inizializza le mappe delle risorse
            assert resources != null;
            for (Resource resource : resources) {
                resourceMap.put(resource.getName(), resource);
                if (resource.getType() == ResourceType.COMPETENCE || resource.getType() == ResourceType.EQUIPMENT) {
                    resourceUsage.put(resource, false); // Risorsa non ancora usata
                }
            }

            // Valida e processa i nuovi subtasks
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
                        resourceUsage.put(mainTaskResource, true); // Marca la risorsa come usata
                    } else if (!resources.contains(resource)) {
                        throw new IllegalArgumentException("Subtasks can't contain resource " + resource.getName());
                    }
                }
            }
        }
        groupDAO.update(group);
        return groupMapper.toGroupDTO(group);
    }


    public boolean compareSubtasks(List<Subtask> subtasks, List<Subtask> groupTaskSubtasks) {
        subtasks.sort(Comparator.comparing(Subtask::getName));
        groupTaskSubtasks.sort(Comparator.comparing(Subtask::getName));
        return subtasks.equals(groupTaskSubtasks);
    }

    @Transactional
    public void deleteGroup (Long taskId){
        Group groupTask = groupDAO.findById(taskId);
        if (groupTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }
        groupTask.deleteTask();
        groupDAO.delete(groupTask.getId());
        for (User member : groupTask.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
    }

    @Transactional
    public void moveToCalendar (long groupId, Long userId){

        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Task con ID " + userId + " non trovato.");
        }
        if(!group.getIsComplete()){
            throw new IllegalArgumentException("The group task you wanna schedule in the calendar is not complete. GROUP SERVICE, moveToCalendar");
        }
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User con ID " + userId + " non trovato.");
        }
        if (group.getUser().equals(user)) {
            group.toCalendar(); // Metodo per l'amministratore
            for(User member: group.getMembers()){
                calendarDAO.update(member.getCalendar());
            }
        } else {
            throw new IllegalArgumentException("the user must be the ADMIN");
        }
        groupDAO.update(group);
    }


    @Transactional
    public void completeSharedBySessions (long groupId){
        Group group = groupDAO.findById(groupId);
        group.completeTaskBySessions();
        for(User member : group.getMembers()) {
            userDAO.update(member);
        }
        groupDAO.update(group);
        for (User member : group.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
    }

    @Transactional
    public void forceCompletion ( long groupId, long userId){
        User user = userDAO.findById(userId);
        Group group = groupDAO.findById(groupId);
        if(user == null){
            throw new IllegalArgumentException("The user doesn't exist.");
        }
        if(group == null){
            throw new IllegalArgumentException("The group doesn't exist.");
        }
        if (user != group.getUser()) {
            throw new IllegalArgumentException("a member can't do this operation.");
        }
        group.forcedCompletion();
        groupDAO.update(group);
        calendarDAO.update(group.getUser().getCalendar());
        for (Session session : group.getSessions()) {
            sessionDAO.update(session);
        }
    }

    @Transactional
    public void completeSession ( long groupId, long sessionId){
        Group group = groupDAO.findById(groupId);
        Session session = sessionDAO.findById(sessionId);
        if (group == null || session == null) return;
        group.completeSession(session);
        sessionDAO.update(session);
        groupDAO.update(group);
    }

    @Transactional
    public void completeSubtaskSession(long userId,long groupId,long subtaskId,long sessionId){
        Group group = groupDAO.findById(groupId);
        if(group == null) throw new IllegalArgumentException("Group not found.");
        User user = userDAO.findById(userId);
        if(user==null){
            throw new IllegalArgumentException("User not found.");
        }
        boolean found = false;
        for(User member: group.getMembers()){
            if(user.equals(member)){
                found = true;
                break;
            }
        }
        if(!found){
            throw new IllegalArgumentException("User not member of the group. GROUP SERVICE completeSubtaskSession");
        }
        if(group.getUser().equals(user)){
            throw new IllegalArgumentException("User must be a user member not an admin. GROUP SERVICE completeSubtaskSession");
        }
        Subtask subtask = subtaskDAO.findById(subtaskId);
        if(subtask == null) throw new IllegalArgumentException("Subtask not found.");

        boolean isSubtaskFounded = false;
        for(Subtask oldSubtask:group.getSubtasks()){
            if (subtask.equals(oldSubtask)) {
                isSubtaskFounded = true;
                break;
            }
        }
        if(!isSubtaskFounded){
            throw new IllegalArgumentException("Subtask not in the group. CLASS GROUP SERVICE completeSubtaskSession");
        }
        Session session = sessionDAO.findById(sessionId);
        if(session==null){
            throw new IllegalArgumentException("Session not found.");
        }
        boolean isSessionFounded = false;
        for(Session oldSession: subtask.getSessions()){
            if (oldSession.equals(session)) {
                isSessionFounded = true;
                break;
            }
        }
        if(!isSessionFounded){
            throw new IllegalArgumentException("Session not in the group. CLASS GROUP SERVICE completeSubtaskSession");
        }
        group.completeSubtaskSession(session);
        for(User member: group.getMembers()){
            userDAO.update(member);
            calendarDAO.update(member.getCalendar());
        }
        groupDAO.update(group);
    }

    @Transactional
    public void handleLimitExceeded(long sessionId, long groupId) {
        Group group = groupDAO.findById(groupId);
        Session session = sessionDAO.findById(sessionId);
        if (group == null) {
            throw new IllegalArgumentException("Personal task with ID " + groupId + " not found.");
        }
        if (session == null) {
            throw new IllegalArgumentException("Session with ID " + groupId + " not found.");
        }
        boolean found = false;
        for (Session oldSession : group.getSessions()) {
            if (session.equals(oldSession)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("session not found in task. CLASS PERSONALSERVICE handleLimit...");
        }
        group.autoSkipIfNotCompleted(session);
        groupDAO.update(group);
        calendarDAO.update(group.getUser().getCalendar());
    }


    @Transactional
    public GroupDTO joinGroup (long userId, long groupId, long subtaskId){
        // Trova il gruppo a cui si sta tentando di unire
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        // Trova l'utente che vuole unirsi al gruppo
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        if (group.getMembers().contains(user)) {
            throw new IllegalArgumentException("the user is already a member of this group.");
        }
        // Trova il subtask a cui l'utente sarà assegnato
        Subtask subtask = subtaskDAO.findById(subtaskId);
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask with ID " + subtaskId + " not found.");
        }
        for(TakenSubtask otherSubtask: group.getTakenSubtasks()){
            if(otherSubtask.getSubtask().equals(subtask)){
                throw new IllegalArgumentException("the subtask is already taken by a member of this group.");
            }
        }
        group.joinGroup(user, subtask);
        subtaskDAO.update(subtask);
        groupDAO.update(group);
        userDAO.update(user);
        // Restituisci il DTO del gruppo aggiornato
        return groupMapper.toGroupDTO(group);
    }


    @Transactional
    public void leaveGroup ( long groupId, long userId){
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        User user = userDAO.findById(userId);
        if (user == null || !group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User with ID " + userId + " not found or not present in the group.");
        }
        if(group.getUser()==user){
            throw new IllegalArgumentException("User must be a user member not an admin. ClASS GROUP SERVICE leaveGroup");
        }
        group.leaveGroupTask(user);
        groupDAO.update(group);
        userDAO.update(user);
        for (User member : group.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
    }


    @Transactional
    public void sendExchangeRequest ( long groupId, long senderId, long receiverId){
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        User sender = userDAO.findById(senderId);
        User receiver = userDAO.findById(receiverId);
        if (!new HashSet<>(group.getMembers()).containsAll(List.of(new User[]{sender, receiver}))) {
            throw new IllegalArgumentException("User with ID " + senderId + " and/or " + receiverId + "not in member list.");
        }
        group.sendExchangeRequest(sender, receiver);
    }

    @Transactional
    public List<RequestDTO> getPendingExchangeRequests(long userId) {
        User user = userDAO.findById(userId);
        if(user==null){
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        return user.getPendingRequests()
                .stream()
                .map(requestMapper::toRequestDTO).collect(Collectors.toList());
    }


    @Transactional
    public void processExchangeRequest(long groupId, long requestId, boolean accept) {
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }

        Request request = requestDAO.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request with ID " + requestId + " not found.");
        }

        if (!request.getGroup().equals(group)) {
            throw new IllegalArgumentException("The request does not belong to the group.");
        }

        User receiver = request.getReceiver();
        group.processExchangeRequest(receiver, request, accept);
        groupDAO.update(group);  // Sincronizza il contesto di persistenza
        for (TakenSubtask takenSubtask : group.getTakenSubtasks()) {
            takenSubtaskDAO.update(takenSubtask);  // Sincronizza il contesto di persistenza
        }

        userDAO.update(receiver);  // Sincronizza il contesto di persistenza
        userDAO.update(request.getSender());  // Sincronizza il contesto di persistenza

        if (accept) {
            calendarDAO.update(request.getSender().getCalendar());  // Sincronizza il contesto di persistenza
            calendarDAO.update(receiver.getCalendar());  // Sincronizza il contesto di persistenza
            group.addTakenSubtasksAndSessions(receiver, request);
            groupDAO.update(group);  // Sincronizza il contesto di persistenza
            calendarDAO.update(request.getSender().getCalendar());  // Sincronizza il contesto di persistenza
            calendarDAO.update(receiver.getCalendar());  // Sincronizza il contesto di persistenza
        }
    }


    public void removeMemberFromGroup (long groupId,long adminId, long userId, boolean substitute){
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        User admin = userDAO.findById(adminId);
        User user = userDAO.findById(userId);
        if (group.getUser().equals(user)) {
            throw new IllegalArgumentException("it seems to be two admins");
        }
        if (!group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        group.removeMember(admin,user,substitute);
        groupDAO.update(group);
        userDAO.update(admin);
        userDAO.update(user);
        calendarDAO.update(user.getCalendar());
    }

    }






