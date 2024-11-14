package orm;

import domain.Group;
import domain.Shared;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class GroupDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public GroupDAO() {}

    public Group findById(long id) {
        return entityManager.find(Group.class, id);
    }
    public List<Group> findAll() {
        TypedQuery<Group> query = entityManager.createQuery("SELECT s FROM Group s", Group.class);
        return query.getResultList();
    }
    @Transactional
    public void save(Group group) {
        entityManager.persist(group);
    }
    @Transactional
    public void update(Group group) {
        entityManager.merge(group);
    }
    @Transactional
    public void delete(long id) {
        Group group = findById(id);
        if (group != null) {
            entityManager.remove(group);
        }
    }

}
