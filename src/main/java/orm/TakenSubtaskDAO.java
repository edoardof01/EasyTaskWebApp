package orm;
import domain.TakenSubtask;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class TakenSubtaskDAO {

    @PersistenceContext
    private EntityManager em;

    public void save(TakenSubtask takenSubtask){
        em.persist(takenSubtask);
    }
    public void update(TakenSubtask takenSubtask){
        em.merge(takenSubtask);
    }
    public void delete(TakenSubtask takenSubtask){
        em.remove(takenSubtask);
    }
}
