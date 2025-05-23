package Endpoints;
import domain.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import orm.*;
import service.GroupService;

import java.time.LocalDateTime;
import java.util.List;
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
    public Response createGroup(GroupSubtaskWrapperDTO wrapper) {
        try {
            // Estrai i DTO dal wrapper
            GroupDTO groupDTO = wrapper.getGroupDTO();
            SubtaskDTO chosenSubtaskDTO = wrapper.getChosenSubtaskDTO();

            // Estrai i campi da groupDTO
            String name = groupDTO.getName();
            Integer numUsers = groupDTO.getNumUser();
            Topic topic = groupDTO.getTopic();
            LocalDateTime deadline = groupDTO.getDeadline();
            int totalTime = groupDTO.getTotalTime();
            Timetable timeSlots = groupDTO.getTimetable();
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
            Subtask chosenSubtask = subtaskMapper.toSubtaskEntity(chosenSubtaskDTO);



            // Passa i campi estratti e le entità al servizio
            GroupDTO createdGroup = groupService.createGroup(
                    name, groupDTO.getUserId(), topic, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, chosenSubtask, sessions, numUsers);

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
    public Response updateGroup(@PathParam("id") long id, GroupDTO groupDTO) {
        try {
            GroupDTO updatedGroup = groupService.modifyGroup(
                    id,
                    groupDTO.getName(),
                    groupDTO.getTopic(),
                    groupDTO.getDeadline(),
                    groupDTO.getTotalTime(),
                    groupDTO.getTimetable(),
                    groupDTO.getStrategies(),
                    groupDTO.getPriority(),
                    groupDTO.getDescription(),
                    groupDTO.getResources().stream().map(resourceMapper::toResourceEntity).collect(Collectors.toList()),
                    groupDTO.getSubtasks().stream().map(subtaskMapper::toSubtaskEntity).collect(Collectors.toList()),
                    groupDTO.getSessions().stream().map(sessionMapper ::toSessionEntity).collect(Collectors.toList()),
                    groupDTO.getNumUser()
            );
            return Response.ok(updatedGroup).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }



    @PUT
    @Path("/moveToCalendar/{groupId}/{userId}")
    public Response moveToCalendar(@PathParam("groupId") long groupId, @PathParam("userId") long userId) {
        try {
            groupService.moveToCalendar(groupId,userId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }


    @PUT
    @Path("/toFeed/{groupId}")
    public Response toFeed(@PathParam("groupId") long groupId) {
        try{
            groupService.moveToFeed(groupId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }


    @PUT
    @Path("/completeSubtaskSession/{userId}/{groupId}/{subtaskId}")
    public Response completeSubtaskSession(@PathParam("userId")long userId,@PathParam("groupId") long groupId,@PathParam("subtaskId") long subtaskId, @QueryParam("sessionId") long sessionId ) {
        try{
            groupService.completeSubtaskSession(userId,groupId,subtaskId,sessionId);
            return Response.status(Response.Status.OK).build();

        } catch (IllegalArgumentException e){
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        }
    }


    @PUT
    @Path("/completeBySessions/{groupId}")
    public Response completeGroupBySessions(@PathParam("groupId") long groupId) {
        try {
            groupService.completeGroupBySessions(groupId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/forceCompletion/{groupId}")
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

    @PUT
    @Path("/handleLimitExceeded/{groupId}")
    public Response handleLimitExceeded(@PathParam("groupId") long groupId,@QueryParam("sessionId") long sessionId) {
        try {
            groupService.handleLimitExceeded(sessionId, groupId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/joinGroup/{groupId}/users/{userId}/subtasks/{subtaskId}")
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


    @PUT
    @Path("/{groupId}/leave/{userId}")
    public Response leaveGroup(@PathParam("groupId") long groupId, @PathParam("userId") long userId) {
        try {
            groupService.leaveGroup(groupId, userId);return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/{groupId}/remove/{adminId}/{userId}")
    public Response removeMemberFromGroup(
            @PathParam("groupId") long groupId,
            @PathParam("adminId") long adminId,
            @PathParam("userId") long userId,
            @QueryParam("substitute") boolean substitute) {
        try {
            groupService.removeMemberFromGroup(groupId, adminId, userId, substitute);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }



}
