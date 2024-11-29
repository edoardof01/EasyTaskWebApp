package Endpoints;
import domain.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import orm.*;
import service.GroupService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/group")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class GroupEndpoint {

    @Inject
    private GroupService groupService;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private SubtaskMapper subtaskMapper;

    @Inject
    private UserMapper userMapper;

    @Inject
    private SessionMapper sessionMapper;


    @GET
    public Response getAllGroupTasks() {
        List<GroupDTO> sharedList = groupService.getAllGroups();
        return Response.ok(sharedList).build();
    }


    @GET
    @Path("/{id}")
    public Response getGroupById(@PathParam("id") long id) {
        GroupDTO groupDTO = groupService.getGroupById(id);

        if (groupDTO == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Task with ID " + id + " not found.")
                    .build();
        }

        return Response.ok(groupDTO).build();
    }


    @POST
    @Transactional
    public Response createGroup(GroupDTO groupDTO) {
        try {
            // Estrai i campi da groupDTO
            String name = groupDTO.getName();
            int numUsers = groupDTO.getNumUser();
            LocalDateTime dateOnFeed = groupDTO.getDateOnFeed();
            Topic topic = groupDTO.getTopic();
            LocalDateTime deadline = groupDTO.getDeadline();
            int totalTime = groupDTO.getTotalTime();
            Set<Timetable> timeSlots = groupDTO.getTimetable();
            List<StrategyInstance> strategies = groupDTO.getStrategies();
            int priority = groupDTO.getPriority();
            String description = groupDTO.getDescription();

            // Mappa i DTO a entità
            List<Resource> resources = groupDTO.getResources().stream()
                    .map(resourceMapper::toResourceEntity)
                    .collect(Collectors.toList());
            List<Subtask> subtasks = groupDTO.getSubtasks().stream()
                    .map(subtaskMapper::toSubtaskEntity)
                    .collect(Collectors.toList());
            List<Session> sessions = groupDTO.getSessions().stream()
                    .map(sessionMapper::toSessionEntity)
                    .collect(Collectors.toList());

            User user = userMapper.toUserEntity(groupDTO.getUser());

            // Passa i campi estratti e le entità al servizio
            GroupDTO createdGroup = groupService.createGroup(
                    name, user, topic, dateOnFeed, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, sessions, numUsers, null);

            return Response.status(Response.Status.CREATED)
                    .entity(createdGroup)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteGroup(@PathParam("id") long id) {
        try {
            groupService.deleteGroup(id);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateGroup(@PathParam("id") long id, GroupDTO groupDTO) {
        try {
            GroupDTO updatedGroup = groupService.modifyGroup(
                    id,
                    groupDTO.getName(),
                    groupDTO.getTopic(),
                    groupDTO.getDeadline(),
                    groupDTO.getTotalTime(),
                    groupDTO.getTimetable(),
                    groupDTO.getStrategies(), // Seleziona una strategia
                    groupDTO.getPriority(),
                    groupDTO.getDescription(),
                    groupDTO.getResources().stream().map(resourceMapper::toResourceEntity).collect(Collectors.toList()),
                    groupDTO.getSubtasks().stream().map(subtaskMapper::toSubtaskEntity).collect(Collectors.toList()),
                    groupDTO.getNumUser()
            );
            return Response.ok(updatedGroup).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }



    @POST
    @Path("/moveToCalendar/")
    @Transactional
    public Response moveToCalendar(GroupDTO groupDTO, @QueryParam("groupId") long groupId) {
        try {
            groupService.moveToCalendar(groupDTO, groupId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/completeSession/{groupId}")
    @Transactional
    public Response completeSession(@PathParam("groupId") long groupId, @QueryParam("sessionId") long sessionId) {
        try {
            groupService.completeSession(groupId, sessionId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/completeBySessions/{groupId}")
    @Transactional
    public Response completeSharedBySessions(@PathParam("groupId") long groupId) {
        try {
            groupService.completeSharedBySessions(groupId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/forceCompletion/{groupId}")
    @Transactional
    public Response forceCompletion(@PathParam("groupId") long groupId, @QueryParam("userId") long userId) {
        try {
            groupService.forceCompletion(groupId,userId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/handleLimitExceeded/{groupId}")
    @Transactional
    public Response handleLimitExceeded(@PathParam("groupId") long groupId, SessionDTO sessionDTO) {
        try {
            groupService.handleLimitExceeded(sessionDTO, groupId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
    @POST
    @Path("/{groupId}/users/{userId}/subtasks/{subtaskId}")
    public Response joinGroup(
            @PathParam("groupId") long groupId,
            @PathParam("userId") long userId,
            @PathParam("subtaskId") long subtaskId) {
        try {
            // Utilizza il nuovo metodo nel GroupService
            GroupDTO updatedGroup = groupService.joinGroup(userId, groupId, subtaskId);
            return Response.ok(updatedGroup).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    }


    @POST
    @Path("/{groupId}/leave/{userId}")
    @Transactional
    @RolesAllowed({"USER"})
    public Response leaveGroup(@PathParam("groupId") long groupId, @PathParam("userId") long userId) {
        try {
            groupService.leaveGroup(groupId, userId);return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{groupId}/exchangeRequest")
    @Transactional
    public Response sendExchangeRequest(
            @PathParam("groupId") long groupId,
            @QueryParam("senderId") long senderId,
            @QueryParam("receiverId") long receiverId) {
        try {
            groupService.sendExchangeRequest(groupId, senderId, receiverId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{groupId}/exchangeRequest/{requestId}/process/{receiverId}")
    @Transactional
    public Response processExchangeRequest(
            @PathParam("groupId") long groupId,
            @PathParam("receiverId") long receiverId,
            @PathParam("requestId") long requestId,
            @QueryParam("accept") boolean accept) {
        try {
            groupService.processExchangeRequest(groupId, receiverId, requestId, accept);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{groupId}/remove/{userId}")
    @Transactional
    public Response removeMemberFromGroup(
            @PathParam("groupId") long groupId,
            @PathParam("userId") long userId,
            @QueryParam("substitute") boolean substitute) {
        try {
            groupService.removeMemberFromGroup(groupId, userId, substitute);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }



}
