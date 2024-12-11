package orm;

import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.persistence.expressions.ExpressionOperator.LocalDateTime;

@ApplicationScoped
public class SharedMapper {

    @Inject
    private SubtaskMapper subtaskMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private UserMapper userMapper;

    @Inject
    private SessionMapper sessionMapper;


    public SharedDTO toSharedDTO(Shared shared) {
        if (shared == null) {
            return null;
        }
        return new SharedDTO(shared);
    }

    public Shared toSharedEntity(SharedDTO sharedDTO) {
        if (sharedDTO == null) {
            return null;
        }
        // Usa gli altri mapper per convertire subtasks e resources
        List<Subtask> subtasks = new ArrayList<>(sharedDTO.getSubtasks()
                .stream()
                .map(subtaskMapper::toSubtaskEntity)
                .toList());

        List<Resource> resources = new ArrayList<>(sharedDTO.getResources()
                .stream()
                .map(resourceMapper::toResourceEntity)
                .toList());

        List<Session> sessions = new ArrayList<>(sharedDTO.getSessions()
                .stream()
                .map(sessionMapper::toSessionEntity)
                .toList());
        User user = userMapper.toUserEntity(sharedDTO.getUser());

        return new Shared(
                sharedDTO.getName(),
                user,
                sharedDTO.getTopic(),
                sharedDTO.getDeadline(),
                sharedDTO.getDescription(),
                subtasks,
                sessions,
                sharedDTO.getPercentageOfCompletion(),
                sharedDTO.getPriority(),
                sharedDTO.getTimetable(),
                sharedDTO.getTotalTime(),
                sharedDTO.getStrategies(),
                resources,
                sharedDTO.getUserGuidance()
        );
    }
    public void updateSharedFromDTO(SharedDTO sharedDTO, Shared shared) {
        if (sharedDTO == null || shared == null) {
            throw new NullPointerException();
        }

        // Aggiornamento delle proprietà principali
        shared.setName(sharedDTO.getName());
        shared.setDeadline(sharedDTO.getDeadline());
        shared.setDescription(sharedDTO.getDescription());
        shared.setPercentageOfCompletion(sharedDTO.getPercentageOfCompletion());
        shared.setComplexity(sharedDTO.getComplexity());
        shared.setPriority(sharedDTO.getPriority());
        shared.setTimetable(sharedDTO.getTimetable());
        shared.setTotalTime(sharedDTO.getTotalTime());

        // Aggiornamento dello stato e delle strategie
        shared.setState(sharedDTO.getTaskState());
        shared.setStrategies(sharedDTO.getStrategies());
        List<Resource> resources = sharedDTO.getResources()
                .stream()
                .map(resourceMapper::toResourceEntity)
                .collect(Collectors.toList());
        shared.setResources(resources);

        // Se c'è una proprietà che rappresenta la guida dell'utente, aggiornarla (facoltativo)
        if (sharedDTO.getUserGuidance() != null) {
            shared.updateUserGuidance(sharedDTO.getUserGuidance());
        }
    }
}


