package orm;

import domain.Resource;
import domain.Session;
import domain.Subtask;
import domain.Task;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class SubtaskMapper {

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private SessionMapper sessionMapper;

    public SubtaskDTO toSubtaskDTO(Subtask subtask) {
        if (subtask == null) return null;
        return new SubtaskDTO(subtask);
    }
    public Subtask toSubtaskEntity(SubtaskDTO subtaskDTO) {
        if (subtaskDTO == null) return null;
        List<Resource> resources = new ArrayList<>(subtaskDTO.getSubResources()
                .stream()
                .map(resourceMapper::toResourceEntity)
                .toList());

        List<Session> sessions = new ArrayList<>(subtaskDTO.getSubSessions()
                .stream()
                .map(sessionMapper::toSessionEntity)
                .toList());


        return new Subtask(
                subtaskDTO.getName(),
                subtaskDTO.getTotalTime(),
                subtaskDTO.getLevel(),
                subtaskDTO.getDescription(),
                resources,
                sessions
        );
    }
    public void updateSubtaskFromDTO(SubtaskDTO subtaskDTO, Subtask subtask) { //NEL CASO LO USASSI, INCOMPLETO
        if (subtaskDTO == null) return;
        subtask.setName(subtaskDTO.getName());
        subtask.setLevel(subtaskDTO.getLevel());
        subtask.setDescription(subtaskDTO.getDescription());
        subtask.setTotalTime(subtaskDTO.getTotalTime());
    }
}
