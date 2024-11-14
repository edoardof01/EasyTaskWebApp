package orm;

import domain.*;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PersonalService {
    @Inject
    private PersonalDAO personalDAO;

    @Inject
    private PersonalMapper personalMapper;

    @Inject
    private SessionDAO sessionDAO;

    @Inject
    private SessionMapper sessionMapper;

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

    public PersonalDTO createPersonal(String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                      Set<Timetable> timeSlots, Set<DefaultStrategy> strategies, int priority,
                                      String description, ArrayList<Resource> resources, @Nullable ArrayList<Subtask> subtasks,
                                      @Nullable Integer requiredUsers, @Nullable String userGuidance) {
        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("mandatory fields missing or invalid fields");
        }

        if (requiredUsers != null || userGuidance != null) {
            throw new IllegalArgumentException("Users number can be set only for group tasks");
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
        int complexity = calculateComplexity(subtasks, resources);

        Personal personalTask = new Personal(name, topic, TaskState.TODO, deadline, description,
                0, complexity, priority, timeSlots, totalTime, strategies, resources);


        personalDAO.save(personalTask);

        return personalMapper.toPersonalDTO(personalTask);
    }

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

    public PersonalDTO modifyShared(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                    Set<Timetable> timeSlots, DefaultStrategy strategy, int priority, String description,
                                    ArrayList<Resource> resources, List<Subtask> subtasks) {

        Personal personalTask = personalDAO.findById(taskId);
        if (personalTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }

        User user = personalTask.getUser();
        if (user == null) {
            throw new IllegalStateException("no user associated to this task");
        }

        personalTask.setState(TaskState.FREEZED);
        personalTask.modifyTask();

        if (name != null) personalTask.setName(name);
        if (topic != null) personalTask.setTopic(topic);
        if (deadline != null) personalTask.setDeadline(deadline);
        personalTask.setTotalTime(totalTime);
        if (timeSlots != null) personalTask.setTimetable(timeSlots);
        personalTask.setPriority(priority);
        if (description != null) personalTask.setDescription(description);
        if (resources != null) personalTask.setResources(resources);

        int complexity = calculateComplexity(subtasks, resources);
        personalTask.setComplexity(complexity);

        personalDAO.update(personalTask);

        return personalMapper.toPersonalDTO(personalTask);
    }

    @Transactional
    public void deleteShared(Long taskId) {
        Personal personalTask = personalDAO.findById(taskId);
        if (personalTask == null) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }

        personalTask.deleteTask();
        personalDAO.delete(personalTask.getId());
    }

    @Transactional
    public void moveToCalendar(PersonalDTO personalDTO) {
        Personal personal = personalDAO.findById(personalDTO.getId());
        if (personal == null) {
            throw new IllegalArgumentException("Task with ID " + personalDTO.getId() + " not found.");
        }
        personal.toCalendar();
        personalDAO.update(personal);
    }

    @Transactional
    public void completePersonalBySessions(long personalId) {
        Personal personal = personalDAO.findById(personalId);
        personal.completeTaskBySessions();
        personalDAO.update(personal);
    }

    @Transactional
    public void handleLimitExceeded(SessionDTO sessionDTO, long personalId) {
        Personal personal = personalDAO.findById(personalId);
        if (personal == null) {
            throw new IllegalArgumentException("Personal task with ID " + personalId + " not found.");

            Session session = sessionMapper.toSessionEntity(sessionDTO);
            if (session == null) {
                throw new IllegalArgumentException("Session with ID " + personalId + " not found.");
            }
            personal.autoSkipIfNotCompleted(session);
            sessionDAO.update(session);
            personalDAO.update(personal);
        }

    }
}

