package com.domain;
import domain.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SharedEntityTest {

    private Shared shared;
    private Profile profile;
    private Session session1;
    private Session session2;
    private User user;
    private Calendar calendar;


    @BeforeEach
    void setUp() {
        shared = new Shared();
        user = new User();
        user.setId(1L);
        shared.setId(1L);
        shared.setName("Test Task");
        shared.setUser(user);
        session1 = new Session();
        session2 = new Session();
        shared.setState(TaskState.TODO);
        user.setId(1L);
        calendar = new Calendar();
        calendar.setId(1L);
        user.setCalendar(calendar);
        calendar.setUser(user);
        shared.setUser(user);
        profile = new Profile();
        profile.setId(1L);
        Map<Topic, Integer> topicsMap = new HashMap<>();
        topicsMap.put(Topic.PROGRAMMING, 5);
        topicsMap.put(Topic.ART, 3);
        profile.setTopics(topicsMap);


        session1.setId(1L);
        session1.setStartDate(LocalDateTime.of(2025, 3, 20, 10, 0));
        session1.setEndDate(LocalDateTime.of(2025, 3, 20, 11, 0));
        session1.setState(SessionState.PROGRAMMED);
        session2.setId(2L);
        session2.setStartDate(LocalDateTime.of(2025, 3, 20, 12, 0));
        session2.setEndDate(LocalDateTime.of(2025, 3, 20, 13, 0));
        session2.setState(SessionState.PROGRAMMED);

        List<Session> sessions = new ArrayList<>(List.of(session1,session2));
        shared.setSessions(sessions);
    }

    @Test
    void completeSessionTest_successNotLast(){
        shared.setState(TaskState.INPROGRESS);
        shared.completeSession(session1);
        assertAll(
                ()-> assertSame(session1.getState(), SessionState.COMPLETED),
                ()-> assertEquals(shared.getConsecutiveSkippedSessions(),0),
                ()-> assertEquals(shared.getPercentageOfCompletion(),50)
        );

    }

    @Test
    void completeSessionTest_successLast(){
        shared.setState(TaskState.INPROGRESS);
        shared.completeSession(session2);
        assertAll(
                ()-> assertSame(session1.getState(), SessionState.COMPLETED),
                ()-> assertSame(session2.getState(), SessionState.COMPLETED),
                ()-> assertEquals(shared.getConsecutiveSkippedSessions(),0),
                ()-> assertEquals(shared.getPercentageOfCompletion(),100),
                ()-> assertSame(shared.getState(),TaskState.FINISHED),
                ()-> assertEquals(shared.getPercentageOfCompletion(),100)
        );
    }

    @Test
    void completeSessionTest_failAlienSession(){
        shared.setState(TaskState.INPROGRESS);
        Session alienSession = new Session();
        alienSession.setId(5L);
        alienSession.setState(SessionState.PROGRAMMED);
        assertThrows(EntityNotFoundException.class, () -> shared.completeSession(alienSession));
    }

    @Test
    void completeSessionTest_failSKIPPED(){
        shared.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.SKIPPED);
        assertThrows(IllegalStateException.class, () -> shared.completeSession(session1));

    }
    @Test
    void completeSessionTest_failCOMPLETED(){
        shared.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.COMPLETED);
        assertThrows(IllegalStateException.class, () -> shared.completeSession(session1));
    }

    /* TEST SULLE STRATEGIE */
    @Test
    void setStrategiesTest_SKIPPEDSuccess(){
        shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null))));
        assertAll(
                ()-> assertEquals(shared.getStrategies().size(),1),
                ()-> assertSame(shared.getStrategies().getFirst().getStrategy(),DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)
        );
    }

    @Test
    void setStrategiesTest_SKIPPEDFail(){
        assertThrows(IllegalArgumentException.class,() -> shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,1,1)))));

    }

    @Test
    void setStrategiesTest_EACHSuccess(){
        shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        assertAll(
                ()-> assertEquals(shared.getStrategies().size(),1),
                ()-> assertSame(shared.getStrategies().getFirst().getStrategy(),DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)
        );
    }

    @Test
    void setStrategiesTest_FREEZETOTSuccess(){
        shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,1,null))));
        assertAll(
                ()-> assertEquals(shared.getStrategies().size(),1),
                ()-> assertSame(shared.getStrategies().getFirst().getStrategy(),DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS)
        );
    }
    @Test
    void setStrategiesTest_FREEZETOTFail(){
        assertThrows(IllegalArgumentException.class, () ->  shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,null,1)))));
    }

    @Test
    void setStrategiesTest_FREEZECONSECSuccess(){
        shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS,null,1))));
        assertAll(
                ()-> assertEquals(shared.getStrategies().size(),1),
                ()-> assertSame(shared.getStrategies().getFirst().getStrategy(),DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS)
        );
    }

    @Test
    void setStrategiesTest_FREEZECONSECFail(){
        assertThrows(IllegalArgumentException.class, ()-> shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS,1,null)))));
    }

    @Test
    void toCalendarTest_success(){
        shared.toCalendar();
        assertAll(
                ()-> assertSame(shared.getState(),TaskState.INPROGRESS),
                ()-> assertSame(shared.getIsInProgress(),true),
                ()-> assertThat(calendar.getSessions()).containsAll(shared.getSessions()),
                ()-> assertThat(shared.getDateOnFeed()).isNotNull(),
                ()-> assertThat(shared.getIsOnFeed()).isTrue()
        );
    }

    @Test
    void toCalendarTest_stateFail(){
        shared.setState(TaskState.FINISHED);
        assertThrows(UnsupportedOperationException.class, () -> shared.toCalendar());
    }



    @Test
    void toCalendarTest_inProgressFail(){
        shared.setIsInProgress(true);
        assertThrows(UnsupportedOperationException.class, () -> shared.toCalendar());
    }

    @Test
    void modifyTaskTest_successFromTODO(){
        shared.modifyTask();
        assertFalse(shared.getIsInProgress());
    }
    @Test
    void modifyTaskTest_successFromIsInProgress(){
        calendar.addSessions(shared.getSessions());
        shared.setState(TaskState.INPROGRESS);
        shared.setIsInProgress(true);
        shared.modifyTask();
        assertAll(
                ()-> assertFalse(shared.getIsInProgress()),
                ()-> assertSame(shared.getState(),TaskState.FREEZED),
                ()-> assertThat(shared.getIsOnFeed()).isTrue()
        );
    }

    @Test
    void modifyTaskTest_failFINISHED(){
        shared.setState(TaskState.FINISHED);
        assertThrows(UnsupportedOperationException.class, () -> shared.modifyTask());
    }

    @Test
    void completeTaskBySessionsTest_success(){
        shared.setState(TaskState.INPROGRESS);
        calendar.addSessions(shared.getSessions());
        session1.setState(SessionState.COMPLETED);
        shared.completeTaskBySessions();
        assertAll(
                ()-> assertSame(shared.getState(),TaskState.FINISHED),
                ()-> assertFalse(shared.getIsInProgress()),
                ()-> assertEquals(shared.getPercentageOfCompletion(),100),
                ()-> assertThat(shared.getIsOnFeed()).isFalse()
        );
    }

    @Test
    void completeTaskBySessionsTest_successWithSubtask(){
        Subtask subtask = new Subtask();
        subtask.setName("subtask");
        subtask.setLevel(1);
        subtask.setDescription("Hello world");
        subtask.setTotalTime(1);
        shared.setSubtasks(List.of(subtask));
        subtask.setSessions(List.of(session1));
        shared.setState(TaskState.INPROGRESS);
        calendar.addSessions(shared.getSessions());
        session1.setState(SessionState.COMPLETED);
        shared.completeTaskBySessions();
        assertAll(
                ()-> assertSame(shared.getState(),TaskState.FINISHED),
                ()-> assertFalse(shared.getIsInProgress()),
                ()-> assertEquals(shared.getPercentageOfCompletion(),100)

        );
    }

    @Test
    void completeTaskBySessionsTest_failTODO(){
        assertThrows(IllegalStateException.class, () -> shared.completeTaskBySessions());
    }

    @Test
    void completeTaskBySessionsTest_failFINISHED(){
        shared.setState(TaskState.FINISHED);
        assertThrows(IllegalStateException.class, () -> shared.completeTaskBySessions());
    }

    @Test
    void completeTaskBySessionsTest_failFREEZED(){
        shared.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class, () -> shared.completeTaskBySessions());
    }

    @Test
    void completeTaskBySessionsTest_failForSessionsPROGRAMMED(){
        shared.setState(TaskState.INPROGRESS);
        calendar.addSessions(shared.getSessions());
        assertThrows(IllegalStateException.class, () -> shared.completeTaskBySessions());
    }

    @Test
    void completeBySessionsAndChooseBestCommentTest_success(){
        shared.setTopic(Topic.ART);
        shared.setState(TaskState.INPROGRESS);
        user.getTasks().add(shared);
        Comment comment = new Comment();
        comment.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        user2.setPersonalProfile(profile);
        comment.setAuthor(user2);
        shared.getComments().add(comment);
        session1.setState(SessionState.COMPLETED);
        shared.completeBySessionsAndChooseBestComment(comment);
        assertAll(
                ()-> assertSame(shared.getState(),TaskState.FINISHED),
                ()-> assertFalse(shared.getIsInProgress()),
                ()-> assertEquals(shared.getPercentageOfCompletion(),100),
                ()-> assertThat(shared.getIsOnFeed()).isFalse(),
                ()-> assertThat(comment.getIsBest()).isTrue()
        );
    }

    @Test
    void completeBySessionsAndChooseBestCommentTest_failComment(){
        shared.setState(TaskState.INPROGRESS);
        shared.setTopic(Topic.ART);
        user.getTasks().add(shared);
        Comment comment = new Comment();
        comment.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        user2.setPersonalProfile(profile);
        session1.setState(SessionState.COMPLETED);
        comment.setAuthor(user2);
        assertAll(
                ()-> assertThrows(EntityNotFoundException.class, () -> shared.completeBySessionsAndChooseBestComment(comment)),
                ()-> assertThat(shared.getPercentageOfCompletion()).isNotEqualTo(100)
        );
    }

    @Test
    void completeBySessionsAndChooseBestComment_failFINISHED(){
        shared.setState(TaskState.FINISHED);
        shared.setTopic(Topic.ART);
        user.getTasks().add(shared);
        Comment comment = new Comment();
        comment.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        user2.setPersonalProfile(profile);
        comment.setAuthor(user2);
        shared.getComments().add(comment);
        session1.setState(SessionState.COMPLETED);
        assertAll(
                ()-> assertThrows(IllegalStateException.class, () -> shared.completeBySessionsAndChooseBestComment(comment)),
                ()-> assertThat(shared.getPercentageOfCompletion()).isNotEqualTo(100)
        );
    }


    @Test
    void forcedCompletion_success(){
        shared.setState(TaskState.INPROGRESS);
        shared.forcedCompletion();
        assertAll(
                ()-> assertSame(shared.getState(),TaskState.FINISHED),
                ()-> assertThat(shared.getIsOnFeed()).isFalse(),
                ()-> assertEquals(shared.getPercentageOfCompletion(),100)
        );
    }

    @Test
    void forcedCompletionTest_FailTODO(){
        assertThrows(IllegalStateException.class,() -> shared.forcedCompletion());
    }

    @Test
    void forcedCompletionTest_FailFREEZED(){
        shared.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class,() -> shared.forcedCompletion());
    }

    @Test
    void forcedCompletionTest_FailFINISHED(){
        shared.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class,() -> shared.forcedCompletion());
    }

    @Test
    void SkipSessionTest_SuccessNoRescheduling() {
        shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null))));
        int initialSkipped = shared.getSkippedSessions();
        int initialConsecutive = shared.getConsecutiveSkippedSessions();
        shared.skipSession(session1);
        assertAll(
                ()-> assertEquals(SessionState.SKIPPED, session1.getState()),
                ()-> assertEquals(initialSkipped + 1, shared.getSkippedSessions()),
                ()-> assertEquals(initialConsecutive + 1, shared.getConsecutiveSkippedSessions())
        );
    }

    @Test
    void skipSessionTest_SuccessWithAddAtEnd() {
        shared.setDeadline(LocalDateTime.of(2025, 3, 25, 23, 59)); // per esempio
        shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        LocalDateTime oldStart = session1.getStartDate();
        LocalDateTime oldEnd = session1.getEndDate();
        shared.setTimetable(Timetable.ALL_DAY);

        shared.skipSession(session1);

        assertAll(
                ()-> assertEquals(SessionState.PROGRAMMED, session1.getState()), //PROGRAMMED PERCHÉ L'HO RIMANDATA
                ()-> assertTrue(session1.getStartDate().isAfter(oldStart)),
                ()-> assertTrue(session1.getEndDate().isAfter(oldEnd)),
                ()-> assertTrue(shared.getSessions().contains(session1))
        );
    }

    @Test
    void skipSessionTest_successWithSubtask(){
        shared.setDeadline(LocalDateTime.of(2025, 3, 25, 23, 59)); // per esempio
        shared.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        LocalDateTime oldStart = session1.getStartDate();
        LocalDateTime oldEnd = session1.getEndDate();
        Subtask subtask = new Subtask();
        subtask.setName("subtask");
        subtask.setSessions(new ArrayList<>(List.of(new Session(session1.getStartDate(),session1.getEndDate()))));
        shared.setTimetable(Timetable.ALL_DAY);

        shared.skipSession(session1);

        assertAll(
                ()-> assertEquals(SessionState.PROGRAMMED, session1.getState()),
                ()-> assertTrue(session1.getStartDate().isAfter(oldStart)),
                ()-> assertTrue(session1.getEndDate().isAfter(oldEnd)),
                ()-> assertTrue(shared.getSessions().contains(session1))
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
                ()-> assertFalse(shared.getSessions().contains(alienSession)),
                ()-> assertThrows(EntityNotFoundException.class, () -> shared.skipSession(alienSession))
        );
    }

    @Test
    void autoSkipIfNotCompletedTest_SessionNotFound() {
        Session unknownSession = new Session();
        unknownSession.setId(999L);
        assertThrows(IllegalStateException.class,
                () -> shared.autoSkipIfNotCompleted(unknownSession));
    }

    @Test
    void autoSkipIfNotCompletedTest_nowBeforeNextSession() {
        session2.setStartDate(LocalDateTime.now().plusHours(1));
        shared.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }

    @Test
    void testAutoSkipIfNotCompleted_nowAfterNextSessionStarted() {
        session1.setStartDate(LocalDateTime.now().minusHours(2));
        session1.setEndDate(LocalDateTime.now().minusHours(1));
        session2.setStartDate(LocalDateTime.now());
        session2.setEndDate(LocalDateTime.now().plusHours(1));
        shared.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.SKIPPED, session1.getState());
    }

    @Test
    void testAutoSkipIfNotCompleted_nowBeforeOneDayAfterThisSession() {
        session1.setStartDate(LocalDateTime.now().minusHours(13));
        session1.setEndDate(LocalDateTime.now().minusHours(12));
        shared.setSessions(new ArrayList<>(List.of(session1)));
        shared.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }


    @Test
    void testRemoveAndFreezeTask_Success() {
        calendar.setSessions(new ArrayList<>(List.of(session1,session2)));
        shared.removeAndFreezeTask(user);

        assertAll(
                ()-> assertFalse(user.getCalendar().getSessions().contains(session1)),
                ()-> assertFalse(user.getCalendar().getSessions().contains(session2)),
                ()-> assertEquals(TaskState.FREEZED, shared.getState()),
                ()-> assertThat(shared.getIsOnFeed()).isFalse(),
                ()-> assertFalse(shared.getIsInProgress())
        );
    }

    @Test
    void testRemoveAndFreezeTask_NoSessionsInCalendar() {
        calendar.setSessions(new ArrayList<>());
        shared.removeAndFreezeTask(user);
        assertAll(
                ()-> assertEquals(TaskState.FREEZED, shared.getState()),
                ()-> assertFalse(shared.getIsInProgress())
        );
    }


}



