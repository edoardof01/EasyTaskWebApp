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
import service.GroupService;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private GroupDAO groupDAO;
    @Mock
    private GroupMapper groupMapper;
    @InjectMocks
    private GroupService groupService;
    @Mock
    private SessionDAO sessionDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private SubtaskDAO subtaskDAO;

    private Group group;
    private GroupDTO groupDTO;
    private User user;
    private User member;
    private Subtask subtask1;
    private Subtask subtask2;
    private Session session1;
    private Session session2;
    private Session session3;
    private Calendar calendar;
    private Calendar calendar2;
    private TakenSubtask takenSubtask2;
    private TaskCalendar taskCalendar;

    @BeforeEach
    void init(){
        user = new User();
        user.setId(1L);
        Profile profile = new Profile();
        profile.setUsername("admin");
        user.setPersonalProfile(profile);

        calendar = new Calendar();
        calendar.setId(1L);
        calendar.setUser(user);
        user.setCalendar(calendar);

        calendar2 = new Calendar();
        member = new User();
        member.setId(2L);
        Profile memberProfile = new Profile();
        profile.setUsername("member");
        member.setPersonalProfile(memberProfile);
        calendar2.setId(2L);
        member.setCalendar(calendar2);

        calendar.setUser(user);
        calendar2.setUser(member);
        // CONTINUA ...


        group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setTopic(Topic.ART);
        group.setTimetable(Timetable.ALL_DAY);
        group.setDescription("we are Testing Group");
        StrategyInstance strategy = new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null);
        List<StrategyInstance> strategies = new ArrayList<>(List.of(strategy));
        strategy.setId(1L);
        group.setStrategies(strategies);
        group.setTotalTime(3);
        group.setNumUsers(2);
        group.setUser(user);
        group.setPriority(1);
        group.setResources(new ArrayList<>());
        group.addMember(member);
        group.addMember(user);
        group.setIsComplete(true);

        taskCalendar = new TaskCalendar();
        taskCalendar.setGroup(group);
        group.setCalendar(taskCalendar);



        session1 = new Session();
        session1.setId(1);
        session1.setStartDate(LocalDateTime.of(2025, 3, 20, 10, 0));
        session1.setEndDate(LocalDateTime.of(2025, 3, 20, 11, 0));
        session1.setState(SessionState.PROGRAMMED);

        session2 = new Session();
        session2.setId(2);
        session2.setStartDate(LocalDateTime.of(2025, 3, 20, 13, 0));
        session2.setEndDate(LocalDateTime.of(2025, 3, 20, 14, 0));
        session2.setState(SessionState.PROGRAMMED);

        session3 = new Session();
        session3.setId(3);
        session3.setStartDate(LocalDateTime.of(2025, 3, 20, 15, 0));
        session3.setEndDate(LocalDateTime.of(2025, 3, 20, 16, 0));
        session3.setState(SessionState.PROGRAMMED);

        List<Session> sessions = new ArrayList<>(List.of(session1,session2,session3));

        subtask1 = new Subtask("subtask1", 1, 2, "this is a subtask",
                new ArrayList<>(), new ArrayList<>(List.of(session1)));
        subtask1.setId(1L);

        subtask2 = new Subtask("subtask2", 1, 2, "this is also a subtask",
                new ArrayList<>(), new ArrayList<>(List.of(session2,session3)));
        subtask2.setId(2L);

        TakenSubtask takenSubtask = new TakenSubtask();
        takenSubtask.setId(1L);
        takenSubtask.setSubtask(subtask1);
        takenSubtask.setUser(user);

        takenSubtask2 = new TakenSubtask();
        takenSubtask2.setId(2L);
        takenSubtask2.setSubtask(subtask2);
        takenSubtask2.setUser(member);

        group.setTakenSubtasks(new ArrayList<>(List.of(takenSubtask,takenSubtask2)));

        group.setSessions(sessions);
        group.setSubtasks(new ArrayList<>(List.of(subtask1,subtask2)));

        groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        groupDTO.setName("Test Group");
    }


    @Test
    void getPersonalByIdTest_Success() {
        when(groupDAO.findById(1L)).thenReturn(group);
        when(groupMapper.toGroupDTO(group)).thenReturn(groupDTO);
        GroupDTO result = groupService.getGroupById(1L);

        assertAll(
                ()-> assertNotNull(result),
                ()-> assertEquals(1L, result.getId()),
                ()-> assertEquals("Test Group", result.getName())
        );
        verify(groupDAO, times(1)).findById(1L);
        verify(groupMapper, times(1)).toGroupDTO(group);
    }

    @Test
    void getPersonalByIdTest_failNotFound() {
        when(groupDAO.findById(99L)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> groupService.getGroupById(99L)); //
        verify(groupDAO, times(1)).findById(99L);
        verify(groupMapper, never()).toGroupDTO(any());
    }


    /// CREATE GROUP TESTS

    @Test
    void createGroupTest_failNullMandatoryFieldName(){
        group.setName(null);
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));


    }

    @Test
    void createGroupTest_failNullMandatoryFieldTopic(){
        group.setTopic(null);
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));

    }

    @Test
    void createGroupTest_failNullMandatoryFieldTimeTable(){
        group.setTimetable(null);
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failNullMandatoryFieldStrategies(){
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),null ,group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failEmptyFieldStrategies(){
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),null ,group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }


    @Test
    void createGroupTest_failNullMandatoryFieldSessions(){
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,null,group.getNumUsers()));
    }

    @Test
    void createGroupTest_failEmptyFieldSessions(){
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,new ArrayList<>(),group.getNumUsers()));
    }


    @Test
    void createGroupTest_failFieldPriorityLessThanZero(){
        group.setPriority(-1);
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }
    @Test
    void createGroupTest_failFieldPriorityMoreThanFive(){
        group.setPriority(6);
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failFieldRequiredUsersDifferentFromNUmberOfSubtasks(){
        group.setSubtasks(new ArrayList<>(List.of(subtask1)));
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),null, null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failSKIPPEDStrategyAndDeadline(){
        group.setDeadline(LocalDateTime.of(2027, 7, 10, 10, 0));
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failSKIPPEDStrategyAndMore(){
        StrategyInstance strategyInstance = new StrategyInstance(DefaultStrategy.FREEZE_TASK_AFTER_TOT_CONSECUTIVE_SKIPPED_SESSIONS,null,1);
        StrategyInstance strategyInstance2 = new StrategyInstance(DefaultStrategy.SKIPPED_SESSIONS_NOT_POSTPONED_THE_TASK_CANNOT_BE_FREEZED_FOR_SKIPPED_SESSIONS,null,null);
        group.setStrategies(new ArrayList<>(List.of(strategyInstance,strategyInstance2)));
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failSubtaskSession(){
        calendar.addSessions(new ArrayList<>(List.of(new Session(LocalDateTime.of(2025, 3, 20, 10, 30),LocalDateTime.of(2025, 3, 20, 11, 30)))));
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failSubtasksSessionsDifferFromTaskSessions(){
        subtask1.getSessions().add(new Session(LocalDateTime.of(2025, 3, 20, 18, 30),LocalDateTime.of(2025, 3, 20, 18, 30)));
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }
    @Test
    void createGroupTest_failSubtasksSessionAlreadyAssigned(){
        subtask1.getSessions().add(session2);
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failSubtasksResourcesDontMatchTaskResources(){
        Resource resource1 = new Resource("computer",ResourceType.EQUIPMENT,3,null);
        Resource resource2 = new Resource("calculator",ResourceType.EQUIPMENT,2,null);
        List<Resource> resources = new ArrayList<>(List.of(resource1,resource2));
        group.setResources(resources);
        subtask1.setResources(new ArrayList<>(List.of(resource1)));
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));
    }

    @Test
    void createGroupTest_failSessionOverflowTimeSLots(){
        group.setTimetable(Timetable.MORNING);
        session1.setStartDate(LocalDateTime.of(2025, 3, 20, 18, 30));
        session1.setEndDate(LocalDateTime.of(2025, 3, 20, 19, 30));
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));

    }

    @Test
    void createGroupTest_failSessionDifferFromTotalTime(){
        group.setTotalTime(1);
        assertThrows(IllegalArgumentException.class, ()-> groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers()));

    }

    @Test
    void createGroupTest_success(){
        when(userDAO.findById(1L)).thenReturn(user);
        when(groupMapper.toGroupDTO(any(Group.class))).thenReturn(groupDTO);
        GroupDTO result = groupService.createGroup(group.getName(),user.getId(),group.getTopic(),group.getDeadline(), null, group.getTotalTime(),
                group.getTimetable(),group.getStrategies(),group.getPriority(),group.getDescription(),group.getResources(),group.getSubtasks(),subtask1,group.getSessions(),group.getNumUsers());
        assertAll(
                ()-> assertThat(result).isNotNull(),
                ()-> {
                    assert result != null;
                    assertEquals("Test Group",result.getName());
                }
        );
    }

    @Test
    void deleteGroupTest_success(){
        when(groupDAO.findById(1L)).thenReturn(group);
        groupService.deleteGroup(1L);
        assertAll(
                ()->assertThat(group.getIsInProgress()).isFalse(),
                ()->assertThat(group.getIsOnFeed()).isFalse()
        );
    }
    @Test
    void deleteGroupTest_successINPROGRESS(){
        group.setState(TaskState.INPROGRESS);
        group.setIsInProgress(true);
        calendar.addSessions(new ArrayList<>(List.of(session1)));
        calendar2.addSessions(new ArrayList<>(List.of(session2,session3)));
        when(groupDAO.findById(1L)).thenReturn(group);
        groupService.deleteGroup(1L);
        assertAll(
                ()->assertThat(group.getIsInProgress()).isFalse(),
                ()->assertThat(group.getIsOnFeed()).isFalse(),
                ()->assertThat(calendar.getSessions()).doesNotContain(session1),
                ()->assertThat(calendar2.getSessions()).doesNotContain(session2,session3)
        );
    }

    @Test
    void deleteGroupTest_fail(){
        when(groupDAO.findById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, ()-> groupService.deleteGroup(1L));
    }

    @Test
    void moveToCalendarTest_success(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        groupService.moveToCalendar(group.getId(),user.getId());
        assertAll(
                ()-> assertThat(group.getIsOnFeed()).isFalse(),
                ()-> assertThat(calendar.getSessions()).contains(session1),
                ()-> assertThat(calendar2.getSessions()).contains(session2,session3)
        );
    }

    @Test
    void moveToCalendarTest_failNoSuchGroup(){
        when(groupDAO.findById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, ()-> groupService.moveToCalendar(group.getId(),user.getId()));
    }

    @Test
    void moveToCalendarTest_failNoSuchUser(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, ()-> groupService.moveToCalendar(group.getId(),user.getId()));
    }

    @Test
    void moveToCalendarTest_failIsNotComplete(){
        group.setIsComplete(false);
        assertThrows(IllegalArgumentException.class, ()-> groupService.moveToCalendar(group.getId(),user.getId()));
    }

    @Test
    void completeGroupBySessions_success(){
        group.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.COMPLETED);
        session2.setState(SessionState.COMPLETED);
        when(groupDAO.findById(1L)).thenReturn(group);
        calendar.addSubtaskSessionsForGroups(subtask1);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        groupService.completeGroupBySessions(group.getId());
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FINISHED),
                ()-> assertThat(group.getIsInProgress()).isFalse(),
                ()-> assertThat(group.getPercentageOfCompletion()).isEqualTo(100),
                ()-> assertThat(session3.getState()).isEqualTo(SessionState.COMPLETED),
                ()-> assertThat(calendar.getSessions().getFirst().getState()).isEqualTo(SessionState.COMPLETED),
                ()-> assertThat(calendar2.getSessions().getFirst().getState()).isEqualTo(SessionState.COMPLETED)
        );
    }

    @Test
    void completeTaskBySessionsTest_failTODO(){
        when(groupDAO.findById(1L)).thenReturn(group);
        group.setState(TaskState.TODO);
        assertThrows(IllegalStateException.class, () -> groupService.completeGroupBySessions(group.getId()));
    }

    @Test
    void completeTaskBySessionsTest_failFINISHED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        group.setState(TaskState.FINISHED);
        assertThrows(IllegalStateException.class, () -> groupService.completeGroupBySessions(group.getId()));
    }

    @Test
    void completeTaskBySessionsTest_failFREEZED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        group.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class, () -> groupService.completeGroupBySessions(group.getId()));
    }

    @Test
    void completeTaskBySessionsTest_failForSessionsPROGRAMMED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        group.setState(TaskState.INPROGRESS);
        calendar.addSessions(new ArrayList<>(List.of(session1)));
        calendar2.addSessions(new ArrayList<>(List.of(session2,session3)));
        assertThrows(IllegalStateException.class, () -> groupService.completeGroupBySessions(group.getId()));
    }

    @Test
    void forcedCompletion_success(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        group.setState(TaskState.INPROGRESS);
        groupService.forceCompletion(group.getId(),user.getId());
        assertAll(
                ()-> assertSame(group.getState(),TaskState.FINISHED),
                ()-> assertEquals(100,group.getPercentageOfCompletion())
        );
    }

    @Test
    void forcedCompletion_FailTODO(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        assertThrows(IllegalStateException.class,() -> groupService.forceCompletion(group.getId(),user.getId()));
    }

    @Test
    void forcedCompletion_FailFREEZED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        group.setState(TaskState.FREEZED);
        assertThrows(IllegalStateException.class,() -> groupService.forceCompletion(group.getId(),user.getId()));
    }

    @Test
    void forcedCompletion_FailFINISHED(){
        group.setState(TaskState.FREEZED);
        assertThrows(IllegalArgumentException.class,() -> groupService.forceCompletion(group.getId(),user.getId()));
    }

    @Test
    void completeSubtaskSessionTest_successNotLast(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(subtaskDAO.findById(1L)).thenReturn(subtask1);
        when(sessionDAO.findById(1L)).thenReturn(session1);
        calendar.addSubtaskSessionsForGroups(subtask1);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        group.setState(TaskState.INPROGRESS);

        groupService.completeSubtaskSession(user.getId(),group.getId(),subtask1.getId(),session1.getId());

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
    void completeSubtaskSessionTest_successLast() {
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(2L)).thenReturn(member);
        when(subtaskDAO.findById(2L)).thenReturn(subtask2);
        when(sessionDAO.findById(3L)).thenReturn(session3);
        group.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.COMPLETED);
        session2.setState(SessionState.COMPLETED);
        groupService.completeSubtaskSession(member.getId(),group.getId(),subtask2.getId(),session3.getId());
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
    void completeSubtaskSessionTest_failAlienSession(){
        group.setState(TaskState.INPROGRESS);
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(subtaskDAO.findById(1L)).thenReturn(subtask1);
        Session alienSession = new Session();
        alienSession.setId(5L);
        alienSession.setStartDate(LocalDateTime.of(2026, 3, 20, 10, 0));
        alienSession.setEndDate(LocalDateTime.of(2026, 3, 20, 11, 0));
        alienSession.setState(SessionState.PROGRAMMED);
        when(sessionDAO.findById(5L)).thenReturn(alienSession);
        assertThrows(IllegalArgumentException.class, () -> groupService.completeSubtaskSession(user.getId(),group.getId(),subtask1.getId(),alienSession.getId()));
    }

    @Test
    void completeSubtaskSessionTest_failSKIPPED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(subtaskDAO.findById(1L)).thenReturn(subtask1);
        when(sessionDAO.findById(1L)).thenReturn(session1);
        calendar.addSubtaskSessionsForGroups(subtask1);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        group.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.SKIPPED);
        assertThrows(IllegalStateException.class, () -> groupService.completeSubtaskSession(user.getId(),group.getId(),subtask1.getId(),session1.getId()));

    }
    @Test
    void completeSubtaskSessionTest_failCOMPLETED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(subtaskDAO.findById(1L)).thenReturn(subtask1);
        when(sessionDAO.findById(1L)).thenReturn(session1);
        calendar.addSubtaskSessionsForGroups(subtask1);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        group.setState(TaskState.INPROGRESS);
        session1.setState(SessionState.COMPLETED);
        assertThrows(IllegalStateException.class, () -> groupService.completeSubtaskSession(user.getId(),group.getId(),subtask1.getId(),session1.getId()));
    }

    @Test
    void joinGroupTest_success(){
        when(groupDAO.findById(1L)).thenReturn(group);
        group.getTakenSubtasks().remove(takenSubtask2);
        group.getMembers().remove(member);
        group.setIsOnFeed(true);
        when(userDAO.findById(2L)).thenReturn(member);
        when(subtaskDAO.findById(2L)).thenReturn(subtask2);
        group.setIsComplete(false);
        groupService.joinGroup(member.getId(),group.getId(),subtask2.getId());
        assertAll(
                ()-> assertThat(group.getMembers()).contains(member),
                ()-> assertThat(group.getTakenSubtasks()).hasSize(2),
                ()-> assertThat(group.getIsComplete()).isTrue()
        );
    }

    @Test
    void joinGroupTest_failIsNotOnFeed(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(2L)).thenReturn(member);
        group.setIsOnFeed(false);
        group.setDateOnFeed(LocalDateTime.of(2025, 3, 5, 22, 0));
        group.getTakenSubtasks().remove(takenSubtask2);
        group.getMembers().remove(member);
        group.setActualMembers(2);
        assertThrows(IllegalArgumentException.class, ()-> groupService.joinGroup(member.getId(),group.getId(),subtask2.getId()));
    }

    @Test
    void joinGroupTest_failSubtaskTaken(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(subtaskDAO.findById(2L)).thenReturn(subtask2);
        group.setIsOnFeed(true);
        group.setDateOnFeed(LocalDateTime.of(2025, 3, 5, 22, 0));
        User alienUser = new User();
        alienUser.setId(3L);
        group.getMembers().remove(member);
        group.setActualMembers(1);
        when(userDAO.findById(3L)).thenReturn(member);
        assertThrows(IllegalArgumentException.class, () -> groupService.joinGroup(alienUser.getId(), group.getId(),subtask2.getId()));
    }


    @Test
    void leaveGroupTaskTest_success(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(2L)).thenReturn(member);
        group.setActualMembers(2);
        groupService.leaveGroup(group.getId(),member.getId());
        assertAll(
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(group.getCalendar().getSessions()).isEmpty(),
                ()-> assertThat(group.getActualMembers()).isEqualTo(1),
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask1.getSessions()),
                ()-> assertThat(member.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions())
        );
    }

    @Test
    void leaveGroupTaskTest_successINPROGRESS(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(2L)).thenReturn(member);
        group.setActualMembers(2);
        calendar.addSessions(subtask1.getSessions());
        calendar2.addSessions(subtask2.getSessions());
        group.setState(TaskState.INPROGRESS);
        groupService.leaveGroup(group.getId(), member.getId());
        assertAll(
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(group.getCalendar().getSessions()).isEmpty(),
                ()-> assertThat(group.getActualMembers()).isEqualTo(1),
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask1.getSessions()),
                ()-> assertThat(member.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions())
        );
    }

    @Test
    void leaveGroupTaskTest_failMember(){
        when(groupDAO.findById(1L)).thenReturn(group);
        User noMember = new User();
        noMember.setId(3L);
        Profile noMemberProfile = new Profile();
        noMemberProfile.setUsername("noMember");
        Map<Topic, Integer> noMemberTopics = new HashMap<>();
        noMemberTopics.put(Topic.PROGRAMMING, 5);
        noMemberTopics.put(Topic.ART, 3);
        noMemberProfile.setTopics(noMemberTopics);
        noMember.setPersonalProfile(noMemberProfile);
        when(userDAO.findById(3L)).thenReturn(noMember);
        assertThrows(IllegalArgumentException.class, () -> groupService.leaveGroup(group.getId(), noMember.getId()));
    }

    @Test
    void removeMemberTest_successSubstituteAndFoundINPROGRESS(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(userDAO.findById(2L)).thenReturn(member);
        calendar.addSubtaskSessionsForGroups(subtask1);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        group.setState(TaskState.INPROGRESS);
        group.setIsOnFeed(true);
        groupService.removeMemberFromGroup(group.getId(), group.getUser().getId(),member.getId(),true);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask1.getSessions()),
                ()-> assertThat(member.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(group.getIsOnFeed()).isTrue()
        );
    }

    @Test
    void removeMemberTest_successSubstituteAndFoundFREEZED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(userDAO.findById(2L)).thenReturn(member);
        group.setState(TaskState.FREEZED);
        group.setIsOnFeed(true);
        groupService.removeMemberFromGroup(group.getId(), group.getUser().getId(),member.getId(),true);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(taskCalendar.getSessions()).isEmpty(),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask1.getSessions()),
                ()-> assertThat(member.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions())
        );
    }

    @Test
    void removeMemberTest_successSubstituteAndNotFoundINPROGRESS(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(userDAO.findById(2L)).thenReturn(member);
        calendar.addSubtaskSessionsForGroups(subtask1);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        group.setState(TaskState.INPROGRESS);
        groupService.removeMemberFromGroup(group.getId(), group.getUser().getId(),member.getId(),true);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isFalse(),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask1.getSessions()),
                ()-> assertThat(member.getCalendar().getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions())
        );
    }

    @Test
    void removeMemberTest_successNotSubstituteINPROGRESS(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(userDAO.findById(2L)).thenReturn(member);
        calendar.addSubtaskSessionsForGroups(subtask1);
        calendar2.addSubtaskSessionsForGroups(subtask2);
        group.setActualMembers(2);
        group.setState(TaskState.INPROGRESS);
        groupService.removeMemberFromGroup(group.getId(), group.getUser().getId(),member.getId(),false);
        assertAll(
                ()-> assertThat(group.getState()).isNotEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isTrue(),
                ()-> assertThat(group.getActualMembers()).isEqualTo(1),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member),
                ()-> assertThat(user.getCalendar().getSessions()).containsAll(subtask1.getSessions())
        );
    }

    @Test
    void removeMemberTest_successNotSubstituteFREEZED(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(userDAO.findById(2L)).thenReturn(member);
        group.setState(TaskState.FREEZED);
        groupService.removeMemberFromGroup(group.getId(), group.getUser().getId(),member.getId(),false);
        assertAll(
                ()-> assertThat(group.getState()).isEqualTo(TaskState.FREEZED),
                ()-> assertThat(group.getIsComplete()).isTrue(),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask2.getSessions()),
                ()-> assertThat(taskCalendar.getSessions()).doesNotContainAnyElementsOf(subtask1.getSessions()),
                ()-> assertThat(group.getMembers()).doesNotContain(member)
        );
    }

    @Test
    void removeMemberTest_FailFINISHED(){
    when(groupDAO.findById(1L)).thenReturn(group);
    when(userDAO.findById(1L)).thenReturn(user);
    when(userDAO.findById(2L)).thenReturn(member);
    group.setState(TaskState.FINISHED);
    assertThrows(IllegalStateException.class, ()-> groupService.removeMemberFromGroup(group.getId(), group.getUser().getId(),member.getId(),true));
    }

    @Test
    void removeMemberTest_FailTODO(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        when(userDAO.findById(2L)).thenReturn(member);
        group.setState(TaskState.TODO);
        assertThrows(IllegalStateException.class, ()-> groupService.removeMemberFromGroup(group.getId(),group.getUser().getId(), member.getId(), false));
    }

    @Test
    void removeMemberTest_FailUserEqualsMember(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        group.setState(TaskState.INPROGRESS);
        assertThrows(IllegalArgumentException.class, ()-> groupService.removeMemberFromGroup(group.getId(),group.getUser().getId(),group.getUser().getId(), false));
    }

    @Test
    void removeMemberTest_FailNoMember(){
        when(groupDAO.findById(1L)).thenReturn(group);
        when(userDAO.findById(1L)).thenReturn(user);
        group.setState(TaskState.INPROGRESS);
        User alienMember = new User();
        alienMember.setAge(20);
        alienMember.setSex(Sex.MALE);
        Profile alienProfile = new Profile();
        alienProfile.setUsername("alien");
        alienMember.setPersonalProfile(alienProfile);
        assertThrows(IllegalArgumentException.class, ()-> groupService.removeMemberFromGroup(group.getId(),group.getUser().getId(),group.getUser().getId(), false));
    }

    @Test
    void handleLimitExceededTest_failSessionNotFound() {
        when(groupDAO.findById(1L)).thenReturn(group);
        Session unknownSession = new Session();
        unknownSession.setId(999L);
        when(sessionDAO.findById(999L)).thenReturn(unknownSession);
        assertThrows(IllegalStateException.class, () -> groupService.handleLimitExceeded(unknownSession.getId(), group.getId()));
    }

    @Test
    void handleLimitExceededTest_nowBeforeNextSession() {
        when(groupDAO.findById(1L)).thenReturn(group);
        when(sessionDAO.findById(2L)).thenReturn(session2);
        session2.setStartDate(LocalDateTime.now().plusHours(1));
        groupService.handleLimitExceeded(session2.getId(), group.getId());
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }

    @Test
    void handleLimitExceededTest_nowAfterNextSessionStarted() {
        when(groupDAO.findById(1L)).thenReturn(group);
        when(sessionDAO.findById(1L)).thenReturn(session1);
        session1.setStartDate(LocalDateTime.now().minusHours(2));
        session1.setEndDate(LocalDateTime.now().minusHours(1));
        session2.setStartDate(LocalDateTime.now().minusMinutes(30));
        session2.setEndDate(LocalDateTime.now().plusHours(1));
        groupService.handleLimitExceeded(session1.getId(), group.getId());
        assertEquals(SessionState.SKIPPED, session1.getState());
    }

    @Test
    void handleLimitExceededTest_nowBeforeOneDayAfterThisSession() {
        when(groupDAO.findById(1L)).thenReturn(group);
        when(sessionDAO.findById(1L)).thenReturn(session1);
        session1.setStartDate(LocalDateTime.now().minusHours(13));
        session1.setEndDate(LocalDateTime.now().minusHours(12));
        group.setSessions(new ArrayList<>(List.of(session1)));
        groupService.handleLimitExceeded(session1.getId(), group.getId());
        assertEquals(SessionState.PROGRAMMED, session1.getState());
    }

}
