package orm;

import domain.Folder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FolderMapper {

    public FolderDTO toFolderDTO(Folder folder){
        if(folder == null) return null;
        return new FolderDTO(folder);
    }
    public Folder toFolderEntity(FolderDTO folderDTO){
        if(folderDTO == null) return null;
        return new Folder(
                folderDTO.getFolderType(),
                folderDTO.getUser()
        );
    }
    public void updateFolderFromDTO(FolderDTO folderDTO, Folder folder){
        if(folderDTO == null) return;
        folder.setFolderType(folderDTO.getFolderType());
        folder.setUser(folderDTO.getUser());
    }
}
