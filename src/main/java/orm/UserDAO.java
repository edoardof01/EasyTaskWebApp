package orm;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public UserDAO() {}

    public User findById(long userId) {
        return entityManager.find(User.class, userId);
    }

    public User findByUsername(String username) {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM User u WHERE u.personalProfile.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Nessun utente trovato
        }
    }

    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);
        return query.getResultList();
    }

    @Transactional
    public void save(User user) {
        entityManager.persist(user);
    }

    @Transactional
    public void update(User user) {
        entityManager.merge(user);
    }

    @Transactional
    public void delete(long id) {
        User user = findById(id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    // Metodo per cercare un utente tramite token di verifica
    public User findByVerificationToken(String token) {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.personalProfile.verificationToken = :token", User.class)
                .setParameter("token", token)
                .getSingleResult();
    }

}
