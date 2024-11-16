package orm;

import domain.Subfolder;
import domain.SubfolderType;

public class SubfolderDTO {
    private final long id;
    private final SubfolderType type;

    public SubfolderDTO(Subfolder subfolder) {
        this.id = subfolder.getId();
        this.type = subfolder.getType();
    }
    public long getId() {
        return id;
    }
    public SubfolderType getType() {
        return type;
    }
}
