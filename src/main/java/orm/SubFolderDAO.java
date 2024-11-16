package orm;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public class SubFolderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public SubFolderDAO() {}

    public SubfolderDTO findById(long id){
        return entityManager.find(SubfolderDTO.class, id);
    }
    public List<SubfolderDTO> findAll(){
        return entityManager.createQuery("Select s from Subfolder s", SubfolderDTO.class).getResultList();
    }
    @Transactional
    public void save(SubfolderDTO subfolderDTO){
         entityManager.persist(subfolderDTO);
    }
    @Transactional
    public void update(SubfolderDTO subfolderDTO){
        entityManager.merge(subfolderDTO);
    }
    @Transactional
    public void delete(SubfolderDTO subfolderDTO){
        entityManager.remove(subfolderDTO);
    }

}
