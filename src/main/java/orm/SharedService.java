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

@ApplicationScoped
public class SharedService {

    @Inject
    private SharedDAO sharedDAO;

    @Inject
    private SharedMapper sharedMapper;

    @Inject
    private SessionDAO sessionDAO;

    @Inject
    private SessionMapper sessionMapper;

    @Inject
    private CommentDAO commentDAO;
    @Inject
    private CommentMapper commentMapper;

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

    @Transactional
    public void addTaskToFeed(Shared sharedTask, String guidance) {
        sharedTask.updateUserGuidance(guidance);
        sharedTask.setState(TaskState.INPROGRESS);
        Feed.getInstance().addTask(sharedTask);
        sharedDAO.update(sharedTask);
    }

    public SharedDTO createShared(String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                  Set<Timetable> timeSlots, Set<DefaultStrategy> strategies, int priority,
                                  String description, ArrayList<Resource> resources, @Nullable ArrayList<Subtask> subtasks,
                                  @Nullable Integer requiredUsers, @Nullable String userGuidance) {

        if (name == null || topic == null || totalTime <= 0 || timeSlots == null || strategies == null) {
            throw new IllegalArgumentException("mandatory fields missing or invalid fields");
        }

        if (requiredUsers != null) {
            throw new IllegalArgumentException("Users number can be set only for group tasks");
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
                    if(resource.getType() != ResourceType.MONEY){
                        if(!resources.contains(resource)){
                            throw new IllegalArgumentException("subtasks can't contain resource " + resource);
                        }
                    } else {
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

        Shared sharedTask = new Shared(name, topic, TaskState.INPROGRESS, deadline, description,
                0, complexity, priority, timeSlots, totalTime, strategies, resources);

        if (userGuidance != null) {
            sharedTask.updateUserGuidance(userGuidance);
        }

        Feed.getInstance().addTask(sharedTask);
        sharedDAO.save(sharedTask);

        return sharedMapper.toSharedDTO(sharedTask);
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

    public SharedDTO modifyShared(Long taskId, String name, Topic topic, @Nullable LocalDateTime deadline, int totalTime,
                                  Set<Timetable> timeSlots, DefaultStrategy strategy, int priority, String description,
                                  ArrayList<Resource> resources, List<Subtask> subtasks, @Nullable String userGuidance) {

        Shared sharedTask = sharedDAO.findById(taskId);
        if (sharedTask == null) {
            throw new IllegalArgumentException("Task con ID " + taskId + " non trovato.");
        }

        User user = sharedTask.getUser();
        if (user == null) {
            throw new IllegalStateException("Nessun utente associato al task.");
        }

        sharedTask.setState(TaskState.FREEZED);
        sharedTask.modifyTask();

        if (name != null) sharedTask.setName(name);
        if (topic != null) sharedTask.setTopic(topic);
        if (deadline != null) sharedTask.setDeadline(deadline);
        sharedTask.setTotalTime(totalTime);
        if (timeSlots != null) sharedTask.setTimetable(timeSlots);
        sharedTask.setPriority(priority);
        if (description != null) sharedTask.setDescription(description);
        if (resources != null) sharedTask.setResources(resources);

        int complexity = calculateComplexity(subtasks, resources);
        sharedTask.setComplexity(complexity);

        if (userGuidance != null) {
            sharedTask.updateUserGuidance(userGuidance);
        }

        Feed.getInstance().addTask(sharedTask);
        sharedDAO.update(sharedTask);

        return sharedMapper.toSharedDTO(sharedTask);
    }

    @Transactional
    public void deleteShared(Long taskId) {
        Shared sharedTask = sharedDAO.findById(taskId);
        if (sharedTask == null) {
            throw new IllegalArgumentException("Task con ID " + taskId + " non trovato.");
        }

        sharedTask.deleteTask();
        sharedDAO.delete(sharedTask.getId());
    }

    @Transactional
    public void moveToCalendar(SharedDTO sharedDTO) {
        Shared shared = sharedDAO.findById(sharedDTO.getId());
        if (shared == null) {
            throw new IllegalArgumentException("Task con ID " + sharedDTO.getId() + " not found.");
        }
        shared.toCalendar();
        sharedDAO.update(shared);
    }

    @Transactional
    public void removeSharedFromFeed(long sharedId){
        Shared shared = sharedDAO.findById(sharedId);
        shared.removeTaskJustFromFeed();
        sharedDAO.update(shared);
    }

    @Transactional
    public Comment getBestComment(long commentId, long sharedId) {
        Shared shared = sharedDAO.findById(sharedId);
        Comment comment = commentDAO.findById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment con ID " + commentId + " not found.");
        }
        shared.bestComment(comment);
        return comment;
    }

    @Transactional
    public void completeSharedBySessions(@Nullable CommentDTO commentDTO, long sharedId) {
        Shared shared = sharedDAO.findById(sharedId);
        if (commentDTO != null) {
            Comment comment = commentMapper.toCommentEntity(commentDTO);
            if (comment != null) {
                shared.completeBySessionsAndChooseBestComment(comment);
                commentDAO.save(comment);
            } else {
                shared.completeTaskBySessions();
            }
            sharedDAO.update(shared);
        }
    }

    @Transactional
    public void handleLimitExceeded(SessionDTO sessionDTO, long sharedId) {
        Shared shared = sharedDAO.findById(sharedId);
        if (shared == null) {
            throw new IllegalArgumentException("Shared task con ID " + sharedId + " non trovato.");
        }
        Session session = sessionMapper.toSessionEntity(sessionDTO);
        if (session == null) {
            throw new IllegalArgumentException("Session with ID " + sharedId + " not found.");
        }
        shared.autoSkipIfNotCompleted(session);
        sessionDAO.update(session);
        sharedDAO.update(shared);
    }
}
