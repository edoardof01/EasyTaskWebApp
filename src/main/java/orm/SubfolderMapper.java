package orm;

import domain.Subfolder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubfolderMapper {

    public SubfolderDTO toSubfolderDTO(Subfolder subfolder) {
        return new SubfolderDTO(subfolder);
    }

    public Subfolder toSubfolderEntity(SubfolderDTO subfolderDTO) {
        return new Subfolder(
                subfolderDTO.getType()
        );
    }
}
