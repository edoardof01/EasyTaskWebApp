package orm;
import jakarta.inject.Inject;
import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PersonalMapper {

    @Inject
    private SubtaskMapper subtaskMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private UserMapper userMapper;

    @Inject
    private SessionMapper sessionMapper;

    @Inject
    private UserService userService;

    public PersonalDTO toPersonalDTO(Personal personal) {
        if(personal == null) {
            return null;
        }
        return new PersonalDTO(personal);
    }
    public Personal toPersonalEntity(PersonalDTO personalDTO) {
        if(personalDTO == null) {
            return null;
        }
        List<Subtask> subtasks = new ArrayList<>(personalDTO.getSubtasks()
                .stream()
                .map(subtaskMapper::toSubtaskEntity)
                .toList());

        List<Resource> resources = new ArrayList<>(personalDTO.getResources()
                .stream()
                .map(resourceMapper::toResourceEntity)
                .toList());

        List<Session> sessions = new ArrayList<>(personalDTO.getSessions()
                .stream()
                .map(sessionMapper::toSessionEntity)
                .toList());

        UserDTO userDTO = userService.getUserByUsername(personalDTO.getUser().getPersonalProfile().getUsername());
        User user = userMapper.toUserEntity(userDTO);


        return new Personal(
                personalDTO.getName(),
                user,
                personalDTO.getTopic(),
                personalDTO.getDeadline(),
                personalDTO.getDescription(),
                subtasks,
                sessions,
                personalDTO.getPercentageOfCompletion(),
                personalDTO.getPriority(),
                personalDTO.getTimetable(),
                personalDTO.getTotalTime(),
                personalDTO.getStrategies(),
                resources
        );
    }
    public void updateSharedFromDTO(PersonalDTO personalDTO, Personal personal) {
        if (personalDTO == null || personal == null) {
            return;
        }
        // Aggiornamento delle proprietà principali
        personal.setName(personalDTO.getName());
        personal.setDeadline(personalDTO.getDeadline());
        personal.setDescription(personalDTO.getDescription());
        personal.setPercentageOfCompletion(personalDTO.getPercentageOfCompletion());
        personal.setComplexity(personalDTO.getComplexity());
        personal.setPriority(personalDTO.getPriority());
        personal.setTimetable(personalDTO.getTimetable());
        personal.setTotalTime(personalDTO.getTotalTime());

        // Aggiornamento dello stato e delle strategie
        personal.setState(personalDTO.getTaskState());
        personal.setStrategies(personalDTO.getStrategies());
        List<Resource> resources = personalDTO.getResources()
                .stream()
                .map(resourceMapper::toResourceEntity)
                .collect(Collectors.toList());
        personal.setResources(resources);
    }


}
