package orm;

import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupMapper {

    @Inject
    private SubtaskMapper subtaskMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private UserMapper userMapper;

    @Inject
    private SessionMapper sessionMapper;

    public GroupDTO toGroupDTO(Group group) {
        if (group == null) return null;
        return new GroupDTO(group);
    }

    public Group toGroupEntity(GroupDTO groupDTO) {
        if (groupDTO == null) return null;

        // Usa gli altri mapper per convertire subtasks e resources
        List<Subtask> subtasks = new ArrayList<>(groupDTO.getSubtasks()
                .stream()
                .map(subtaskMapper::toSubtaskEntity)
                .toList());

        List<Resource> resources = new ArrayList<>(groupDTO.getResources()
                .stream()
                .map(resourceMapper::toResourceEntity)
                .toList());

        List<Session> sessions = new ArrayList<>(groupDTO.getSessions()
                .stream()
                .map(sessionMapper::toSessionEntity)
                .toList());


        User user = userMapper.toUserEntity(groupDTO.getUser());

        return new Group(
                groupDTO.getNumUser(),
                user,
                groupDTO.getName(),
                groupDTO.getTopic(),
                groupDTO.getDeadline(),
                groupDTO.getDescription(),
                subtasks,
                sessions,
                groupDTO.getPercentageOfCompletion(),
                groupDTO.getPriority(),
                groupDTO.getTimetable(),
                groupDTO.getTotalTime(),
                groupDTO.getStrategies(),
                resources
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
        group.setTotalTime(groupDTO.getTotalTime());

        // Aggiornamento dello stato e delle strategie
        group.setState(groupDTO.getTaskState());
        group.setStrategies(groupDTO.getStrategies());

        // Aggiornamento della timetable
        group.setTimetable(groupDTO.getTimetable());

        // Aggiornamento delle risorse usando il ResourceMapper
        List<Resource> resources = groupDTO.getResources()
                .stream()
                .map(resourceMapper::toResourceEntity)
                .collect(Collectors.toList());
        group.setResources(resources);

        group.setNumUsers(groupDTO.getNumUser());

    }

}


