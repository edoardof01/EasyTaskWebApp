package orm;


import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TaskDAO {

    @PersistenceContext
    private EntityManager em;

    public List<Task> findAll() {
        List<Task> result = new ArrayList<>();

        // Carico tutti i Group che hanno isOnFeed = true
        List<Group> groups = em.createQuery(
                "SELECT g FROM Group g WHERE g.isOnFeed = true",
                Group.class
        ).getResultList();

        // Carico tutti i Shared che hanno isOnFeed = true
        List<Shared> shareds = em.createQuery(
                "SELECT s FROM Shared s WHERE s.isOnFeed = true",
                Shared.class
        ).getResultList();

        // Unisco le due liste
        result.addAll(groups);
        result.addAll(shareds);

        return result;
    }



    public List<Task> findFiltered(Topic topicFilter, boolean groupFilter, boolean sharedFilter) {
        List<Task> result = new ArrayList<>();

        if (groupFilter) {
            StringBuilder sbGroup = new StringBuilder("SELECT g FROM Group g WHERE g.isOnFeed = true ");
            if (topicFilter != null) {
                sbGroup.append("AND g.topic = :topic ");
            }
            var queryGroup = em.createQuery(sbGroup.toString(), Group.class);
            if (topicFilter != null) {
                queryGroup.setParameter("topic", topicFilter);
            }
            result.addAll(queryGroup.getResultList());
        }

        if (sharedFilter) {
            StringBuilder sbShared = new StringBuilder("SELECT s FROM Shared s WHERE s.isOnFeed = true ");
            if (topicFilter != null) {
                sbShared.append("AND s.topic = :topic ");
            }
            var queryShared = em.createQuery(sbShared.toString(), Shared.class);
            if (topicFilter != null) {
                queryShared.setParameter("topic", topicFilter);
            }
            result.addAll(queryShared.getResultList());
        }

        return result;
    }

}
