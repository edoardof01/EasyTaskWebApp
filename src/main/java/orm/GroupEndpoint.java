package orm;
import domain.*;
import orm.SharedDTO;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/shared")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupEndpoint {

    @Inject
    private GroupService groupService;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private SubtaskMapper subtaskMapper;



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
    public Response createShared(GroupDTO groupDTO) {
        try {
            // Estrai i campi da groupDTO
            String name = groupDTO.getName();
            int numUsers = groupDTO.getNumUser();
            LocalDateTime dateOnFeed = groupDTO.getDateOnFeed();
            Topic topic = groupDTO.getTopic();
            LocalDateTime deadline = groupDTO.getDeadline();
            int totalTime = groupDTO.getTotalTime();
            Set<Timetable> timeSlots = groupDTO.getTimetable();
            Set<DefaultStrategy> strategies = groupDTO.getStrategies();
            int priority = groupDTO.getPriority();
            String description = groupDTO.getDescription();

            // Mappa i DTO a entità
            List<Resource> resources = groupDTO.getResources().stream()
                    .map(resourceMapper::toResourceEntity)
                    .collect(Collectors.toList());
            List<Subtask> subtasks = groupDTO.getSubtasks().stream()
                    .map(subtaskMapper::toSubtaskEntity)
                    .collect(Collectors.toList());

            // Passa i campi estratti e le entità al servizio
            GroupDTO createdGroup = groupService.createGroup(
                    name, topic, dateOnFeed, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, numUsers, null);

            return Response.status(Response.Status.CREATED)
                    .entity(createdGroup)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }



    @POST
    @Path("/moveToCalendar")
    @Transactional
    public Response moveToCalendar(GroupDTO groupDTO) {
        try {
            groupService.moveToCalendar(groupDTO);
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
    public Response completeSharedBySessions(@PathParam("groupId") long sharedId) {
        try {
            groupService.completeSharedBySessions(sharedId);
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
    @Path("/{groupId}/leave/{userId}")
    @Transactional
    public Response leaveGroup(@PathParam("groupId") long groupId, @PathParam("userId") long userId) {
        try {
            groupService.leaveGroup(groupId, userId);
            return Response.ok().build();
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
