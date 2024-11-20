package orm;

import domain.Profile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class ProfileDAO {

    @Inject
    EntityManager em;

    public ProfileDAO(){}

    public Profile findById(long id) {
        return em.find(Profile.class, id);
    }
    public List<Profile> findAll() {
        return em.createQuery("SELECT p FROM Profile p", Profile.class).getResultList();
    }
    public void save(Profile profile) {
        em.persist(profile);
    }
    public void update(Profile profile) {
        em.merge(profile);
    }
    public void delete(Profile profile) {
        em.remove(em.merge(profile));
    }
}
