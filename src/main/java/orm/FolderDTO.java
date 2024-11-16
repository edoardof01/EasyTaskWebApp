package orm;

import domain.Folder;
import domain.FolderType;
import domain.User;

public class FolderDTO {
    private final FolderType folderType;
    private final User user;

    public FolderDTO(Folder folder) {
        long id = folder.getId();
        this.folderType = folder.getFolderType();
        this.user = folder.getUser();
    }
    public long getId() {
        return user.getId();
    }
    public FolderType getFolderType() {
        return folderType;
    }
    public User getUser() {
        return user;
    }
}
