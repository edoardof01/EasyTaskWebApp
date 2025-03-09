package com.domain;
import domain.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CalendarEntityTest {

    @InjectMocks
    private Calendar calendar;

    @BeforeEach
    void setUp() {
    calendar = new Calendar();
    calendar.setId(1L);
    User user = new User();
    user.setId(1L);
    calendar.setUser(user);
    user.setCalendar(calendar);
    }

    @Test
    void addSessionsTest_success(){
        LocalDateTime now = LocalDateTime.now();
        Session s1 = new Session(now, now.plusHours(1));
        Session s2 = new Session(now.plusHours(2), now.plusHours(3));
        List<Session> sessionsToAdd = new ArrayList<>();
        sessionsToAdd.add(s1);
        sessionsToAdd.add(s2);

        calendar.addSessions(sessionsToAdd);

        assertAll(
                ()-> assertEquals(2, calendar.getSessions().size()),
                ()->assertTrue(calendar.getSessions().contains(s1)),
                ()->assertTrue(calendar.getSessions().contains(s2))
        );
    }

    @Test
    void addSessionsTest_failDuplicate() {
        LocalDateTime now = LocalDateTime.now();
        Session s1 = new Session(now, now.plusHours(1));
        Session sDuplicate = new Session(now, now.plusHours(1));

        List<Session> sessionsToAdd = new ArrayList<>();
        sessionsToAdd.add(s1);
        calendar.addSessions(sessionsToAdd);

        List<Session> duplicateList = new ArrayList<>();
        duplicateList.add(sDuplicate);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            calendar.addSessions(duplicateList);
        });
        assertTrue(ex.getMessage().contains("already exists in the calendar"));
    }

    @Test
    void testAddSessions_collision() {
        LocalDateTime now = LocalDateTime.now();
        Session s1 = new Session(now, now.plusHours(2));
        // sCollision inizia durante s1
        Session sCollision = new Session(now.plusHours(1), now.plusHours(3));
        List<Session> sessionsToAdd = new ArrayList<>();
        sessionsToAdd.add(s1);
        calendar.addSessions(sessionsToAdd);

        List<Session> collisionList = new ArrayList<>();
        collisionList.add(sCollision);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            calendar.addSessions(collisionList);
        });
        assertTrue(ex.getMessage().contains("collides with an existing session"));
    }

    @Test
    void testRemoveSessions_success() {
        LocalDateTime now = LocalDateTime.now();
        Session s1 = new Session(now, now.plusHours(1));
        Session s2 = new Session(now.plusHours(2), now.plusHours(3));
        List<Session> sessionsToAdd = new ArrayList<>();
        sessionsToAdd.add(s1);
        sessionsToAdd.add(s2);
        calendar.addSessions(sessionsToAdd);

        // Crea un dummy task che restituisce le stesse sessioni
        Personal task = new Personal();
        task.setSessions(sessionsToAdd);

        calendar.removeSessions(task);

        assertTrue(calendar.getSessions().isEmpty());
    }

    @Test
    void addSubtaskSessionsForGroupsTest_success() {
        LocalDateTime now = LocalDateTime.now();
        Session s1 = new Session(now, now.plusHours(1));
        Session s2 = new Session(now.plusHours(2), now.plusHours(3));

        Subtask subtask = new Subtask("subtask 1",1,1,"this is a subtask",new ArrayList<Resource>(),new ArrayList<Session>(List.of(s1,s2)));

        calendar.addSubtaskSessionsForGroups(subtask);
        assertAll(
                () -> assertEquals(2, calendar.getSessions().size()),
                () -> assertTrue(calendar.getSessions().contains(s1)),
                ()-> assertTrue(calendar.getSessions().contains(s2))
        );
    }

    @Test
    void addSubtaskSessionsForGroupsTest_duplicate() {
        LocalDateTime now = LocalDateTime.now();
        Session s1 = new Session(now, now.plusHours(1));
        List<Session> subtaskSessions = new ArrayList<>();

        Subtask subtask = new Subtask("subtask 1",1,1,"this is a subtask",new ArrayList<Resource>(),new ArrayList<Session>(List.of(s1)));

        calendar.addSubtaskSessionsForGroups(subtask);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            calendar.addSubtaskSessionsForGroups(subtask);
        });
        assertTrue(ex.getMessage().contains("already exists in the calendar"));
    }

    @Test
    void testRemoveSubtaskSessionsForGroups() {
        LocalDateTime now = LocalDateTime.now();
        Session s1 = new Session(now, now.plusHours(1));
        Session s2 = new Session(now.plusHours(2), now.plusHours(3));

        Subtask subtask = new Subtask("subtask 1",1,1,"this is a subtask",new ArrayList<Resource>(),new ArrayList<Session>(List.of(s1,s2)));

        // Aggiungi le sessioni dal subtask
        calendar.addSubtaskSessionsForGroups(subtask);
        assertEquals(2, calendar.getSessions().size());

        // Rimuovi le sessioni del subtask dal calendario
        calendar.removeSubtaskSessionsForGroups(subtask);
        assertEquals(0, calendar.getSessions().size());
    }





}
