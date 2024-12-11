package orm;

import domain.CommentedFolder;
import domain.User;

public class CommentedFolderDTO {
    private long id;
    private  UserDTO user;

    public CommentedFolderDTO() {}

    public CommentedFolderDTO(CommentedFolder folder) {
        this.user = new UserDTO(folder.getUser());
    }
    public long getId() {
        return id;
    }
    public UserDTO getUser() {
        return user;
    }
}
