
package com.services;

import domain.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import orm.*;
import service.PersonalService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalServiceTest {

    @Mock
    private PersonalDAO personalDAO;

    @Mock
    private SessionDAO sessionDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private PersonalMapper personalMapper;

    @Mock
    private CalendarDAO calendarDAO;

    @Mock
    private SubtaskDAO subtaskDAO;

    @InjectMocks
    private PersonalService personalService;

    private Personal personal;
    private PersonalDTO personalDTO;
    private User user;
    private Session session;

    @BeforeEach
    void setup() {
        personal = new Personal();
        user = new User();
        user.setId(1L);
        personal.setId(1L);
        personal.setName("Test Task");
        personal.setUser(user);
        session = new Session();
        session.setId(1L);
        session.setStartDate(LocalDateTime.of(2025, 3, 20, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 20, 11, 0));
        session.setState(SessionState.PROGRAMMED);
        List<Session> sessions = new ArrayList<>(List.of(session));
        personal.setSessions(sessions);

        personalDTO = new PersonalDTO();
        personalDTO.setId(1L);
        personalDTO.setName("Test Task");
    }

    @Test
    void getPersonalByIdTest_Success() {
        when(personalDAO.findById(1L)).thenReturn(personal);
        when(personalMapper.toPersonalDTO(personal)).thenReturn(personalDTO);

        PersonalDTO result = personalService.getPersonalById(1L);


        assertAll(
                ()-> assertNotNull(result),
                ()-> assertEquals(1L, result.getId()),
                ()-> assertEquals("Test Task", result.getName())
        );

        verify(personalDAO, times(1)).findById(1L);
        verify(personalMapper, times(1)).toPersonalDTO(personal);
    }

    @Test
    void getPersonalByIdTest_NotFound() {
        when(personalDAO.findById(99L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> personalService.getPersonalById(99L)); //

        verify(personalDAO, times(1)).findById(99L);
        verify(personalMapper, never()).toPersonalDTO(any()); // serve per controllare che non si arrivi mai
                                                                // a chiamare il metodo del mapper perché c'è un errore
    }


    /* CREATE PERSONALTASK TESTS */
    /* INIZIO SEQUENZA DI TEST CHE RIGUARDANO LA MANCANZA DI CAMPI OBBLIGATORI NELLA CREAZIONE DEL TASK */
    @Test
    void createPersonalTest_failNullMandatoryFieldName() {
        long userId = 1L;
        Topic topic = Topic.ART;
        int totalTime = 1;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        int priority = 1;
        String description = "Test description";
        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        List<Session> sessions = List.of(session);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(null, userId, topic, null, totalTime, timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );
    }

    @Test
    void createPersonalTest_NullMandatoryFieldTopic() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 1;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        int priority = 1;
        String description = "Test description";
        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        List<Session> sessions = List.of(session);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, null, null, totalTime, timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );
    }

      @Test void createPersonalTest_NullMandatoryFieldTimeTable() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 1;
        Topic topic = Topic.ART;
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        int priority = 1;
        String description = "Test description";
        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        List<Session> sessions = List.of(session);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, null, strategies,
                        priority, description, resources, null, sessions)
        );
    }


    @Test
    void createPersonalTest_NullMandatoryFieldTotalTime() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = -1;
        Topic topic = Topic.ART;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        int priority = 1;
        String description = "Test description";
        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        List<Session> sessions =
                List.of(session);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime , timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );
    }

    @Test
    void createPersonalTest_NullMandatoryFieldStrategies() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 1;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        String description = "Test description";
        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        List<Session> sessions = List.of(session);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, null,
                        priority, description, resources, null, sessions)
        );
    }

    @Test
    void createPersonalTest_NullMandatoryFieldSessions() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 1; // mi assicuro che il totalTime corrisponda alle sessioni
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        String description = "Test description";
        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, null,
                        priority, description, resources, null, null)
        );
    }

    @Test
    public void createPersonalTest_negativeFieldTotalTime() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = -1;
        Topic topic = Topic.ART;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        int priority = 1;
        String description = "Test description";
        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 12, 0));
        List<Session> sessions =
                List.of(session);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime , timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );
    }




    /* FINE SEQUENZA DI TEST CHE RIGUARDANO LA MANCANZA DI CAMPI OBBLIGATORI NELLA CREAZIONE DEL TASK **/



    /* INIZIO TEST SU STRATEGIE */

    @Test
    void createPersonalTest_SKIPPEDStrategyAndDeadline() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 2;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY;
        String description = "Test description";
        LocalDateTime deadline = LocalDateTime.of(2025, 3, 9, 10, 0);
        List<Resource> resources = new ArrayList<>();
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 12, 0));
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        List<Session> sessions = List.of(session);


        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, deadline, totalTime, timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );
    }

  @Test
  void createPersonalTest_SKIPPEDStrategyAndMore() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 1;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        String description = "Test description";

        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null),
                new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,2,null));
        List<Session> sessions = List.of(session);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );
    }

    /* FINE TEST SU STRATEGIE */


    @Test
    void createPersonalTest_DoesUserExist(){
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 1;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        String description = "Test description";

        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida
        // Creiamo una sessione valida (dato che le sessioni sono obbligatorie)
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        List<Session> sessions = List.of(session);

        when(userDAO.findById(userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );
    }

    @Test
    void createPersonalTest_SessionsOverlapping() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 1;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY; // oggetto valido
        String description = "Test description";

        List<Resource> resources = new ArrayList<>(); // lista vuota ma valida

        Session personalSession = new Session();
        personalSession.setStartDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        personalSession.setEndDate(LocalDateTime.of(2025, 3, 9, 12, 0));
        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null));
        List<Session> sessions = new ArrayList<>(List.of(personalSession));

        Session calendarSession = new Session();
        calendarSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 15));
        calendarSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 15));
        List<Session> calendarSessions = new ArrayList<>(List.of(calendarSession));

        when(userDAO.findById(userId)).thenReturn(user);


        Calendar calendar = new Calendar(user);
        calendar.addSessions(calendarSessions);
        user.setCalendar(calendar);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, strategies,
                        priority, description, resources, null, sessions)
        );

    }

    /* INIZIO TEST SUI SUBTASKS */
    /* Test a esito positivo*/
    @Test
    void createPersonalTest_sessionsValidData() {
        // Configura tutti i dati in modo che siano validi
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 2;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY;
        String description = "Test description";
        List<Resource> resources = new ArrayList<>();

        Session personalSession = new Session();
        Session personalSession2 = new Session();
        personalSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        personalSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        personalSession2.setStartDate(LocalDateTime.of(2025, 3, 9, 14, 0));
        personalSession2.setEndDate(LocalDateTime.of(2025, 3, 9, 15, 0));
        List<Session> sessions = new ArrayList<>(List.of(personalSession, personalSession2));

        // Crea due subtasks che usano le stesse sessioni della lista principale (caso valido)
        Subtask subtask1 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(), new ArrayList<>(List.of(personalSession)));
        Subtask subtask2 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(), new ArrayList<>(List.of(personalSession2)));
        List<Subtask> subtasks = new ArrayList<>(List.of(subtask1, subtask2));

        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null));

        // Configura l'utente e il calendario
        when(userDAO.findById(userId)).thenReturn(user);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);

        when(personalMapper.toPersonalDTO(any(Personal.class))).thenReturn(personalDTO);

        // Esegui il metodo e verifica che non venga lanciata eccezione
        PersonalDTO result = personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, strategies,
                priority, description, resources, subtasks, sessions);

        assertAll(
                ()-> assertNotNull(result),
                ()-> assertEquals("Test Task", result.getName())
        );
    }
    /* Test a esito negativo */
    @Test
    void createPersonalTest_duplicateSubtaskSession() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 2;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY;
        String description = "Test description";
        List<Resource> resources = new ArrayList<>();

        Session personalSession = new Session();
        Session personalSession2 = new Session();
        personalSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        personalSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        personalSession2.setStartDate(LocalDateTime.of(2025, 3, 9, 14, 0));
        personalSession2.setEndDate(LocalDateTime.of(2025, 3, 9, 15, 0));
        List<Session> sessions = new ArrayList<>(List.of(personalSession, personalSession2));

        // Crea due subtasks che usano le stesse sessioni della lista principale (caso valido)
        Subtask subtask1 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(), new ArrayList<>(List.of(personalSession)));
        Subtask subtask2 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(), new ArrayList<>(List.of(personalSession)));
        List<Subtask> subtasks = new ArrayList<>(List.of(subtask1, subtask2));

        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null));

        when(userDAO.findById(userId)).thenReturn(user);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);

        // Aspettiamo che venga lanciata l'eccezione per sessione duplicata
        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, strategies,
                        priority, description, resources, subtasks, sessions)
        );
    }

    /* Test a esito negativo */
    @Test
    void createPersonalTest_duplicateSubtaskResources() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 2;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY;
        String description = "Test description";

        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        resource1.setName("computer");
        resource1.setType(ResourceType.EQUIPMENT);
        resource1.setValue(2);

        List<Resource> resources = new ArrayList<>(List.of(resource1, resource2));

        resource2.setName("calculator");
        resource2.setType(ResourceType.EQUIPMENT);
        resource2.setValue(3);

        Session personalSession = new Session();
        Session personalSession2 = new Session();
        personalSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        personalSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        personalSession2.setStartDate(LocalDateTime.of(2025, 3, 9, 14, 0));
        personalSession2.setEndDate(LocalDateTime.of(2025, 3, 9, 15, 0));
        List<Session> sessions = new ArrayList<>(List.of(personalSession, personalSession2));

        // Crea due subtasks che usano le stesse sessioni della lista principale (caso valido)
        Subtask subtask1 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(List.of(resource1)), new ArrayList<>(List.of(personalSession)));
        Subtask subtask2 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(List.of(resource1)), new ArrayList<>(List.of(personalSession2)));
        List<Subtask> subtasks = new ArrayList<>(List.of(subtask1, subtask2));

        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null));

        when(userDAO.findById(userId)).thenReturn(user);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);

        // Aspettiamo che venga lanciata l'eccezione per sessione duplicata
        assertThrows(IllegalArgumentException.class, () ->
                personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, strategies,
                        priority, description, resources, subtasks, sessions)
        );

    }




    /* Test a esito positivo */
    @Test
    void createPersonalTest_resourcesValidData() {
        long userId = 1L;
        String name = "Test Task";
        int totalTime = 2;
        Topic topic = Topic.ART;
        int priority = 1;
        Timetable timeSlots = Timetable.ALL_DAY;
        String description = "Test description";

        Resource resource1 = new Resource();
        Resource resource2 = new Resource();
        resource1.setName("computer");
        resource1.setType(ResourceType.EQUIPMENT);
        resource1.setValue(2);

        List<Resource> resources = new ArrayList<>(List.of(resource1, resource2));

        resource2.setName("calculator");
        resource2.setType(ResourceType.EQUIPMENT);
        resource2.setValue(3);

        Session personalSession = new Session();
        Session personalSession2 = new Session();
        personalSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        personalSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));
        personalSession2.setStartDate(LocalDateTime.of(2025, 3, 9, 14, 0));
        personalSession2.setEndDate(LocalDateTime.of(2025, 3, 9, 15, 0));
        List<Session> sessions = new ArrayList<>(List.of(personalSession, personalSession2));

        // Crea due subtasks che usano le stesse sessioni della lista principale (caso valido)
        Subtask subtask1 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(List.of(resource1)), new ArrayList<>(List.of(personalSession)));
        Subtask subtask2 = new Subtask("test subtasks", 1, 1, "sub description", new ArrayList<>(List.of(resource2)), new ArrayList<>(List.of(personalSession2)));
        List<Subtask> subtasks = new ArrayList<>(List.of(subtask1, subtask2));

        List<StrategyInstance> strategies = List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null));

        when(userDAO.findById(userId)).thenReturn(user);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);

        when(personalMapper.toPersonalDTO(any(Personal.class))).thenReturn(personalDTO);

        // Esegui il metodo e verifica che non venga lanciata eccezione
        PersonalDTO result = personalService.createPersonal(name, userId, topic, null, totalTime, timeSlots, strategies,
                priority, description, resources, subtasks, sessions);
        assertAll(
                ()-> assertNotNull(result),
                ()-> assertEquals("Test Task", result.getName())
        );

    }



    /* MODIFY PERSONALTASK TESTS */

    @Test
    void modifyPersonalTest_TaskNotFound() {
        when(personalDAO.findById(99L)).thenReturn(null);
        List<Session> sessions = new  ArrayList<>(personal.getSessions());

        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(99L, "New Name", Topic.ART, null, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
        verify(personalDAO, times(1)).findById(99L);
    }

    @Test
    void modifyPersonalTest_NoUserAssociated() {
        personal.setUser(null);  // forziamo lo scenario
        when(personalDAO.findById(1L)).thenReturn(personal);
        List<Session> sessions = new  ArrayList<>(personal.getSessions());

        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, "New Name", Topic.ART, null, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }

    /* START MANDATORY FIELDS TESTS */

    @Test
    void modifyPersonalTest_NullMandatoryFieldName() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, null, Topic.ART, null, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }

    @Test
    void modifyPersonalTest_NullMandatoryFieldTopic() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),null, null, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }

    @Test
    void modifyPersonalTest_NullMandatoryFieldTimeSlots() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),Topic.ART, null, 1, null,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }

    @Test
    void modifyPersonalTest_NullMandatoryFieldStrategies() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),Topic.ART, null, 1, Timetable.ALL_DAY,
                        null, 1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }

    @Test
    void modifyPersonalTest_NullMandatoryFieldSessions() {
        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),Topic.ART, null, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, null, null, null
                )
        );
    }

    @Test
    void modifyPersonalTest_NegativeTotalTime() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),Topic.ART, null, -1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }


    /** FALSE TEST THAT FAILS **/
    /*
    @Test
    void falseTest() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        when(personalDAO.findById(1L)).thenReturn(personal);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        Session session = new Session();
        session.setStartDate(LocalDateTime.of(2025, 4, 9, 10, 0));
        session.setEndDate(LocalDateTime.of(2025, 4, 9, 11, 0));
        calendar.setSessions(List.of(session));
        doNothing().when(calendarDAO).update(calendar);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),Topic.ART, null, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }
    */


    @Test
    void modifyPersonalTest_SKIPPEDStrategyAndDeadline() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        LocalDateTime deadline = LocalDateTime.of(2025, 3, 1, 10, 0);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),Topic.ART, deadline, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }

    @Test
    void modifyPersonalTest_SKIPPEDStrategyAndMore() {
        List<Session> sessions = new  ArrayList<>(personal.getSessions());
        List<StrategyInstance> strategies = List.of(
                new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null),
                new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS, 2, null)
        );
        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, personal.getName(),Topic.ART, null, 1, Timetable.ALL_DAY,
                        strategies, 1, "description", new ArrayList<>(), null, sessions,
                        null, null
                )
        );
    }


    @Test
    void modifyPersonalTest_ValidData() {
        Session existingSession = new Session();
        existingSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        existingSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));

        Calendar calendar = new Calendar(user);
        calendar.addSessions(List.of(existingSession));
        user.setCalendar(calendar);

        when(personalDAO.findById(1L)).thenReturn(personal);

        Session newSession = new Session();
        newSession.setStartDate(LocalDateTime.of(2025, 3, 9, 11, 30));
        newSession.setEndDate(LocalDateTime.of(2025, 3, 9, 12, 30));

        List<Session> sessions = new ArrayList<>(List.of(newSession));

        when(personalMapper.toPersonalDTO(any(Personal.class))).thenReturn(personalDTO);

        PersonalDTO result = personalService.modifyPersonal(1L, "Modified Task", Topic.ART, null, 1, Timetable.ALL_DAY,
                List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                1, "new description", new ArrayList<>(), null, sessions, null, null
        );

        assertAll(
                ()-> assertNotNull(result),
                ()-> assertEquals("Test Task", result.getName())
        );

        verify(personalDAO, times(1)).update(any(Personal.class));
        verify(personalMapper, times(1)).toPersonalDTO(any(Personal.class));
    }





    @Test
    void modifyPersonalTest_SessionOverlap() {

        // Calendario dell'utente con una sessione 10:00-11:00
        Session existingSession = new Session();
        existingSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        existingSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));


        Calendar calendar = new Calendar(user);
        calendar.addSessions(List.of(existingSession));
        user.setCalendar(calendar);


        // Personal esistente
        when(personalDAO.findById(1L)).thenReturn(personal);

        // modifico la sessione del personal
        Session newSession = new Session();
        newSession.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 30));
        newSession.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 30));

        List<Session> sessions = new ArrayList<>(List.of(newSession));

        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(
                        1L, "New Name", Topic.ART, null, 1, Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), null, sessions, null, null
                )
        );
    }

    // HO ALTERNATO CASI IN CUI LE SESSIONI LE AGGIUNGO E CASI IN CUI LE SOSTITUISCO INTERAMENTE
    @Test
    void modifyPersonalTest_duplicateSubtaskSession() {
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);

        Session session1 = new Session();
        session1.setStartDate(LocalDateTime.of(2025, 3, 9, 10, 0));
        session1.setEndDate(LocalDateTime.of(2025, 3, 9, 11, 0));

        Session session2 = new Session();
        session2.setStartDate(LocalDateTime.of(2025, 3, 9, 14, 0));
        session2.setEndDate(LocalDateTime.of(2025, 3, 9, 15, 0));
        List<Session> newSessions = List.of(session1, session2);
        personal.getSessions().addAll(newSessions);

        // Subtasks che usano la stessa sessione => duplicato
        Subtask subtask1 = new Subtask("Subtask1", 1, 1, "description", new ArrayList<>(), new ArrayList<>(List.of(session1)));
        Subtask subtask2 = new Subtask("Subtask2", 1, 1, "desc", new ArrayList<>(), new ArrayList<>(List.of(session1)));
        List<Subtask> subtasks = List.of(subtask1, subtask2);

        assertThrows(IllegalArgumentException.class, () ->
                personalService.modifyPersonal(1L, "New Name", Topic.ART, null, 1,
                        Timetable.ALL_DAY,
                        List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS, null, null)),
                        1, "description", new ArrayList<>(), subtasks, personal.getSessions(), null, null
                )
        );
    }

    // VERIFICO CHE SI SUPERI LA LINEA DI ISTRUZIONE CHE RIMUOVE IL TASK
    @Test
    void deletePersonalSuccessTest(){
        when(personalDAO.findById(1L)).thenReturn(personal);
        personalService.deletePersonal(1L);
        verify(calendarDAO, times(1)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(1)).delete(1L);
    }

    @Test
    void deletePersonalFailTest(){
        when(personalDAO.findById(100L)).thenReturn(null);
        assertThrows( IllegalArgumentException.class, () -> personalService.deletePersonal(100L));
        verify(calendarDAO, times(0)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(0)).delete(1L);
    }


    @Test
    void moveToCalendarSuccessTest(){
        when(personalDAO.findById(1L)).thenReturn(personal);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        personalService.moveToCalendar(1L);
        verify(calendarDAO, times(1)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(1)).update(personal);
        verify(userDAO, times(1)).update(personal.getUser());
    }

    @Test
    void moveToCalendarFailTest(){
        when(personalDAO.findById(100L)).thenReturn(null);
        assertThrows( IllegalArgumentException.class, () -> personalService.moveToCalendar(100L));
        verify(calendarDAO, times(0)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(0)).update(personal);
        verify(userDAO, times(0)).update(personal.getUser());

    }

    @Test
    void completeSessionTest_subtaskSuccess(){
        when(personalDAO.findById(1L)).thenReturn(personal);
        when(sessionDAO.findById(1L)).thenReturn(session);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        calendar.addSessions(personal.getSessions());
        personal.setState(TaskState.INPROGRESS);
        Subtask subtask = new Subtask();
        subtask.setName("Subtask1");
        subtask.setDescription("description");
        subtask.setTotalTime(1);
        subtask.setLevel(1);
        Session subSession = new Session(session.getStartDate(),session.getEndDate());
        subtask.setSessions(List.of(subSession));
        personal.setSubtasks(List.of(subtask));

        personalService.completeSession(personal.getId(),session.getId());
        assertAll(
                ()-> assertTrue(personal.getSessions().contains(session)),
                ()-> assertTrue(subtask.getSessions().contains(subSession)),
                ()-> assertSame( SessionState.COMPLETED,subtask.getSessions().getFirst().getState())
        );
        verify(subtaskDAO,times(1)).update(subtask);
        verify(personalDAO,times(1)).update(personal);
    }

    @Test
    void completeSessionTest_personalFail(){
        when(personalDAO.findById(99L)).thenReturn(null);
        when(sessionDAO.findById(1L)).thenReturn(session);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        calendar.addSessions(personal.getSessions());
        Subtask subtask = new Subtask();
        subtask.setName("Subtask1");
        subtask.setDescription("description");
        subtask.setTotalTime(1);
        subtask.setLevel(1);
        Session subSession = new Session(session.getStartDate(),session.getEndDate());
        subtask.setSessions(List.of(subSession));

        assertThrows(IllegalArgumentException.class, () -> personalService.completeSession(99L, session.getId()));
        verify(personalDAO, times(1)).findById(99L);// Non si arriva a cercare la session
        verify(subtaskDAO, never()).update(any());
        verify(personalDAO, never()).update(any());
    }
    @Test
    void completeSessionTest_sessionFail(){
        when(sessionDAO.findById(99L)).thenReturn(null);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        calendar.addSessions(personal.getSessions());
        Subtask subtask = new Subtask();
        subtask.setName("Subtask1");
        subtask.setDescription("description");
        subtask.setTotalTime(1);
        subtask.setLevel(1);
        Session subSession = new Session(session.getStartDate(),session.getEndDate());
        subtask.setSessions(List.of(subSession));

        assertThrows(IllegalArgumentException.class, () -> personalService.completeSession(personal.getId(), 99L));
        verify(personalDAO, times(1)).findById(personal.getId());// Non si arriva a cercare la session
        verify(subtaskDAO, never()).update(any());
        verify(personalDAO, never()).update(any());
    }

    @Test
    void freezeTaskTest_success(){
        when(personalDAO.findById(1L)).thenReturn(personal);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        calendar.addSessions(personal.getSessions());
        personalService.freezeTask(1L);
        verify(calendarDAO, times(1)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(1)).update(personal);
    }

    @Test
    void freezeTaskTest_fail(){
        when(personalDAO.findById(100L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> personalService.freezeTask(100L));
        verify(calendarDAO, times(0)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(0)).update(personal);
    }

    @Test
    void handleLimitExceededTest_success(){
        when(personalDAO.findById(1L)).thenReturn(personal);
        when(sessionDAO.findById(1L)).thenReturn(session);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        calendar.addSessions(personal.getSessions());
        personalService.handleLimitExceeded(session.getId(),personal.getId());
        verify(calendarDAO, times(1)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(1)).update(personal);
    }

    @Test
    void handleLimitExceededTest_personalFail(){
        when(personalDAO.findById(100L)).thenReturn(null);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        calendar.addSessions(personal.getSessions());
        assertThrows(IllegalArgumentException.class, () -> personalService.handleLimitExceeded(session.getId(),100L));
        verify(calendarDAO, times(0)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(0)).update(personal);
    }

    @Test
    void handleLimitExceededTest_sessionFail(){
        when(sessionDAO.findById(100L)).thenReturn(null);
        Calendar calendar = new Calendar(user);
        user.setCalendar(calendar);
        calendar.addSessions(personal.getSessions());
        assertThrows(IllegalArgumentException.class, () -> personalService.handleLimitExceeded(100L,personal.getId()));
        verify(calendarDAO, times(0)).update(personal.getUser().getCalendar());
        verify(personalDAO, times(0)).update(personal);
    }

















}

