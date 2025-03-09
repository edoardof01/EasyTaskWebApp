package orm;

import domain.Calendar;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CalendarDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public CalendarDAO(){}

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Calendar findById(long id){
        return entityManager.find(Calendar.class, id);
    }
    public List<Calendar> findAll(){
        TypedQuery<Calendar> query = entityManager.createQuery("SELECT c FROM Calendar c", Calendar.class);
        return query.getResultList();
    }
    @Transactional
    public void save(Calendar calendar){
        entityManager.persist(calendar);
    }
    @Transactional
    public void update(Calendar calendar){
        entityManager.merge(calendar);
    }
    @Transactional
    public void delete(Calendar calendar){
        entityManager.remove(calendar);
    }

}
