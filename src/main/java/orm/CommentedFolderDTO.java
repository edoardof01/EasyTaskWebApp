package orm;

import domain.CommentedFolder;
import domain.User;

public class CommentedFolderDTO {
    private long id;
    private final User user;

    public CommentedFolderDTO(CommentedFolder folder) {
        this.user = folder.getUser();
    }
    public long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
}
