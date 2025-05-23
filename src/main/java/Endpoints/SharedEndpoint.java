package Endpoints;

import domain.*;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import orm.*;
import service.SharedService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/shared")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class SharedEndpoint {

    @Inject
    private SharedService sharedService;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private SubtaskMapper subtaskMapper;

    @Inject
    private SessionMapper sessionMapper;

    @Inject
    private SharedMapper sharedMapper;

    @Inject
    private CommentMapper commentMapper;


    /**
     * Ottieni tutti i task Shared.
     */
    @GET
    public Response getAllSharedTasks() {
        List<SharedDTO> sharedList = sharedService.getAllShared();
        return Response.ok(sharedList).build();
    }

    /**
     * Ottieni un task Shared per ID.
     */
    @GET
    @Path("/{id}")
    public Response getSharedById(@PathParam("id") long id) {
        SharedDTO sharedDTO = sharedService.getSharedById(id);

        if (sharedDTO == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Task with ID " + id + " not found.")
                    .build();
        }

        return Response.ok(sharedDTO).build();
    }


    @POST
    public Response createShared(SharedDTO sharedDTO) {
        try {
            // Estrai i campi da sharedDTO
            String name = sharedDTO.getName();

            Topic topic = sharedDTO.getTopic();
            LocalDateTime deadline = sharedDTO.getDeadline();
            int totalTime = sharedDTO.getTotalTime();
            Timetable timeSlots = sharedDTO.getTimetable();
            List<StrategyInstance> strategies = sharedDTO.getStrategies();
            int priority = sharedDTO.getPriority();
            String description = sharedDTO.getDescription();
            String userGuidance = sharedDTO.getUserGuidance();
            List<Resource> resources = sharedDTO.getResources().stream()
                    .map(resourceMapper::toResourceEntity)
                    .collect(Collectors.toList());
            List<Subtask> subtasks = sharedDTO.getSubtasks().stream()
                    .map(subtaskMapper::toSubtaskEntity)
                    .collect(Collectors.toList());
            List<Session> sessions = sharedDTO.getSessions().stream()
                    .map(sessionMapper::toSessionEntity)
                    .collect(Collectors.toList());

            // Passa i campi estratti
            SharedDTO createdShared = sharedService.createShared(
                    name, sharedDTO.getUserId(), topic, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, sessions, userGuidance
            );

            return Response.status(Response.Status.CREATED)
                    .entity(createdShared)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/addToFeed/{sharedId}")
    public Response addToFeed(@PathParam("sharedId") long sharedId, String guidance ) {
        try {
            SharedDTO sharedDTO = sharedService.getSharedById(sharedId);
            Shared shared = sharedMapper.toSharedEntity(sharedDTO);
            sharedService.addTaskToFeed(shared,guidance);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @PUT
    @Path("/{sharedId}")
    public Response updateShared(@PathParam("sharedId") long sharedId, SharedDTO sharedDTO) {
        try {
            SharedDTO updatedShared = sharedService.modifyShared(
                    sharedId,
                    sharedDTO.getName(),
                    sharedDTO.getTopic(),
                    sharedDTO.getDeadline(),
                    sharedDTO.getTotalTime(),
                    sharedDTO.getTimetable(),
                    sharedDTO.getStrategies(),
                    sharedDTO.getPriority(),
                    sharedDTO.getDescription(),
                    sharedDTO.getResources().stream().map(resourceMapper::toResourceEntity).collect(Collectors.toList()),
                    sharedDTO.getSubtasks().stream().map(subtaskMapper::toSubtaskEntity).collect(Collectors.toList()),
                    sharedDTO.getUserGuidance(),
                    sharedDTO.getSessions().stream().map(sessionMapper::toSessionEntity).collect(Collectors.toList()),
                    null
            );
            return Response.ok(updatedShared).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/{id}")
    public Response deleteShared(@PathParam("id") long id) {
        try {
            sharedService.deleteShared(id);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }


    /**
     * Sposta il task Shared nel calendario.
     */
    @PUT
    @Path("/moveToCalendar")
    public Response moveToCalendar(@QueryParam("sharedId") long sharedId) {
        try {
            sharedService.moveToCalendar(sharedId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Rimuovi il task Shared dal feed.
     */
    @DELETE
    @Path("/removeFromFeed/{sharedId}")
    public Response removeSharedFromFeed(@PathParam("sharedId") long sharedId) {
        try {
            sharedService.removeSharedFromFeed(sharedId);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Completa il task Shared in base alle sessioni e, se specificato, assegna il miglior commento.
     */
    @PUT
    @Path("/completeBySessionsWithComment/{sharedId}")
    public Response completeSharedBySessionsWithComment(@PathParam("sharedId") long sharedId,@QueryParam("commentId") Long commentId) {
        try {
            sharedService.completeSharedBySessionsWithComment(commentId, sharedId); // Se commentDTO non è null, usalo
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
    @PUT
    @Path("/completeBySessions/{sharedId}")
    public Response completeSharedBySessions(@PathParam("sharedId") long sharedId) {
        try {
            sharedService.completeSharedBySessions(sharedId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }



    @PUT
    @Path("/forceCompletion/{sharedId}")
    public Response forceCompletion(@PathParam("sharedId") long sharedId) {
        try {
            sharedService.forceCompletion(sharedId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }


    @PUT
    @Path("/completeSession/{sharedId}")
    public Response completeSession(@PathParam("sharedId") long sharedId, @QueryParam("sessionId") long sessionId) {
        try {
            sharedService.completeSession(sharedId, sessionId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Gestisce il caso di superamento del limite per una sessione.
     */
    @PUT
    @Path("/handleLimitExceeded/{sharedId}")
    public Response handleLimitExceeded(@PathParam("sharedId") long sharedId, @QueryParam("sessionId") long sessionId) {
        try {
            sharedService.handleLimitExceeded(sessionId, sharedId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
