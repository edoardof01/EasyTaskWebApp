package orm;

import domain.CommentedFolder;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CommentedFolderMapper {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserMapper userMapper;

    public CommentedFolderMapper() {}

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
        User user = userMapper.toUserEntity(commentedFolderDTO.getUser());
        return new CommentedFolder(
                user
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
