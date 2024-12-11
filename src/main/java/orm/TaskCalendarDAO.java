package orm;

import domain.TaskCalendar;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class TaskCalendarDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public TaskCalendarDAO() {}

    public TaskCalendar findById(long id){
        return entityManager.find(TaskCalendar.class, id);
    }
    public List<TaskCalendar> findAll(){
        return entityManager.createQuery("SELECT t FROM TaskCalendar t",TaskCalendar.class).getResultList();
    }
    public void save(TaskCalendar taskCalendar){
        entityManager.persist(taskCalendar);
    }
    public void update(TaskCalendar taskCalendar){
        entityManager.merge(taskCalendar);
    }
    public void delete(TaskCalendar taskCalendar){
        entityManager.remove(taskCalendar);
    }
}

