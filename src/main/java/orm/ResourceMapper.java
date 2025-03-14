package orm;

import domain.Resource;
import domain.ResourceType;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResourceMapper {

    public ResourceDTO toResourceDTO(Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ResourceDTO(resource);
    }
    public Resource toResourceEntity(ResourceDTO resourceDTO) {
        if (resourceDTO == null) {
            return null;
        }
        return new Resource(
                resourceDTO.getName(),
                resourceDTO.getType(),
                resourceDTO.getValue(),
                resourceDTO.getMoney()
        );

    }

}
