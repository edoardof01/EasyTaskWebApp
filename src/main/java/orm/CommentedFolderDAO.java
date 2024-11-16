package orm;

import domain.CommentedFolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CommentedFolderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public CommentedFolderDAO() {}

    public CommentedFolderDTO toCommentedFolderDTO(CommentedFolder commentedFolder) {
        if (commentedFolder == null) {
            return null;
        }
        return new CommentedFolderDTO(commentedFolder);
    }
    public CommentedFolder toCommentedFolderEntity(CommentedFolderDTO commentedFolderDTO) {
        if (commentedFolderDTO == null) {
            return null;
        }
        return new CommentedFolder(
                commentedFolderDTO.getUser()
        );
    }
    @Transactional
    public void save(CommentedFolder commentedFolder) {
        entityManager.persist(commentedFolder);
    }
    @Transactional
    public void update(CommentedFolder commentedFolder) {
        entityManager.merge(commentedFolder);
    }
    @Transactional
    public void delete(CommentedFolder commentedFolderDTO) {
        entityManager.remove(commentedFolderDTO);
    }

}
