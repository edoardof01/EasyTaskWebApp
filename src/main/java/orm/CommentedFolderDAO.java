package orm;

import domain.CommentedFolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class CommentedFolderDAO {

    @PersistenceContext
    private EntityManager em;

    public CommentedFolder findById(long id) {
        return em.find(CommentedFolder.class, id);
    }

    public List<CommentedFolder> findAll() {
        return em.createQuery("Select c from CommentedFolder c", CommentedFolder.class).getResultList();
    }

    public void save(CommentedFolder commentedFolder) {
        em.persist(commentedFolder);
    }
    public void update(CommentedFolder commentedFolder) {
        em.merge(commentedFolder);
    }
    public void delete(CommentedFolder commentedFolder) {
        em.remove(commentedFolder);
    }
}

