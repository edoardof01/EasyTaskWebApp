package orm;

import domain.Group;
import domain.Request;
import domain.Subtask;
import domain.User;
import jakarta.inject.Inject;

public class RequestMapper {

    @Inject
    UserMapper userMapper;

    @Inject
    SubtaskMapper subtaskMapper;
    @Inject
    GroupMapper groupMapper;


    public RequestDTO toRequestDTO(Request request) {
        if (request == null){
            throw new IllegalArgumentException("Request cannot be null");
        }
        return new RequestDTO(request);
    }
    public Request toRequestEntity(RequestDTO requestDTO) {
        if (requestDTO == null){
            throw new IllegalArgumentException("RequestDTO cannot be null");
        }
        User receiver = userMapper.toUserEntity(requestDTO.getReceiver());
        User sender = userMapper.toUserEntity(requestDTO.getSender());
        Subtask givenSubtask = subtaskMapper.toSubtaskEntity(requestDTO.getGivenSubtask());
        Subtask subtaskToReceive = subtaskMapper.toSubtaskEntity(requestDTO.getSubtaskToReceive());
        Group group = groupMapper.toGroupEntity(requestDTO.getGroup());
        return new Request(
                sender, receiver, group, givenSubtask, subtaskToReceive
        );
    }
}