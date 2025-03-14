package orm;

import domain.Session;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SessionDAO {

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PersistenceContext
    private EntityManager entityManager;

    public Session findById(long id){
        return entityManager.find(Session.class, id);
    }

    public List<Session> findAll(){
        TypedQuery<Session> query = entityManager.createQuery("SELECT s FROM Session s",Session.class);
        return query.getResultList();
    }

    public void save(Session session) {
        entityManager.persist(session);
    }

    public void update(Session session) {
        entityManager.merge(session);
    }

    public void delete(long id) {
        Session session = findById(id);
        if (session != null) {
            entityManager.remove(session);
        }
    }

}
