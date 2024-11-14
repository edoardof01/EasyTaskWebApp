package orm;

import domain.Shared;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SharedDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public SharedDAO() {}

    public Shared findById(long id) {
        return entityManager.find(Shared.class, id);
    }
    public List<Shared> findAll() {
        TypedQuery<Shared> query = entityManager.createQuery("SELECT s FROM Shared s", Shared.class);
        return query.getResultList();
    }
    @Transactional
    public void save(Shared shared) {
        entityManager.persist(shared);
    }
    @Transactional
    public void update(Shared shared) {
        entityManager.merge(shared);
    }
    @Transactional
    public void delete(long id) {
        Shared shared = findById(id);
        if (shared != null) {
            entityManager.remove(shared);
        }
    }

}
