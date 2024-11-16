package orm;

import domain.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

public class CommentDAO {

    @PersistenceContext
    EntityManager entityManager;

    public Comment findById(long id) {
        return entityManager.find(Comment.class, id);
    }
    public List<Comment> findAll() {
        return entityManager.createQuery("SELECT c fROM Comment c", Comment.class).getResultList();
    }
    @Transactional
    public void save(Comment comment) {
        entityManager.persist(comment);
    }

    @Transactional
    public void update(Comment comment) {
        entityManager.merge(comment);
    }
    @Transactional
    public void delete(Comment comment) {
        entityManager.remove(comment);
    }
}
