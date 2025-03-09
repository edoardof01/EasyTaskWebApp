package com.DAOs;


import domain.Session;
import domain.SessionState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import orm.SessionDAO;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class SessionDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private SessionDAO sessionDAO;

    @BeforeAll
    static void init() {
        emf = Persistence.createEntityManagerFactory("EasyTaskTestPU");
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        sessionDAO = new SessionDAO();
        sessionDAO.setEntityManager(em);
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
    void saveAndFindByIdTest() {
        Session session = new Session(LocalDateTime.of(2025, 3, 20, 10, 0),
                LocalDateTime.of(2025, 3, 20, 12, 0));
        session.setState(SessionState.PROGRAMMED);

        em.getTransaction().begin();
        sessionDAO.save(session);
        em.getTransaction().commit();

        Session foundSession = sessionDAO.findById(session.getId());
        assertNotNull(foundSession);
        assertEquals(session.getStartDate(), foundSession.getStartDate());
        assertEquals(session.getEndDate(), foundSession.getEndDate());
        assertEquals(session.getState(), foundSession.getState());
    }

    @Test
    void updateSessionTest() {
        Session session = new Session(LocalDateTime.of(2025, 3, 20, 14, 0),
                LocalDateTime.of(2025, 3, 20, 15, 0));
        session.setState(SessionState.PROGRAMMED);

        em.getTransaction().begin();
        sessionDAO.save(session);
        em.getTransaction().commit();

        // Modifica lo stato della sessione
        session.setState(SessionState.COMPLETED);

        em.getTransaction().begin();
        sessionDAO.update(session);
        em.getTransaction().commit();

        Session updatedSession = sessionDAO.findById(session.getId());
        assertNotNull(updatedSession);
        assertEquals(SessionState.COMPLETED, updatedSession.getState());
    }

    @Test
    void findAllSessionsTest() {
        Session session1 = new Session(LocalDateTime.of(2025, 3, 20, 16, 0),
                LocalDateTime.of(2025, 3, 20, 17, 0));
        session1.setState(SessionState.PROGRAMMED);

        Session session2 = new Session(LocalDateTime.of(2025, 3, 21, 10, 0),
                LocalDateTime.of(2025, 3, 21, 11, 0));
        session2.setState(SessionState.PROGRAMMED);

        em.getTransaction().begin();
        sessionDAO.save(session1);
        sessionDAO.save(session2);
        em.getTransaction().commit();

        List<Session> sessions = sessionDAO.findAll();
        assertNotNull(sessions);
        assertTrue(sessions.size() >= 2);
    }

    @Test
    void deleteSessionTest() {
        Session session = new Session(LocalDateTime.of(2025, 3, 22, 12, 0),
                LocalDateTime.of(2025, 3, 22, 13, 0));
        session.setState(SessionState.PROGRAMMED);

        em.getTransaction().begin();
        sessionDAO.save(session);
        em.getTransaction().commit();

        long sessionId = session.getId();

        em.getTransaction().begin();
        sessionDAO.delete(sessionId);
        em.getTransaction().commit();

        Session deletedSession = sessionDAO.findById(sessionId);
        assertNull(deletedSession);
    }
}
