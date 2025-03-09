package com.domain;

import domain.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PersonalEntityTest {

    private Personal personal;

    private Session session1;
    private Session session2;
    private User user;
    private Calendar calendar;

    @BeforeEach
    void setUp() {
        personal = new Personal();
        user = new User();
        user.setId(1L);
        personal.setId(1L);
        personal.setName("Test Task");
        personal.setUser(user);
        session1 = new Session();
        session2 = new Session();
        personal.setState(TaskState.TODO);
        user.setId(1L);
        calendar = new Calendar();
        calendar.setId(1L);
        user.setCalendar(calendar);
        calendar.setUser(user);
        /*calendar.setSessions(new ArrayList<>(List.of(session1, session2)));*/
        personal.setUser(user);

        session1.setId(1L);
        session1.setStartDate(LocalDateTime.of(2025, 3, 20, 10, 0));
        session1.setEndDate(LocalDateTime.of(2025, 3, 20, 11, 0));
        session1.setState(SessionState.PROGRAMMED);
        session2.setId(2L);
        session2.setStartDate(LocalDateTime.of(2025, 3, 20, 12, 0));
        session2.setEndDate(LocalDateTime.of(2025, 3, 20, 13, 0));
        session2.setState(SessionState.PROGRAMMED);

        List<Session> sessions = new ArrayList<>(List.of(session1,session2));
        personal.setSessions(sessions);
    }

    @Test
    void completeSessionTest_successNotLast(){
        personal.setState(TaskState.INPROGRESS);
        personal.completeSession(session1);
        assertAll(
                ()-> assertSame(session1.getState(), SessionState.COMPLETED),
                ()-> assertEquals(personal.getConsecutiveSkippedSessions(),0),
                ()-> assertEquals(personal.getPercentageOfCompletion(),50)
        );

    }

    @Test
    void completeSessionTest_successLast(){
        personal.setState(TaskState.INPROGRESS);
        personal.completeSession(session2);
        assertAll(
                ()-> assertSame(session1.getState(), SessionState.COMPLETED),
                ()-> assertSame(session2.getState(), SessionState.COMPLETED),
                ()-> assertEquals(personal.getConsecutiveSkippedSessions(),0),
                ()-> assertEquals(personal.getPercentageOfCompletion(),100),
                ()-> assertSame(personal.getState(),TaskState.FINISHED),
                ()-> assertEquals(personal.getPercentageOfCompletion(),100)
        );
    }

    @Test
    void completeSessionTest_failAlienSession(){
        personal.setState(TaskState.INPROGRESS);
        Session alienSession = new Session();
        alienSession.setId(5L);
        alienSession.setState(SessionState.PROGRAMMED);
        assertThrows(EntityNotFoundException.class, () -> personal.completeSession(alienSession));
    }

    @Test
    void completeSessionTest_failSKIPPED(){
        personal.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.SKIPPED);
        assertThrows(IllegalStateException.class, () -> personal.completeSession(session1));

    }
    @Test
    void completeSessionTest_failCOMPLETED(){
        personal.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.COMPLETED);
        assertThrows(IllegalStateException.class, () -> personal.completeSession(session1));
    }

    /* TEST SULLE STRATEGIE */
    @Test
    void setStrategiesTest_SKIPPEDSuccess(){
        personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null))));
        assertAll(
                ()-> assertEquals(personal.getStrategies().size(),1),
                ()-> assertSame(personal.getStrategies().getFirst().getStrategy(),DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)
        );
    }

    @Test
    void setStrategiesTest_SKIPPEDFail(){
        assertThrows(IllegalArgumentException.class,() -> personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,1,1)))));

    }

    @Test
    void setStrategiesTest_EACHSuccess(){
        personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        assertAll(
                ()-> assertEquals(personal.getStrategies().size(),1),
                ()-> assertSame(personal.getStrategies().getFirst().getStrategy(),DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)
        );
    }

    @Test
    void setStrategiesTest_FREEZETOTSuccess(){
        personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,1,null))));
        assertAll(
                ()-> assertEquals(personal.getStrategies().size(),1),
                ()-> assertSame(personal.getStrategies().getFirst().getStrategy(),DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS)
        );
    }
    @Test
    void setStrategiesTest_FREEZETOTFail(){
        assertThrows(IllegalArgumentException.class, () ->  personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,null,1)))));
    }

    @Test
    void setStrategiesTest_FREEZECONSECSuccess(){
        personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS,null,1))));
        assertAll(
                ()-> assertEquals(personal.getStrategies().size(),1),
                ()-> assertSame(personal.getStrategies().getFirst().getStrategy(),DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS)
        );
    }

    @Test
    void setStrategiesTest_FREEZECONSECFail(){
        assertThrows(IllegalArgumentException.class, ()-> personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS,1,null)))));
    }

    @Test
    void commonToCalendarLogicTest_success(){
        personal.commonToCalendarLogic(user);
        assertAll(
                ()-> assertSame(personal.getState(),TaskState.INPROGRESS),
                ()-> assertSame(personal.getIsInProgress(),true),
                ()-> assertTrue(calendar.getSessions().containsAll(personal.getSessions()))
        );
    }

    @Test
    void commonToCalendarLogicTest_stateFail(){
        personal.setState(TaskState.FINISHED);
        assertThrows(UnsupportedOperationException.class, () -> personal.commonToCalendarLogic(user));
    }

    @Test
    void commonToCalendarLogicTest_inProgressFail(){
        personal.setIsInProgress(true);
        assertThrows(UnsupportedOperationException.class, () -> personal.commonToCalendarLogic(user));
    }

    @Test
    void commonModifyLogicTest_successFromTODO(){
        personal.commonModifyLogic(user);
        assertFalse(personal.getIsInProgress());
    }
    @Test
    void commonModifyLogicTest_successFromIsInProgress(){
        calendar.addSessions(personal.getSessions());
        personal.setState(TaskState.INPROGRESS);
        personal.setIsInProgress(true);
        personal.commonModifyLogic(user);
        assertAll(
                ()-> assertFalse(personal.getIsInProgress()),
                ()-> assertSame(personal.getState(),TaskState.FREEZED)
        );
    }

    @Test
    void commonModifyLogicTest_failFINISHED(){
        personal.setState(TaskState.FINISHED);
        assertThrows(UnsupportedOperationException.class, () -> personal.commonModifyLogic(user));
    }

    @Test
    void commonCompleteBySessionsLogicTest_success(){
        personal.setState(TaskState.INPROGRESS);
        calendar.addSessions(personal.getSessions());
        session1.setState(SessionState.COMPLETED);
        personal.commonCompleteBySessionsLogic(user);
        assertAll(
                ()-> assertSame(personal.getState(),TaskState.FINISHED),
                ()-> assertFalse(personal.getIsInProgress()),
                ()-> assertEquals(personal.getPercentageOfCompletion(),100)
        );
    }

    @Test
    void commonCompleteBySessionsLogicTest_successWithSubtask(){
        Subtask subtask = new Subtask();
        subtask.setName("subtask");
        subtask.setLevel(1);
        subtask.setDescription("Hello world");
        subtask.setTotalTime(1);
        personal.setSubtasks(List.of(subtask));
        subtask.setSessions(List.of(session1));
        personal.setState(TaskState.INPROGRESS);
        calendar.addSessions(personal.getSessions());
        session1.setState(SessionState.COMPLETED);
        personal.commonCompleteBySessionsLogic(user);
        assertAll(
                ()-> assertSame(personal.getState(),TaskState.FINISHED),
                ()-> assertFalse(personal.getIsInProgress()),
                ()-> assertEquals(personal.getPercentageOfCompletion(),100)
        );
    }

    @Test
    void commonCompleteBySessionsLogicTest_failTODO(){
        assertThrows(IllegalStateException.class, () -> personal.commonCompleteBySessionsLogic(user));
    }

    @Test
    void commonCompleteBySessionsLogicTest_failFINISHED(){
        personal.setState(TaskState.FINISHED);
        assertThrows(IllegalStateException.class, () -> personal.commonCompleteBySessionsLogic(user));
    }

    @Test
    void commonCompleteBySessionsLogicTest_failFREEZED(){
        personal.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class, () -> personal.commonCompleteBySessionsLogic(user));
    }

    @Test
    void commonCompleteBySessionsLogicTest_failForSessionsPROGRAMMED(){
        personal.setState(TaskState.INPROGRESS);
        calendar.addSessions(personal.getSessions());
        assertThrows(IllegalStateException.class, () -> personal.commonCompleteBySessionsLogic(user));
    }


    @Test
    void commonForcedCompletionLogicTest_success(){
        personal.setState(TaskState.INPROGRESS);
        personal.commonForcedCompletionLogic(user);
        assertAll(
                ()-> assertSame(personal.getState(),TaskState.FINISHED),
                ()-> assertEquals(personal.getPercentageOfCompletion(),100)
        );
    }

    @Test
    void commonForcedCompletionLogicTest_FailTODO(){
        assertThrows(IllegalStateException.class,() -> personal.commonForcedCompletionLogic(user));
    }

    @Test
    void commonForcedCompletionLogicTest_FailFREEZED(){
        personal.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class,() -> personal.commonForcedCompletionLogic(user));
    }

    @Test
    void commonForcedCompletionLogicTest_FailFINISHED(){
        personal.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class,() -> personal.commonForcedCompletionLogic(user));
    }

    @Test
    void SkipSessionTest_SuccessNoRescheduling() {
        personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null))));
        int initialSkipped = personal.getSkippedSessions();
        int initialConsecutive = personal.getConsecutiveSkippedSessions();
        personal.skipSession(session1);
        assertAll(
                ()-> assertEquals(SessionState.SKIPPED, session1.getState()),
                ()-> assertEquals(initialSkipped + 1, personal.getSkippedSessions()),
                ()-> assertEquals(initialConsecutive + 1, personal.getConsecutiveSkippedSessions())
        );
    }

    @Test
    void skipSessionTest_SuccessWithAddAtEnd() {
        personal.setDeadline(LocalDateTime.of(2025, 3, 25, 23, 59)); // per esempio
        personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        LocalDateTime oldStart = session1.getStartDate();
        LocalDateTime oldEnd = session1.getEndDate();
        personal.setTimetable(Timetable.ALL_DAY);

        personal.skipSession(session1);

        assertAll(
                ()-> assertEquals(SessionState.PROGRAMMED, session1.getState()), //PROGRAMMED PERCHÉ L'HO RIMANDATA
                ()-> assertTrue(session1.getStartDate().isAfter(oldStart)),
                ()-> assertTrue(session1.getEndDate().isAfter(oldEnd)),
                ()-> assertTrue(personal.getSessions().contains(session1))
        );
    }

    @Test
    void skipSessionTest_successWithSubtask(){
        personal.setDeadline(LocalDateTime.of(2025, 3, 25, 23, 59)); // per esempio
        personal.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        LocalDateTime oldStart = session1.getStartDate();
        LocalDateTime oldEnd = session1.getEndDate();
        Subtask subtask = new Subtask();
        subtask.setName("subtask");
        subtask.setSessions(new ArrayList<>(List.of(new Session(session1.getStartDate(),session1.getEndDate()))));
        subtask.setSessions(new ArrayList<>(List.of(session1)));
        personal.setTimetable(Timetable.ALL_DAY);

        personal.skipSession(session1);

        assertAll(
                ()-> assertEquals(SessionState.PROGRAMMED, session1.getState()),
                ()-> assertTrue(session1.getStartDate().isAfter(oldStart)),
                ()-> assertTrue(session1.getEndDate().isAfter(oldEnd)),
                ()-> assertTrue(personal.getSessions().contains(session1))
        );
    }

    @Test
    void skipSessionTest_SessionNotFound() {
        Session alienSession = new Session();
        alienSession.setId(999L);
        alienSession.setStartDate(LocalDateTime.now());
        alienSession.setEndDate(LocalDateTime.now().plusHours(1));
        alienSession.setState(SessionState.PROGRAMMED);

        assertAll(
                ()-> assertFalse(personal.getSessions().contains(alienSession)),
                ()-> assertThrows(EntityNotFoundException.class, () -> personal.skipSession(alienSession))
        );
    }

    @Test
    void autoSkipIfNotCompletedTest_SessionNotFound() {
        Session unknownSession = new Session();
        unknownSession.setId(999L);
        assertThrows(IllegalStateException.class,
                () -> personal.autoSkipIfNotCompleted(unknownSession));
    }

    @Test
    void autoSkipIfNotCompletedTest_nowBeforeNextSession() {
        session2.setStartDate(LocalDateTime.now().plusHours(1));
        personal.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }

    @Test
    void testAutoSkipIfNotCompleted_nowAfterNextSessionStarted() {
        session1.setStartDate(LocalDateTime.now().minusHours(2));
        session1.setEndDate(LocalDateTime.now().minusHours(1));
        session2.setStartDate(LocalDateTime.now());
        session2.setEndDate(LocalDateTime.now().plusHours(1));
        personal.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.SKIPPED, session1.getState());
    }

    @Test
    void testAutoSkipIfNotCompleted_nowBeforeOneDayAfterThisSession() {
        session1.setStartDate(LocalDateTime.now().minusHours(13));
        session1.setEndDate(LocalDateTime.now().minusHours(12));
        personal.setSessions(new ArrayList<>(List.of(session1)));
        personal.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }


    @Test
    void testRemoveAndFreezeTask_Success() {
        calendar.setSessions(new ArrayList<>(List.of(session1,session2)));
        personal.removeAndFreezeTask(user);

        assertAll(
                ()-> assertFalse(user.getCalendar().getSessions().contains(session1)),
                ()-> assertFalse(user.getCalendar().getSessions().contains(session2)),
                ()-> assertEquals(TaskState.FREEZED, personal.getState()),
                ()-> assertFalse(personal.getIsInProgress())
        );
    }

    @Test
    void testRemoveAndFreezeTask_NoSessionsInCalendar() {
        calendar.setSessions(new ArrayList<>());
        personal.removeAndFreezeTask(user);
        assertAll(
                ()-> assertEquals(TaskState.FREEZED, personal.getState()),
                ()-> assertFalse(personal.getIsInProgress())
        );
    }


}





















