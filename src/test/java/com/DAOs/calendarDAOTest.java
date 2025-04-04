package com.DAOs;


import domain.*;
import org.junit.jupiter.api.*;
import jakarta.persistence.*;
import orm.CalendarDAO;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarDAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private CalendarDAO calendarDAO;

    @BeforeAll
    static void init() {
        // Inizializza l'EntityManagerFactory con la PU di test (es. "EasyTaskTestPU")
        emf = Persistence.createEntityManagerFactory("EasyTaskTestPU");
    }

    @BeforeEach
    void setup() {
        em = emf.createEntityManager();
        calendarDAO = new CalendarDAO();
        calendarDAO.setEntityManager(em);
    }

    @AfterEach
    void tearDown() {
        if (em.isOpen()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Calendar").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
            em.clear();
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
        User user = new User();
        Profile profile = new Profile();
        user.setPersonalProfile(profile);
        em.persist(user);
        Calendar calendar = new Calendar(user);

        calendarDAO.save(calendar);

        em.getTransaction().commit();

        assertTrue(calendar.getId() > 0);

        Calendar found = calendarDAO.findById(calendar.getId());
        assertNotNull(found);
        assertEquals(user.getId(), found.getUser().getId());
    }

    @Test
    void testFindAll() {
        em.getTransaction().begin();

        User user1 = new User();
        Profile profile1 = new Profile();
        user1.setPersonalProfile(profile1);
        em.persist(user1);

        User user2 = new User();
        Profile profile2 = new Profile();
        user2.setPersonalProfile(profile2);
        em.persist(user2);
        Calendar c1 = new Calendar(user1);
        Calendar c2 = new Calendar(user2);

        calendarDAO.save(c1);
        calendarDAO.save(c2);

        em.getTransaction().commit();

        List<Calendar> all = calendarDAO.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testUpdate() {
        em.getTransaction().begin();

        User user = new User();
        Profile profile = new Profile();
        user.setPersonalProfile(profile);
        em.persist(user);

        Calendar calendar = new Calendar(user);
        calendarDAO.save(calendar);

        em.getTransaction().commit();
        em.getTransaction().begin();

        User newUser = new User();
        Profile profile2 = new Profile();
        newUser.setPersonalProfile(profile2);
        em.persist(newUser);

        calendar.setUser(newUser);
        calendarDAO.update(calendar);

        em.getTransaction().commit();

        Calendar updated = calendarDAO.findById(calendar.getId());
        assertNotNull(updated);
        assertEquals(newUser.getId(), updated.getUser().getId());
    }

    @Test
    void testDelete() {
        em.getTransaction().begin();

        User user = new User();
        Profile profile = new Profile();
        user.setPersonalProfile(profile);
        em.persist(user);

        Calendar calendar = new Calendar(user);
        calendarDAO.save(calendar);

        em.getTransaction().commit();

        em.getTransaction().begin();
        calendarDAO.delete(calendar);
        em.getTransaction().commit();

        Calendar found = calendarDAO.findById(calendar.getId());
        assertNull(found);
    }

    @Test
    void testAddSessions() {
        em.getTransaction().begin();

        User user = new User();
        Profile profile = new Profile();
        user.setPersonalProfile(profile);
        em.persist(user);

        Calendar calendar = new Calendar(user);
        calendarDAO.save(calendar);

        em.getTransaction().commit();

        em.getTransaction().begin();
        Session s1 = new Session();
        s1.setStartDate(LocalDateTime.of(2025, 3, 20, 10, 0));
        s1.setEndDate(LocalDateTime.of(2025, 3, 20, 11, 0));

        Session s2 = new Session();
        s2.setStartDate(LocalDateTime.of(2025, 3, 21, 9, 0));
        s2.setEndDate(LocalDateTime.of(2025, 3, 21, 10, 0));
        em.persist(s1);
        em.persist(s2);

        em.getTransaction().commit();


        em.getTransaction().begin();
        Calendar managedCalendar = calendarDAO.findById(calendar.getId());
        managedCalendar.addSessions(List.of(s1, s2));

        calendarDAO.update(managedCalendar);
        em.getTransaction().commit();

        Calendar updatedCalendar = calendarDAO.findById(calendar.getId());
        assertEquals(2, updatedCalendar.getSessions().size());
    }

    @Test
    void testRemoveSessions() {
        em.getTransaction().begin();

        User user = new User();
        Profile profile = new Profile();
        user.setPersonalProfile(profile);
        em.persist(user);

        Calendar calendar = new Calendar(user);
        calendarDAO.save(calendar);

        Personal task = new Personal();
        Session s1 = new Session();
        s1.setStartDate(LocalDateTime.of(2025, 3, 20, 10, 0));
        s1.setEndDate(LocalDateTime.of(2025, 3, 20, 11, 0));

        Session s2 = new Session();
        s2.setStartDate(LocalDateTime.of(2025, 3, 21, 9, 0));
        s2.setEndDate(LocalDateTime.of(2025, 3, 21, 10, 0));

        em.persist(s1);
        em.persist(s2);


        task.getSessions().add(s1);
        task.getSessions().add(s2);

        Calendar managedCalendar = calendarDAO.findById(calendar.getId());
        managedCalendar.addSessions(task.getSessions());
        calendarDAO.update(managedCalendar);

        em.getTransaction().commit();

        em.getTransaction().begin();
        Calendar managedCalendar2 = calendarDAO.findById(calendar.getId());
        managedCalendar2.removeSessions(task);
        calendarDAO.update(managedCalendar2);
        em.getTransaction().commit();

        Calendar updatedCalendar = calendarDAO.findById(calendar.getId());
        assertTrue(updatedCalendar.getSessions().isEmpty());
    }
}
