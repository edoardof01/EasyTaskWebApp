package com.DAOs;

import domain.Group;
import domain.Profile;
import domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import orm.GroupDAO;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class GroupDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private GroupDAO groupDAO;

    @BeforeAll
    static void init() {
        emf = Persistence.createEntityManagerFactory("EasyTaskTestPU");
    }

    @BeforeEach
    void setup() {
        em = emf.createEntityManager();
        groupDAO = new GroupDAO();
        groupDAO.setEntityManager(em);
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

        Group group = new Group();
        group.setName("Test Task");
        group.setUser(user);

        groupDAO.save(group);
        em.getTransaction().commit();

        assertNotNull(group.getId());
        Group found = groupDAO.findById(group.getId());
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

        Group p1 = new Group();
        p1.setName("Task 1");
        p1.setUser(user);
        groupDAO.save(p1);

        Group p2 = new Group();
        p2.setName("Task 2");
        p2.setUser(user);
        groupDAO.save(p2);

        em.getTransaction().commit();

        List<Group> all = groupDAO.findAll();
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

        Group group = new Group();
        group.setName("Original Name");
        group.setUser(user);
        groupDAO.save(group);
        em.getTransaction().commit();

        em.getTransaction().begin();
        group.setName("Updated Name");
        groupDAO.update(group);
        em.getTransaction().commit();

        Group found = groupDAO.findById(group.getId());
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

        Group group = new Group();
        group.setName("To Delete");
        group.setUser(user);
        groupDAO.save(group);
        em.getTransaction().commit();

        em.getTransaction().begin();
        groupDAO.delete(group.getId());
        em.getTransaction().commit();

        Group deleted = groupDAO.findById(group.getId());
        assertNull(deleted);
    }
}
