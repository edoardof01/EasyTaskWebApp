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
    public void updateResourceFromDTO(ResourceDTO resourceDTO, Resource resource) {
        if (resource == null || resourceDTO == null) {
            throw new NullPointerException("resourceDTO or resource is null");
        }

        resource.setName(resourceDTO.getName());
        resource.setType(resourceDTO.getType());

        if (resource.getType() == ResourceType.MONEY) {
            // Aggiorna il campo money e calcola dinamicamente il valore
            resource.setMoney(resourceDTO.getMoney());
        } else {
            // Aggiorna il campo value per COMPETENCE e EQUIPMENT
            resource.setValue(resourceDTO.getValue());
            // Assicuriamoci che il campo money sia nullo per non-MONEY
            resource.setMoney(null);
        }
    }

}
