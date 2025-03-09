
package com.DAOs;

import domain.Personal;
import domain.Profile;
import domain.User;
import org.junit.jupiter.api.*;
import jakarta.persistence.*;
import orm.PersonalDAO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonalDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private PersonalDAO personalDAO;

    @BeforeAll
    static void init() {
        emf = Persistence.createEntityManagerFactory("EasyTaskTestPU");
    }

    @BeforeEach
    void setup() {
        em = emf.createEntityManager();
        personalDAO = new PersonalDAO();
        personalDAO.setEntityManager(em);
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

    @Test
    void aveAndFindByIdTest() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("Edoardo");
        profile.setPassword("HelloToEveryone");
        User user = new User();
        user.setPersonalProfile(profile);
        em.persist(user);

        Personal personal = new Personal();
        personal.setName("Test Task");
        personal.setUser(user);

        personalDAO.save(personal);
        em.getTransaction().commit();

        assertNotNull(personal.getId());
        Personal found = personalDAO.findById(personal.getId());
        assertNotNull(found);
        assertEquals("Test Task", found.getName());
        assertEquals(user.getId(), found.getUser().getId());
    }


    @Test
    void testFindAll() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("Edoardo");
        profile.setPassword("HelloToEveryone");
        User user = new User();
        user.setPersonalProfile(profile);
        em.persist(user);

        Personal p1 = new Personal();
        p1.setName("Task 1");
        p1.setUser(user);
        personalDAO.save(p1);

        Personal p2 = new Personal();
        p2.setName("Task 2");
        p2.setUser(user);
        personalDAO.save(p2);

        em.getTransaction().commit();

        List<Personal> all = personalDAO.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testUpdate() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("Edoardo");
        profile.setPassword("HelloToEveryone");
        User user = new User();
        user.setPersonalProfile(profile);
        em.persist(user);

        Personal personal = new Personal();
        personal.setName("Original Name");
        personal.setUser(user);
        personalDAO.save(personal);
        em.getTransaction().commit();

        em.getTransaction().begin();
        personal.setName("Updated Name");
        personalDAO.update(personal);
        em.getTransaction().commit();

        Personal found = personalDAO.findById(personal.getId());
        assertNotNull(found);
        assertEquals("Updated Name", found.getName());
    }

    @Test
    void testDelete() {
        em.getTransaction().begin();

        Profile profile = new Profile();
        profile.setUsername("Edoardo");
        profile.setPassword("HelloToEveryone");
        User user = new User();
        user.setPersonalProfile(profile);
        em.persist(user);

        Personal personal = new Personal();
        personal.setName("To Delete");
        personal.setUser(user);
        personalDAO.save(personal);
        em.getTransaction().commit();

        em.getTransaction().begin();
        personalDAO.delete(personal.getId());
        em.getTransaction().commit();

        Personal deleted = personalDAO.findById(personal.getId());
        assertNull(deleted);
    }
}
