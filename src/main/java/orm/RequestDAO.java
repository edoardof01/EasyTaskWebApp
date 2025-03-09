/*
package orm;

import domain.Request;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class RequestDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public Request findById(long id) {
        return entityManager.find(Request.class, id);
    }
    public List<Request> findAll() {
        return entityManager.createQuery("SELECT r FROM Request r", Request.class).getResultList();
    }
    @Transactional
    public void save(Request request) {
        entityManager.persist(request);
    }
    @Transactional
    public void update(Request request) {
        entityManager.merge(request);
    }
    @Transactional
    public void delete(int id) {
        Request request = findById(id);
    }
}
*/
