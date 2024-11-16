package orm;

import domain.Resource;
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
                resourceDTO.getValue(),
                resourceDTO.getType(),
                resourceDTO.getMoney()
        );

    }
    public void updateResourceFromDTO(ResourceDTO resourceDTO, Resource resource) {
        if (resource == null || resourceDTO == null) {
            throw new NullPointerException("resourceDTO is null");
        }
        resource.setName(resourceDTO.getName());
        resource.setValue(resourceDTO.getValue());
        resource.setMoney(resourceDTO.getMoney());
    }
}
