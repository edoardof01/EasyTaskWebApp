package orm;

import domain.Personal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PersonalDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public PersonalDAO() {}

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Personal findById(long id) {
        return entityManager.find(Personal.class, id);
    }
    public List<Personal> findAll() {
        TypedQuery<Personal> query = entityManager.createQuery("SELECT s FROM Personal s", Personal.class);
        return query.getResultList();
    }
    @Transactional
    public void save(Personal personal) {
        entityManager.persist(personal);
    }

    @Transactional
    public void update(Personal personal) {
        entityManager.merge(personal);
    }

    @Transactional
    public void delete(long id) {
        Personal shared = findById(id);
        if (shared != null) {
            entityManager.remove(shared);
        }
    }
}
