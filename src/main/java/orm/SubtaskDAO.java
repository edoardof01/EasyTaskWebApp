package orm;

import domain.Subtask;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SubtaskDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public SubtaskDAO() {}

    public Subtask findById(long id) {
        return entityManager.find(Subtask.class, id);
    }
    public List<Subtask> findAll() {
        TypedQuery<Subtask> query = entityManager.createQuery("SELECT s FROM Subtask s", Subtask.class);
        return query.getResultList();
    }
    @Transactional
    public void save(Subtask subtask) {
        entityManager.persist(subtask);
    }
    @Transactional
    public void update(Subtask subtask) {
        entityManager.merge(subtask);
    }
    @Transactional
    public void delete(Subtask subtask) {
        entityManager.remove(subtask);
    }
}
