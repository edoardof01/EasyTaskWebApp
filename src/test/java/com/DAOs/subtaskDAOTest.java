package com.DAOs;

import domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import orm.ResourceDAO;
import orm.SubtaskDAO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private SubtaskDAO subtaskDAO;

    @BeforeAll
    static void init() {
        emf = Persistence.createEntityManagerFactory("EasyTaskTestPU");
    }

    @BeforeEach
    void setup() {
        em = emf.createEntityManager();
        subtaskDAO = new SubtaskDAO();
        subtaskDAO.setEntityManager(em);
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
    void testSaveAndFindById() {
        em.getTransaction().begin();

        Session session = new Session(LocalDateTime.of(2025, 3, 20, 10, 0),
                LocalDateTime.of(2025, 3, 20, 12, 0));
        session.setState(SessionState.PROGRAMMED);
        em.persist(session);

        List<Session> sessions = List.of(session);
        Subtask subtask = new Subtask("Test Subtask", 120, 1, "description", new ArrayList<>(), sessions);

        subtaskDAO.save(subtask);
        em.getTransaction().commit();
        assertNotNull(subtask.getId(), "ID should be generated after save");

        Subtask found = subtaskDAO.findById(subtask.getId());
        assertNotNull(found, "Subtask should be found by ID");
        assertEquals("Test Subtask", found.getName());
        assertEquals(1, found.getLevel());
        assertEquals("description", found.getDescription());
        assertEquals(120, found.getTotalTime());
        assertEquals(1, found.getSessions().size());
        assertEquals(0, found.getResources().size());
    }


    @Test
    void findAllTest() {
        em.getTransaction().begin();

        // Creiamo la prima subtask
        Subtask s1 = new Subtask();
        s1.setName("Subtask1");
        s1.setDescription("desc1");
        s1.setLevel(1);
        s1.setTotalTime(60);
        subtaskDAO.save(s1);

        // Creiamo la seconda subtask
        Subtask s2 = new Subtask();
        s2.setName("Subtask2");
        s2.setDescription("desc2");
        s2.setLevel(2);
        s2.setTotalTime(120);
        subtaskDAO.save(s2);

        em.getTransaction().commit();

        // Recuperiamo tutte le subtasks
        List<Subtask> all = subtaskDAO.findAll();
        assertTrue(all.size() >= 2, "We should have at least 2 subtasks in the DB");
    }

    @Test
    void updateTest() {
        em.getTransaction().begin();

        // Creiamo e salviamo una subtask
        Subtask subtask = new Subtask();
        subtask.setName("Original");
        subtask.setDescription("Original desc");
        subtask.setLevel(1);
        subtask.setTotalTime(30);
        subtaskDAO.save(subtask);

        em.getTransaction().commit();

        // Modifichiamo la subtask
        em.getTransaction().begin();
        subtask.setName("Updated");
        subtask.setDescription("Updated desc");
        subtaskDAO.update(subtask);
        em.getTransaction().commit();

        // Verifichiamo
        Subtask found = subtaskDAO.findById(subtask.getId());
        assertNotNull(found);
        assertEquals("Updated", found.getName());
        assertEquals("Updated desc", found.getDescription());
    }

    @Test
    void deleteTest() {
        em.getTransaction().begin();

        // Creiamo e salviamo una subtask
        Subtask subtask = new Subtask();
        subtask.setName("ToDelete");
        subtask.setDescription("desc delete");
        subtask.setLevel(3);
        subtask.setTotalTime(90);
        subtaskDAO.save(subtask);

        em.getTransaction().commit();

        Long subtaskId = subtask.getId();

        // Eliminiamo
        em.getTransaction().begin();
        Subtask managedSubtask = subtaskDAO.findById(subtaskId);
        subtaskDAO.delete(managedSubtask);
        em.getTransaction().commit();

        // Verifichiamo
        Subtask deleted = subtaskDAO.findById(subtaskId);
        assertNull(deleted, "Subtask should be null after deletion");
    }
}

