package com.domain;


import domain.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class GroupEntityTest {

    private Group group;

    private Session session1;
    private Session session2;
    private Session session3;

    private Calendar calendar;
    private Calendar calendar2;
    private Calendar calendar3;

    private User user;
    private User member;
    private User member2;

    private Subtask subtask;
    private Subtask subtask2;
    private Subtask subtask3;
    private TaskCalendar taskCalendar;

    private TakenSubtask takenSubtask3;

    @BeforeEach
    void setUp() {
        group = new Group();
        user = new User();
        user.setId(1L);
        member = new User();
        member.setId(2L);
        member2 = new User();
        member2.setId(3L);
        group.setId(1L);
        group.setName("Test Task");
        group.setUser(user);
        group.addMember(user);
        group.addMember(member);
        group.addMember(member2);
        group.setTotalTime(3);
        session1 = new Session();
        session2 = new Session();
        session3 = new Session();
        group.setState(TaskState.TODO);
        group.setUser(user);

        calendar = new Calendar();
        calendar.setId(1L);
        user.setCalendar(calendar);
        calendar.setUser(user);

        calendar2 = new Calendar();
        calendar2.setId(2L);
        member.setCalendar(calendar2);
        calendar2.setUser(member);

        calendar3 = new Calendar();
        calendar3.setId(3L);
        member2.setCalendar(calendar3);
        calendar3.setUser(member2);

        Profile profile = new Profile();
        profile.setUsername("MARCO");
        profile.setId(1L);
        Map<Topic, Integer> topicsMap = new HashMap<>();
        topicsMap.put(Topic.PROGRAMMING, 5);
        topicsMap.put(Topic.ART, 3);
        profile.setTopics(topicsMap);
        user.setPersonalProfile(profile);

        Profile profile2 = new Profile();
        profile2.setId(2L);
        profile2.setUsername("MARIA");
        Map<Topic, Integer> topicsMap2 = new HashMap<>();
        topicsMap2.put(Topic.PROGRAMMING, 5);
        topicsMap2.put(Topic.ART, 3);
        profile2.setTopics(topicsMap2);
        member.setPersonalProfile(profile2);

        Profile profile3 = new Profile();
        profile3.setId(2L);
        profile3.setUsername("BRUNO");
        Map<Topic, Integer> topicsMap3 = new HashMap<>();
        topicsMap3.put(Topic.PROGRAMMING, 5);
        topicsMap3.put(Topic.ART, 3);
        profile3.setTopics(topicsMap3);
        member2.setPersonalProfile(profile3);


        taskCalendar = new TaskCalendar();
        group.setCalendar(taskCalendar);
        taskCalendar.setGroup(group);

        subtask = new Subtask();
        subtask.setId(1L);
        subtask.setName("Subtask");
        subtask.setSessions(List.of(session1));
        subtask2 = new Subtask();
        subtask2.setId(2L);
        subtask2.setName("Subtask2");
        subtask2.setSessions(List.of(session2));
        subtask3 = new Subtask();
        subtask3.setId(3L);
        subtask3.setName("Subtask3");
        subtask3.setSessions(List.of(session3));

        TakenSubtask takenSubtask = new TakenSubtask();
        takenSubtask.setId(1L);
        takenSubtask.setUser(user);
        takenSubtask.setSubtask(subtask);

        TakenSubtask takenSubtask2 = new TakenSubtask();
        takenSubtask2.setId(2L);
        takenSubtask2.setUser(member);
        takenSubtask2.setSubtask(subtask2);

        takenSubtask3 = new TakenSubtask();
        takenSubtask3.setId(3L);
        takenSubtask3.setUser(member2);
        takenSubtask3.setSubtask(subtask3);

        List<TakenSubtask> takenList = new ArrayList<>(List.of(takenSubtask, takenSubtask2,takenSubtask3));

        session1.setId(1L);
        session1.setStartDate(LocalDateTime.of(2025, 3, 20, 10, 0));
        session1.setEndDate(LocalDateTime.of(2025, 3, 20, 11, 0));
        session1.setState(SessionState.PROGRAMMED);
        session2.setId(2L);
        session2.setStartDate(LocalDateTime.of(2025, 4, 20, 12, 0));
        session2.setEndDate(LocalDateTime.of(2025, 4, 20, 13, 0));
        session2.setState(SessionState.PROGRAMMED);
        session3.setId(3L);
        session3.setStartDate(LocalDateTime.of(2025, 5, 20, 14, 0));
        session3.setEndDate(LocalDateTime.of(2025, 5, 20, 15, 0));
        session3.setState(SessionState.PROGRAMMED);



        List<Session> sessions = new ArrayList<>(List.of(session1,session2,session3));
        group.setSessions(sessions);
        group.setSubtasks(new ArrayList<>(List.of(subtask,subtask2,subtask3)));
        group.setTakenSubtasks(takenList);
        group.setNumUsers(3);
        group.setActualMembers(3);
    }

    /* TEST SULLE STRATEGIE */
    @Test
    void setStrategiesTest_SKIPPEDSuccess(){
        group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null))));
        assertAll(
                ()-> assertEquals(group.getStrategies().size(),1),
                ()-> assertSame(group.getStrategies().getFirst().getStrategy(),DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS)
        );
    }

    @Test
    void setStrategiesTest_SKIPPEDFail(){
        assertThrows(IllegalArgumentException.class,() -> group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,1,1)))));

    }

    @Test
    void setStrategiesTest_EACHSuccess(){
        group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        assertAll(
                ()-> assertEquals(group.getStrategies().size(),1),
                ()-> assertSame(group.getStrategies().getFirst().getStrategy(),DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING)
        );
    }

    @Test
    void setStrategiesTest_FREEZETOTSuccess(){
        group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,1,null))));
        assertAll(
                ()-> assertEquals(group.getStrategies().size(),1),
                ()-> assertSame(group.getStrategies().getFirst().getStrategy(),DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS)
        );
    }
    @Test
    void setStrategiesTest_FREEZETOTFail(){
        assertThrows(IllegalArgumentException.class, () ->  group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,null,1)))));
    }

    @Test
    void setStrategiesTest_FREEZECONSECSuccess(){
        group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS,null,1))));
        assertAll(
                ()-> assertEquals(group.getStrategies().size(),1),
                ()-> assertSame(group.getStrategies().getFirst().getStrategy(),DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS)
        );
    }

    @Test
    void setStrategiesTest_FREEZECONSECFail(){
        assertThrows(IllegalArgumentException.class, ()-> group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS,1,null)))));
    }

    @Test
    void toCalendarTest_success(){
        group.setIsComplete(true);
        group.toCalendar();
        assertAll(
                ()-> assertSame(group.getState(),TaskState.INPROGRESS),
                ()-> assertSame(group.getIsInProgress(),true),
                ()-> assertThat(calendar.getSessions()).containsAll(subtask.getSessions()),
                ()-> assertThat(calendar2.getSessions()).containsAll(subtask2.getSessions()),
                ()-> assertThat(calendar3.getSessions()).containsAll(subtask3.getSessions()),
                ()-> assertThat(taskCalendar.getSessions()).containsAll(subtask.getSessions()),
                ()-> assertThat(taskCalendar.getSessions()).containsAll(subtask2.getSessions()),
                ()-> assertThat(taskCalendar.getSessions()).containsAll(subtask3.getSessions()),
                ()-> assertThat(group.getIsOnFeed()).isFalse()
        );
    }

    @Test
    void toCalendarTest_stateFail(){
        group.setState(TaskState.FINISHED);
        assertThrows(UnsupportedOperationException.class, () -> group.toCalendar());
    }

    @Test
    void toCalendarTest_inProgressFail(){
        group.setIsInProgress(true);
        assertThrows(UnsupportedOperationException.class, () -> group.toCalendar());
    }

    @Test
    void toCalendarTest_failIsComplete(){
        group.setIsComplete(false);
        assertThrows(UnsupportedOperationException.class, () -> group.toCalendar());
    }

    @Test
    void modifyTask_successFromTODO(){
        group.modifyTask();
        assertAll(
                ()-> assertFalse(group.getIsInProgress()),
                ()-> assertFalse(group.getIsOnFeed()),
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED)
        );
    }
    @Test
    void modifyTask_successFromIsInProgress(){
        calendar.addSessions(group.getSessions());
        group.setState(TaskState.INPROGRESS);
        group.setIsInProgress(true);
        group.modifyTask();
        assertAll(
                ()-> assertFalse(group.getIsInProgress()),
                ()-> assertSame(group.getState(),TaskState.FREEZED)
        );
    }

    @Test
    void modifyTask_failFINISHED(){
        group.setState(TaskState.FINISHED);
        assertThrows(UnsupportedOperationException.class, () -> group.modifyTask());
    }

    @Test
    void completeTaskBySessionsTest_success(){
        group.setState(TaskState.INPROGRESS);
        calendar.addSubtaskSessionsForGroups(subtask);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        calendar3.addSubtaskSessionsForGroups(subtask3);

        session1.setState(SessionState.COMPLETED);
        session2.setState(SessionState.COMPLETED);

        group.completeTaskBySessions();
        assertAll(
                ()-> assertSame(group.getState(),TaskState.FINISHED),
                ()-> assertFalse(group.getIsInProgress()),
                ()-> assertEquals(group.getPercentageOfCompletion(),100),
                ()-> assertFalse(group.getIsOnFeed()),
                ()-> assertThat(calendar.getSessions()).containsAll(subtask.getSessions()),
                ()-> assertThat(calendar2.getSessions()).containsAll(subtask2.getSessions()),
                ()-> assertThat(calendar3.getSessions()).containsAll(subtask3.getSessions())
        );
    }

    @Test
    void completeTaskBySessionsTest_failTODO(){
        assertThrows(IllegalStateException.class, () -> group.completeTaskBySessions());
    }

    @Test
    void completeTaskBySessionsTest_failFINISHED(){
        group.setState(TaskState.FINISHED);
        assertThrows(IllegalStateException.class, () -> group.completeTaskBySessions());
    }

    @Test
    void completeTaskBySessionsTest_failFREEZED(){
        group.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class, () -> group.completeTaskBySessions());
    }

    @Test
    void completeTaskBySessionsTest_failForSessionsPROGRAMMED(){
        group.setState(TaskState.INPROGRESS);
        calendar.addSessions(new ArrayList<>(List.of(session1)));
        calendar2.addSessions(new ArrayList<>(List.of(session2)));
        calendar2.addSessions(new ArrayList<>(List.of(session3)));
        assertThrows(IllegalStateException.class, () -> group.completeTaskBySessions());
    }


    @Test
    void forcedCompletion_success(){
        group.setState(TaskState.INPROGRESS);
        group.forcedCompletion();
        assertAll(
                ()-> assertSame(group.getState(),TaskState.FINISHED),
                ()-> assertEquals(group.getPercentageOfCompletion(),100)
        );
    }

    @Test
    void forcedCompletion_FailTODO(){
        assertThrows(IllegalStateException.class,() -> group.forcedCompletion());
    }

    @Test
    void forcedCompletion_FailFREEZED(){
        group.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class,() -> group.forcedCompletion());
    }

    @Test
    void forcedCompletion_FailFINISHED(){
        group.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class,() -> group.forcedCompletion());
    }

    @Test
    void completeSubtaskSessionTest_successLast() {
        group.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.COMPLETED);
        session2.setState(SessionState.COMPLETED);
        group.completeSubtaskSession(session3);
        assertAll(
                () -> assertSame(session1.getState(), SessionState.COMPLETED),
                () -> assertSame(session2.getState(), SessionState.COMPLETED),
                () -> assertSame(session3.getState(), SessionState.COMPLETED),
                () -> assertEquals(group.getConsecutiveSkippedSessions(), 0),
                () -> assertEquals(group.getPercentageOfCompletion(), 100),
                () -> assertSame(group.getState(), TaskState.FINISHED)
        );
    }


    @Test
    void completeSubtaskSessionTest_successNotLast(){
        group.setState(TaskState.INPROGRESS);
        group.completeSubtaskSession(session1);
        assertAll(
                ()-> assertSame(session1.getState(), SessionState.COMPLETED),
                ()-> assertSame(session2.getState(), SessionState.PROGRAMMED),
                ()-> assertSame(session3.getState(), SessionState.PROGRAMMED),
                ()-> assertEquals(group.getConsecutiveSkippedSessions(),0),
                ()-> assertEquals(group.getPercentageOfCompletion(),33),
                ()-> assertSame(group.getState(),TaskState.INPROGRESS)
        );
    }

    @Test
    void completeSubtaskSessionTest_failAlienSession(){
        group.setState(TaskState.INPROGRESS);
        Session alienSession = new Session();
        alienSession.setId(5L);
        alienSession.setStartDate(LocalDateTime.of(2026, 3, 20, 10, 0));
        alienSession.setEndDate(LocalDateTime.of(2026, 3, 20, 11, 0));
        alienSession.setState(SessionState.PROGRAMMED);
        assertThrows(EntityNotFoundException.class, () -> group.completeSubtaskSession(alienSession));
    }

    @Test
    void completeSubtaskSessionTest_failSKIPPED(){
        group.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.SKIPPED);
        assertThrows(IllegalStateException.class, () -> group.completeSubtaskSession(session1));

    }
    @Test
    void completeSubtaskSessionTest_failCOMPLETED(){
        group.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.COMPLETED);
        assertThrows(IllegalStateException.class, () -> group.completeSubtaskSession(session1));
    }



    @Test
    void SkipSessionTest_SuccessNoRescheduling() {
        group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null))));
        int initialSkipped = group.getSkippedSessions();
        int initialConsecutive = group.getConsecutiveSkippedSessions();
        group.skipSession(session1);
        assertAll(
                ()-> assertEquals(SessionState.SKIPPED, session1.getState()),
                ()-> assertEquals(initialSkipped + 1, group.getSkippedSessions()),
                ()-> assertEquals(initialConsecutive + 1, group.getConsecutiveSkippedSessions())
        );
    }

    @Test
    void skipSessionTest_SuccessWithAddAtEnd() {
        group.setDeadline(LocalDateTime.of(2027, 3, 25, 23, 59)); // per esempio
        group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.EACH_SESSION_LOST_WILL_BE_ADDED_AT_THE_END_OF_THE_SCHEDULING,null,null))));
        LocalDateTime oldEnd = session1.getEndDate();
        group.setTimetable(Timetable.ALL_DAY);
        subtask.setSessions(new ArrayList<>(List.of(session1)));
        group.skipSession(session1);

        assertAll(
                ()-> assertThat(calendar.getSessions().getFirst().getState()).isEqualTo(SessionState.PROGRAMMED),
                ()-> assertThat(calendar.getSessions().getFirst().getStartDate()).isAfter(oldEnd),
                ()-> assertThat(group.getCalendar().getSessions().getFirst().getState()).isEqualTo(SessionState.PROGRAMMED),
                ()-> assertThat(group.getCalendar().getSessions().getFirst().getStartDate()).isAfter(oldEnd)
        );
    }

    @Test
    void skipSessionTest_successFREEZE(){
        group.setDeadline(LocalDateTime.of(2027, 3, 25, 23, 59)); // per esempio
        group.setStrategies(new ArrayList<>(List.of(new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_SKIPPED_SESSIONS,0,null))));
        LocalDateTime oldEnd = session1.getEndDate();
        group.setTimetable(Timetable.ALL_DAY);
        subtask.setSessions(new ArrayList<>(List.of(session1)));
        subtask2.setSessions(new ArrayList<>(List.of(session2)));
        subtask3.setSessions(new ArrayList<>(List.of(session3)));
        group.skipSession(session1);
        assertAll(
                ()-> assertThat(calendar.getSessions()).isEmpty(),
                ()-> assertThat(calendar2.getSessions()).isEmpty(),
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getCalendar().getSessions()).isEmpty()
        );
    }


    @Test
    void skipSessionTest_sessionNotFound() {
        Session alienSession = new Session();
        alienSession.setId(999L);
        alienSession.setStartDate(LocalDateTime.now());
        alienSession.setEndDate(LocalDateTime.now().plusHours(1));
        alienSession.setState(SessionState.PROGRAMMED);

        assertAll(
                ()-> assertFalse(group.getSessions().contains(alienSession)),
                ()-> assertThrows(EntityNotFoundException.class, () -> group.skipSession(alienSession))
        );
    }

    @Test
    void joinGroupTest_success(){
        group.setIsOnFeed(true);
        group.setDateOnFeed(LocalDateTime.of(2025, 3, 5, 22, 0));
        group.getTakenSubtasks().remove(takenSubtask3);
        group.getMembers().remove(member2);
        group.setActualMembers(2);
        group.joinGroup(member2,subtask3);
        assertAll(
                ()-> assertThat(group.getMembers()).contains(member2),
                ()-> assertThat(group.getTakenSubtasks()).hasSize(3),
                ()-> assertThat(group.getIsComplete()).isTrue()
        );
    }

    @Test
    void joinGroupTest_failIsOnFeed(){
        group.setIsOnFeed(false);
        group.setDateOnFeed(LocalDateTime.of(2025, 3, 5, 22, 0));
        group.getTakenSubtasks().remove(takenSubtask3);
        group.getMembers().remove(member2);
        group.setActualMembers(2);
        assertThrows(IllegalArgumentException.class, () -> group.joinGroup(member2,subtask3));
    }

    @Test
    void joinGroupTest_failSubtaskAlreadyTaken(){
        group.setIsOnFeed(true);
        group.setDateOnFeed(LocalDateTime.of(2025, 3, 5, 22, 0));
        User alienUser = new User();
        group.getMembers().remove(member2);
        group.setActualMembers(2);
        assertThrows(IllegalArgumentException.class, () -> group.joinGroup(alienUser,subtask3));
    }


    @Test
    void leaveGroupTaskTest_success(){
        group.setState(TaskState.FREEZED);
        group.leaveGroupTask(member2);
        assertAll(
                ()-> assertThat(group.getMembers()).doesNotContain(member2),
                ()-> assertThat(group.getCalendar().getSessions()).isEmpty(),
                ()-> assertThat(group.getActualMembers()).isEqualTo(2),
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask.getSessions()),
                ()-> assertThat(member.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions())
        );
    }

    @Test
    void leaveGroupTaskTest_successINPROGRESS(){
        calendar.addSessions(subtask.getSessions());
        calendar2.addSessions(subtask2.getSessions());
        calendar3.addSessions(subtask3.getSessions());
        group.setState(TaskState.INPROGRESS);
        group.leaveGroupTask(member2);
        assertAll(
                ()-> assertThat(group.getMembers()).doesNotContain(member2),
                ()-> assertThat(group.getCalendar().getSessions()).isEmpty(),
                ()-> assertThat(group.getActualMembers()).isEqualTo(2),
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask.getSessions()),
                ()-> assertThat(member.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions())
        );
    }

    @Test
    void leaveGroupTaskTest_failMember(){
        User noMember = new User();
        Profile noMemberProfile = new Profile();
        noMemberProfile.setUsername("noMember");
        Map<Topic, Integer> noMemberTopics = new HashMap<>();
        noMemberTopics.put(Topic.PROGRAMMING, 5);
        noMemberTopics.put(Topic.ART, 3);
        noMemberProfile.setTopics(noMemberTopics);
        noMember.setPersonalProfile(noMemberProfile);
        assertThrows(IllegalArgumentException.class, () -> group.leaveGroupTask(noMember));
    }

    @Test
    void removeMemberTest_successSubstituteAndFoundINPROGRESS(){
        calendar.addSubtaskSessionsForGroups(subtask);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        calendar3.addSubtaskSessionsForGroups(subtask3);
        group.setState(TaskState.INPROGRESS);
        group.setIsOnFeed(true);
        group.removeMember(user,member,true);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask.getSessions()),
                ()-> assertThat(member2.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask3.getSessions()),
                ()-> assertThat(group.getIsOnFeed()).isTrue()
        );
    }

   @Test
    void removeMemberTest_successSubstituteAndFoundFREEZED(){
        group.setState(TaskState.FREEZED);
        group.setIsOnFeed(true);
        group.removeMember(user,member,true);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(taskCalendar.getSessions()).isEmpty(),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask.getSessions()),
                ()-> assertThat(member2.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask3.getSessions())
        );
    }

    @Test
    void removeMemberTest_successSubstituteAndNotFoundINPROGRESS(){
        calendar.addSubtaskSessionsForGroups(subtask);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        calendar3.addSubtaskSessionsForGroups(subtask3);
        group.setState(TaskState.INPROGRESS);
        group.removeMember(user,member,true);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask.getSessions()),
                ()-> assertThat(member2.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask3.getSessions())
        );
    }

    @Test
    void removeMemberTest_successNotSubstituteINPROGRESS(){
        calendar.addSubtaskSessionsForGroups(subtask);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        calendar3.addSubtaskSessionsForGroups(subtask3);
        group.setState(TaskState.INPROGRESS);

        group.removeMember(user,member,false);
        assertAll(
                ()-> assertThat(group.getState()).isNotEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isTrue(),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).containsAll(subtask.getSessions()),
                ()-> assertThat(member2.getCalendar().getSessions()).containsAll(subtask3.getSessions())
        );
    }

    @Test
    void removeMemberTest_successNotSubstituteFREEZED(){
        group.setState(TaskState.FREEZED);

        group.removeMember(user,member,false);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isTrue(),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask3.getSessions()),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member)
        );
    }

    @Test
    void removeMemberTest_FailFINISHED(){
        group.setState(TaskState.FINISHED);
        assertThrows(IllegalStateException.class, ()->group.removeMember(user,member,true));
    }

    @Test
    void removeMemberTest_FailTODO(){
        group.setState(TaskState.TODO);
        assertThrows(IllegalStateException.class, ()->group.removeMember(user,member,false));
    }

    @Test
    void removeMemberTest_FailUserEqualsMember(){
        group.setState(TaskState.INPROGRESS);
        assertThrows(IllegalArgumentException.class, ()->group.removeMember(user,user,true));
    }

    @Test
    void removeMemberTest_FailNoMember(){
        group.setState(TaskState.INPROGRESS);
        User alienMember = new User();
        alienMember.setAge(20);
        alienMember.setSex(Sex.MALE);
        Profile alienProfile = new Profile();
        alienProfile.setUsername("alien");
        alienMember.setPersonalProfile(alienProfile);
        assertThrows(IllegalArgumentException.class, ()->group.removeMember(user,alienMember,false));
    }

    @Test
    void autoSkipIfNotCompletedTest_SessionNotFound() {
        Session unknownSession = new Session();
        unknownSession.setId(999L);
        assertThrows(IllegalStateException.class,
                () -> group.autoSkipIfNotCompleted(unknownSession));
    }

    @Test
    void autoSkipIfNotCompletedTest_nowBeforeNextSession() {
        session2.setStartDate(LocalDateTime.now().plusHours(2));
        group.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }

    @Test
    void autoSkipIfNotCompletedTest_nowAfterNextSessionStarted() {
        session1.setStartDate(LocalDateTime.now().minusHours(2));
        session1.setEndDate(LocalDateTime.now().minusHours(1));
        session2.setStartDate(LocalDateTime.now().minusMinutes(30));
        session2.setEndDate(LocalDateTime.now().plusHours(1));
        group.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.SKIPPED, session1.getState());
    }

    @Test
    void autoSkipIfNotCompletedTest_nowBeforeOneDayAfterThisSession() {
        session1.setStartDate(LocalDateTime.now().minusHours(13));
        session1.setEndDate(LocalDateTime.now().minusHours(12));
        group.setSessions(new ArrayList<>(List.of(session1)));
        group.autoSkipIfNotCompleted(session1);
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }


    @Test
    void testRemoveAndFreezeTask_Success() {
        calendar.setSessions(new ArrayList<>(List.of(session1)));
        calendar2.setSessions(new ArrayList<>(List.of(session2)));
        calendar3.setSessions(new ArrayList<>(List.of(session3)));
        group.setState(TaskState.INPROGRESS);
        group.setIsInProgress(true);
        group.removeAndFreezeTask(user);

        assertAll(
                ()-> assertFalse(user.getCalendar().getSessions().contains(session1)),
                ()-> assertFalse(user.getCalendar().getSessions().contains(session2)),
                ()-> assertEquals(TaskState.FREEZED, group.getState()),
                ()-> assertFalse(group.getIsInProgress())
        );
    }

    @Test
    void testRemoveAndFreezeTask_NoSessionsInCalendar() {
        calendar.setSessions(new ArrayList<>());
        group.removeAndFreezeTask(user);
        assertAll(
                ()-> assertEquals(TaskState.FREEZED, group.getState()),
                ()-> assertFalse(group.getIsInProgress())
        );
    }


}






















