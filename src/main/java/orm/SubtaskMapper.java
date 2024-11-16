package orm;

import domain.Subtask;

public class SubtaskMapper {
    public SubtaskDTO toSubtaskDTO(Subtask subtask) {
        if (subtask == null) return null;
        return new SubtaskDTO(subtask);
    }
    public Subtask toSubtaskEntity(SubtaskDTO subtaskDTO) {
        if (subtaskDTO == null) return null;
        return new Subtask(
                subtaskDTO.getName(),
                subtaskDTO.getLevel(),
                subtaskDTO.getDescription()
        );
    }
    public void updateSubtaskFromDTO(SubtaskDTO subtaskDTO, Subtask subtask) {
        if (subtaskDTO == null) return;
        subtask.setName(subtaskDTO.getName());
        subtask.setLevel(subtaskDTO.getLevel());
        subtask.setDescription(subtaskDTO.getDescription());
    }
}
