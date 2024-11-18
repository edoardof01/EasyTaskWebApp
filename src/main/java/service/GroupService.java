package service;

import domain.*;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import orm.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class GroupService {

    @Inject
    GroupDAO groupDAO;

    @Inject
    GroupMapper groupMapper;

    @Inject
    UserDAO userDAO;

    @Inject
    SessionDAO sessionDAO;

    @Inject
    SessionMapper sessionMapper;

    @Inject
    RequestDAO requestDAO;

    @Inject
    CalendarDAO calendarDAO;

    @Inject
    SubtaskDAO subtaskDAO;


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

    public GroupDTO createGroup(String name, User user, Topic topic, @Nullable LocalDateTime deadline, LocalDateTime dateOnFeed, int totalTime,
                                Set<Timetable> timeSlots, Set<DefaultStrategy> strategies, int priority,
                                String description, List<Resource> resources, @Nullable List<Subtask> subtasks,
                                int numUsers, @Nullable String userGuidance) {
        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null || numUsers <= 0) {
            throw new IllegalArgumentException("mandatory fields missing or invalid fields");
        }
        if (userGuidance != null) {
            throw new IllegalArgumentException("UsersGuidance can be written only for shared tasks");
        }
        if (strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
            if (deadline != null) {
                throw new IllegalArgumentException("if this strategy is set, a deadline can't be selected");
            }
        }
        if (deadline != null) {
            if (strategies.contains(DefaultStrategy.IF_THE_EXPIRATION_DATE_IS_NOT_SET_EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)) {
                throw new IllegalArgumentException("if a deadline is set, this strategy can't be selected");
            }
        }

        if (subtasks != null) {
            int totalMoney = 0;
            for (Subtask subtask : subtasks) {
                for (Resource resource : subtask.getResources()) {
                    if (resource.getType() != ResourceType.MONEY) {
                        if (!resources.contains(resource)) {
                            throw new IllegalArgumentException("subtasks can't contain resource " + resource);
                        }
                    } else {
                        totalMoney += resource.getMoney();
                        Optional<Resource> moneyResource = resources.stream()
                                .filter(resourceMoney -> resourceMoney.getMoney() != null)
                                .findFirst();
                        if (moneyResource.isEmpty()) {
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

        Group group = new Group(numUsers, user, dateOnFeed, name, topic, TaskState.TODO, deadline, description, 0, priority, timeSlots,
                totalTime, strategies, resources);
        user.setUserRole(Role.ADMIN);
        Feed.getInstance().getGroup().add(group);
        for(User member : group.getMembers())  Feed.getInstance().getContributors().add(member);
        groupDAO.save(group);
        userDAO.update(user);
        return groupMapper.toGroupDTO(group);
    }


    public GroupDTO modifyGroup(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                Set<Timetable> timeSlots, Set<DefaultStrategy> strategy, int priority, String description,
                                List<Resource> resources, List<Subtask> subtasks, Integer numUsers) {

        Group group = groupDAO.findById(taskId);
        if (group == null) {
            throw new IllegalArgumentException("Task con ID " + taskId + " non trovato.");
        }
        if(numUsers!=null) {
            if (numUsers < group.getNumUsers()) {
                throw new IllegalArgumentException("the new numUsers must be more than or equal to the old numUsers");
            }
        }

        User user = group.getUser();
        if (user == null) {
            throw new IllegalStateException("Nessun utente associato al task.");
        }

        group.setState(TaskState.FREEZED);
        group.modifyTask();
        for (User member : group.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
        if (name != null) group.setName(name);
        if (topic != null) group.setTopic(topic);
        if(strategy != null) group.setStrategies(strategy);
        if (deadline != null) group.setDeadline(deadline);
        group.setTotalTime(totalTime);
        if (timeSlots != null) group.setTimetable(timeSlots);
        group.setPriority(priority);
        if (description != null) group.setDescription(description);
        if (resources != null) group.setResources(resources);
        if (numUsers != null) group.setNumUsers(numUsers);
        if (subtasks != null)  group.setSubtasks(subtasks);



        group.setComplexity(group.calculateComplexity());

        Feed.getInstance().getGroup().add(group);
        for(User member: group.getMembers()) {
            Feed.getInstance().getContributors().add(member);
        }
        groupDAO.update(group);

        return groupMapper.toGroupDTO(group);
    }

    @Transactional
    public void deleteGroup(Long taskId) {
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
    public void moveToCalendar(GroupDTO groupDTO, Long userId) {
        Group group = groupDAO.findById(groupDTO.getId());
        if (group == null) {
            throw new IllegalArgumentException("Task con ID " + groupDTO.getId() + " non trovato.");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User con ID " + userId + " non trovato.");
        }

        // Verifica se l'utente è l'amministratore del gruppo
        if (group.getUser().equals(user) && user.getUserRole() == Role.ADMIN) {
            group.toCalendar(); // Metodo per l'amministratore
            calendarDAO.update(group.getUser().getCalendar());
        } else if (group.getMembers().contains(user)) {
            group.toCalendarForUser(user); // Metodo per altri membri
            calendarDAO.update(user.getCalendar());
        } else {
            throw new IllegalArgumentException("the user is not a member of this group .");
        }

        groupDAO.update(group);
    }


    @Transactional
    public void completeSharedBySessions(long groupId) {
        Group group = groupDAO.findById(groupId);
        group.completeTaskBySessions();
        groupDAO.update(group);
        for (User member : group.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
    }

    @Transactional
    public void forceCompletion(long groupId,long userId) {
        User user = userDAO.findById(userId);
        Group group = groupDAO.findById(groupId);
        if(user.getUserRole() != Role.ADMIN) {
            throw new IllegalArgumentException("a member can't do this operation.");
        }
        if(group == null) return;
        group.forcedCompletion();
        groupDAO.update(group);
        calendarDAO.update(group.getUser().getCalendar());
        for(Session session : group.getSessions()) {
            sessionDAO.update(session);
        }
    }

    @Transactional
    public void completeSession(long groupId, long sessionId) {
        Group group = groupDAO.findById(groupId);
        Session session = sessionDAO.findById(sessionId);
        if (group == null || session == null) return;
        group.completeSession(session);
        sessionDAO.update(session);
        groupDAO.update(group);
    }


    @Transactional
    public void handleLimitExceeded(SessionDTO sessionDTO, long groupId) {
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group task with ID " + groupId + " not found.");
        }
        Session session = sessionMapper.toSessionEntity(sessionDTO);
        if (session == null) {
            throw new IllegalArgumentException("Session with ID " + groupId + " not found.");
        }
        group.autoSkipIfNotCompleted(session);
        sessionDAO.update(session);
        groupDAO.update(group);
        for (User member : group.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
    }

    @Transactional
    public GroupDTO joinGroup(long userId, long groupId, long subtaskId) {
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
        if(group.getMembers().contains(user)) {
            throw new IllegalArgumentException("the user is already a member of this group.");
        }
        // Trova il subtask a cui l'utente sarà assegnato
        Subtask subtask = subtaskDAO.findById(subtaskId);
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask with ID " + subtaskId + " not found.");
        }
        group.joinGroup(user, subtask);

        subtaskDAO.update(subtask);
        groupDAO.update(group);
        userDAO.update(user);
        // Restituisci il DTO del gruppo aggiornato
        return groupMapper.toGroupDTO(group);
    }


    @Transactional
    public void leaveGroup(long groupId, long userId) {
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        User user = userDAO.findById(userId);
        if (user == null || !group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User with ID " + userId + " not found or not present in the group.");
        }
        group.leaveGroupTask(user);
        groupDAO.update(group);
        userDAO.update(user);
        for (User member : group.getMembers()) {
            calendarDAO.update(member.getCalendar());
        }
    }


    @Transactional
    public void sendExchangeRequest(long groupId, long senderId, long receiverId) {
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        User sender = userDAO.findById(senderId);
        User receiver = userDAO.findById(receiverId);
        if (!group.getMembers().containsAll(List.of(new User[]{sender, receiver}))) {
            throw new IllegalArgumentException("User with ID " + senderId + " and/or " + receiverId + "not in member list.");
        }
        group.sendExchangeRequest(sender, receiver);
    }

    @Transactional
    public void processExchangeRequest(long groupId, long receiverId, long requestId, boolean accept) {
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }

        User receiver = userDAO.findById(receiverId);
        if (receiver == null) {
            throw new IllegalArgumentException("User with ID " + receiverId + " not found.");
        }
        Request request = requestDAO.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request data is missing.");
        }
        group.processExchangeRequest(receiver, request, accept);
        groupDAO.update(group);
        if (accept) {
            calendarDAO.update(request.getSender().getCalendar());
            calendarDAO.update(receiver.getCalendar());
        }

    }

    public void removeMemberFromGroup(long groupId, long userId, boolean substitute) {
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        User user = userDAO.findById(userId);
        if(user.getUserRole() != Role.ADMIN) {
            throw new IllegalArgumentException("a member can't do this operation.");
        }
        if (!group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        group.removeMember(user, substitute);
        groupDAO.update(group);
        userDAO.update(user);
        calendarDAO.update(user.getCalendar());
    }

}





