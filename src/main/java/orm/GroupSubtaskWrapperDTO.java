package orm;
public class GroupSubtaskWrapperDTO {

    private GroupDTO groupDTO;
    private SubtaskDTO chosenSubtaskDTO;

    // Costruttore vuoto
    public GroupSubtaskWrapperDTO() {
    }

    // Costruttore parametrizzato
    public GroupSubtaskWrapperDTO(GroupDTO groupDTO, SubtaskDTO chosenSubtaskDTO) {
        this.groupDTO = groupDTO;
        this.chosenSubtaskDTO = chosenSubtaskDTO;
    }

    // Getters e Setters
    public GroupDTO getGroupDTO() {
        return groupDTO;
    }

    public void setGroupDTO(GroupDTO groupDTO) {
        this.groupDTO = groupDTO;
    }

    public SubtaskDTO getChosenSubtaskDTO() {
        return chosenSubtaskDTO;
    }

    public void setChosenSubtaskDTO(SubtaskDTO chosenSubtaskDTO) {
        this.chosenSubtaskDTO = chosenSubtaskDTO;
    }
}