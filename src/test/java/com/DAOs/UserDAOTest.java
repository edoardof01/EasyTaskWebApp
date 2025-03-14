package com.DAOs;

import domain.User;
import domain.Profile;
import domain.Sex;
import org.junit.jupiter.api.*;
import jakarta.persistence.*;
import orm.UserDAO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private UserDAO userDAO;

    @BeforeAll
    static void init() {
        emf = Persistence.createEntityManagerFactory("EasyTaskTestPU");
    }

    @BeforeEach
    void setup() {
        em = emf.createEntityManager();
        userDAO = new UserDAO();
        userDAO.setEntityManager(em);
    }

    @AfterEach
    void tearDown() {
        if (em.isOpen()) {
            em.close();
        }
    }

    @AfterAll
    static void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    // Qui mettiamo i test per i vari metodi del DAO
    @Test
    void testSaveAndFindById() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("myUser");


        User user = new User();
        user.setAge(30);
        user.setSex(Sex.MALE);
        user.setDescription("Test user description");
        user.setPersonalProfile(profile);

        userDAO.save(user);
        em.getTransaction().commit();

        assertTrue(user.getId() > 0);

        User found = userDAO.findById(user.getId());
        assertNotNull(found);
        assertEquals("myUser", found.getPersonalProfile().getUsername());
        assertEquals(30, found.getAge());
    }

    @Test
    void testFindByUsername() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("uniqueUser");

        User user = new User();
        user.setAge(25);
        user.setSex(Sex.FEMALE);
        user.setPersonalProfile(profile);

        userDAO.save(user);
        em.getTransaction().commit();

        User found = userDAO.findByUsername("uniqueUser");
        assertNotNull(found);
        assertEquals("uniqueUser", found.getPersonalProfile().getUsername());

        User notFound = userDAO.findByUsername("noSuchUser");
        assertNull(notFound);
    }

    @Test
    void testFindAll() {
        em.getTransaction().begin();

        Profile p1 = new Profile();
        p1.setUsername("user1");

        User u1 = new User();
        u1.setAge(20);
        u1.setSex(Sex.MALE);
        u1.setPersonalProfile(p1);
        userDAO.save(u1);

        Profile p2 = new Profile();
        p2.setUsername("user2");

        User u2 = new User();
        u2.setAge(40);
        u2.setSex(Sex.MALE);
        u2.setPersonalProfile(p2);
        userDAO.save(u2);

        em.getTransaction().commit();

        List<User> allUsers = userDAO.findAll();
        assertEquals(2, allUsers.size());
    }

    @Test
    void testUpdate() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("oldUsername");

        User user = new User();
        user.setAge(18);
        user.setSex(Sex.MALE);
        user.setPersonalProfile(profile);

        userDAO.save(user);
        em.getTransaction().commit();

        em.getTransaction().begin();
        user.setAge(19);
        user.getPersonalProfile().setUsername("newUsername");
        userDAO.update(user);
        em.getTransaction().commit();

        User found = userDAO.findById(user.getId());
        assertEquals(19, found.getAge());
        assertEquals("newUsername", found.getPersonalProfile().getUsername());
    }

    @Test
    void testDelete() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("toDelete");

        User user = new User();
        user.setAge(50);
        user.setSex(Sex.MALE);
        user.setPersonalProfile(profile);

        userDAO.save(user);
        em.getTransaction().commit();

        em.getTransaction().begin();
        userDAO.delete(user.getId());
        em.getTransaction().commit();

        User found = userDAO.findById(user.getId());
        assertNull(found);
    }
}
