package orm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FolderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public FolderDAO() {}

    public FolderDTO findById(long id) {
        return entityManager.find(FolderDTO.class, id);
    }
    public List<FolderDTO> findAll() {
        return entityManager.createQuery("select f from Folder f", FolderDTO.class).getResultList();
    }
    @Transactional
    public void save(FolderDTO folder) {
        entityManager.persist(folder);
    }
    @Transactional
    public void update(FolderDTO folder) {
        entityManager.merge(folder);
    }
    @Transactional
    public void delete(FolderDTO folder) {
        entityManager.remove(folder);
    }
}
