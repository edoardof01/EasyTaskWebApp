package orm;

import domain.Resource;
import domain.Subtask;
import jakarta.inject.Inject;

import java.util.List;

public class SubtaskMapper {

    @Inject
    private ResourceMapper resourceMapper;

    public SubtaskDTO toSubtaskDTO(Subtask subtask) {
        if (subtask == null) return null;
        return new SubtaskDTO(subtask);
    }
    public Subtask toSubtaskEntity(SubtaskDTO subtaskDTO) {
        if (subtaskDTO == null) return null;
        List<Resource> resources = subtaskDTO.getResources().stream().
                map(resourceMapper :: toResourceEntity).toList();
        return new Subtask(
                subtaskDTO.getName(),
                subtaskDTO.getTotalTime(),
                subtaskDTO.getLevel(),
                subtaskDTO.getDescription(),
                resources
        );
    }
    public void updateSubtaskFromDTO(SubtaskDTO subtaskDTO, Subtask subtask) {
        if (subtaskDTO == null) return;
        subtask.setName(subtaskDTO.getName());
        subtask.setLevel(subtaskDTO.getLevel());
        subtask.setDescription(subtaskDTO.getDescription());
        subtask.setTotalTime(subtaskDTO.getTotalTime());
    }
}
