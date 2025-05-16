package orm;

import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;


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
                0,
                sharedDTO.getPriority(),
                sharedDTO.getTimetable(),
                sharedDTO.getTotalTime(),
                sharedDTO.getStrategies(),
                resources,
                sharedDTO.getUserGuidance()
        );
    }

}


