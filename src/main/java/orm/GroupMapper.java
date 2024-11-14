package orm;

import domain.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;

@ApplicationScoped
public class GroupMapper {
    public GroupDTO toGroupDTO(Group group) {
        if (group == null) {
            return null;
        }
        return new GroupDTO(group);
    }

    public Group toGroupEntity(GroupDTO groupDTO) {
        if (groupDTO == null) {
            return null;
        }
        return new Group(
                groupDTO.getNumUser(),
                groupDTO.getDateOnFeed(),
                groupDTO.getName(),
                groupDTO.getTopic(),
                groupDTO.getTaskState(),
                groupDTO.getDeadline(),
                groupDTO.getDescription(),
                groupDTO.getPercentageOfCompletion(),
                groupDTO.getComplexity(),
                groupDTO.getPriority(),
                groupDTO.getTimetable(),
                groupDTO.getTotalTime(),
                groupDTO.getStrategies(),
                groupDTO.getResources()
        );
    }
    public void updateGroupFromDTO(GroupDTO groupDTO, Group group) {
        if (groupDTO == null || group == null) {
            return;
        }
        // Aggiornamento delle proprietà principali
        group.setName(groupDTO.getName());
        group.setDeadline(groupDTO.getDeadline());
        group.setDescription(groupDTO.getDescription());
        group.setPercentageOfCompletion(groupDTO.getPercentageOfCompletion());
        group.setComplexity(groupDTO.getComplexity());
        group.setPriority(groupDTO.getPriority());
        group.setTimetable(groupDTO.getTimetable());
        group.setTotalTime(groupDTO.getTotalTime());

        // Aggiornamento dello stato e delle strategie
        group.setState(groupDTO.getTaskState());
        group.setStrategies(groupDTO.getStrategies());
        group.setResources(groupDTO.getResources());

    }
}


