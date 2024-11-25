package orm;

import domain.Profile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class ProfileDAO {

    @PersistenceContext
    private EntityManager em;

    public void save(Profile profile) {
        em.persist(profile);
    }
    public void update(Profile profile) {
        em.merge(profile);
    }
}
