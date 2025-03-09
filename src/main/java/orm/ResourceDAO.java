package orm;

import domain.Resource;
import jakarta.persistence.EntityManager;


public class ResourceDAO {
    private EntityManager em;

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public void save(Resource resource) {
        em.persist(resource);  // Nuova risorsa
    }

    public Resource findById(Long id) {
        return em.find(Resource.class, id);
    }

    public void delete(Resource resource) {
        if (em.contains(resource)) {
            em.remove(resource);
        } else {
            em.remove(em.merge(resource));
        }
    }
}
