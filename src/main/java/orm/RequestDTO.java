/*
package orm;


import domain.Request;
import jakarta.persistence.ManyToOne;


public class RequestDTO {
    private Long id;
    private UserDTO sender;
    private UserDTO receiver;
    private SubtaskDTO givenSubtask;
    @ManyToOne
    private SubtaskDTO SubtaskToReceive;
    @ManyToOne
    private GroupDTO group;

    public RequestDTO() {}

    public RequestDTO(Request request) {
        this.id = request.getId();
        this.sender = new UserDTO(request.getSender());
        this.receiver = new UserDTO(request.getReceiver());
        this.givenSubtask = new SubtaskDTO(request.getGivenSubtask());
        this.SubtaskToReceive = new SubtaskDTO(request.getSubtaskToReceive());
        this.group = new GroupDTO(request.getGroup());
    }
    public RequestDTO(Long id, UserDTO sender, UserDTO receiver, SubtaskDTO givenSubtask, SubtaskDTO SubtaskToReceive, GroupDTO group) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.givenSubtask = givenSubtask;
        this.SubtaskToReceive = SubtaskToReceive;
        this.group = group;
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public UserDTO getSender() {
        return sender;
    }
    public void setSender(UserDTO sender) {
        this.sender = sender;
    }
    public UserDTO getReceiver() {
        return receiver;
    }
    public void setReceiver(UserDTO receiver) {
        this.receiver = receiver;
    }
    public SubtaskDTO getGivenSubtask() {
        return givenSubtask;
    }
    public void setGivenSubtask(SubtaskDTO givenSubtask) {
        this.givenSubtask = givenSubtask;
    }
    public SubtaskDTO getSubtaskToReceive() {
        return SubtaskToReceive;
    }
    public void setSubtaskToReceive(SubtaskDTO subtaskToReceive) {
        this.SubtaskToReceive = subtaskToReceive;
    }
    public GroupDTO getGroup() {
        return group;
    }
    public void setGroup(GroupDTO group) {
        this.group = group;
    }


}
*/
