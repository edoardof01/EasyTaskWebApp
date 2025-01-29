package orm;

import domain.RegisteredUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegisterDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(RegisteredUser user) {
        entityManager.persist(user);
    }

    @Transactional
    public RegisteredUser findByUsername(String username) {
        try {
            return entityManager.createQuery("SELECT u FROM RegisteredUser u WHERE u.username = :username", RegisteredUser.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

