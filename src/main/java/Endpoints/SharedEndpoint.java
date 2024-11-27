package Endpoints;

import domain.*;
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
    private UserMapper userMapper;

    @Inject
    private SessionMapper sessionMapper;

    @Inject
    private SharedMapper sharedMapper;




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
    @Transactional
    public Response createShared(SharedDTO sharedDTO) {
        try {
            // Estrai i campi da sharedDTO
            String name = sharedDTO.getName();

            Topic topic = sharedDTO.getTopic();
            LocalDateTime deadline = sharedDTO.getDeadline();
            int totalTime = sharedDTO.getTotalTime();
            Set<Timetable> timeSlots = sharedDTO.getTimetable();
            Set<DefaultStrategy> strategies = sharedDTO.getStrategies();
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

            User user = userMapper.toUserEntity(sharedDTO.getUser());

            // Passa i campi estratti
            SharedDTO createdShared = sharedService.createShared(
                    name, user, topic, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, sessions, null, userGuidance
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
    @Transactional
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
    @Transactional
    public Response updateShared(@PathParam("sharedId") long sharedId, SharedDTO sharedDTO) {
        try {
            SharedDTO updatedShared = sharedService.modifyShared(
                    sharedId,
                    sharedDTO.getName(),
                    sharedDTO.getTopic(),
                    sharedDTO.getDeadline(),
                    sharedDTO.getTotalTime(),
                    sharedDTO.getTimetable(),
                    sharedDTO.getStrategies(), // Seleziona una strategia
                    sharedDTO.getPriority(),
                    sharedDTO.getDescription(),
                    sharedDTO.getResources().stream().map(resourceMapper::toResourceEntity).collect(Collectors.toList()),
                    sharedDTO.getSubtasks().stream().map(subtaskMapper::toSubtaskEntity).collect(Collectors.toList()),
                    sharedDTO.getUserGuidance()
            );
            return Response.ok(updatedShared).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/{id}")
    @Transactional
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
    @POST
    @Path("/moveToCalendar")
    @Transactional
    public Response moveToCalendar(SharedDTO sharedDTO) {
        try {
            sharedService.moveToCalendar(sharedDTO);
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
    @Transactional
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
    @POST
    @Path("/completeBySessions/{sharedId}")
    @Transactional
    public Response completeSharedBySessions(@PathParam("sharedId") long sharedId, CommentDTO commentDTO) {
        try {
            sharedService.completeSharedBySessions(sharedService.getBestComment(sharedId, commentDTO.getId()), sharedId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/forceCompletion/{sharedId}")
    @Transactional
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


    @POST
    @Path("/completeSession/{sharedId}")
    @Transactional
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
    @POST
    @Path("/handleLimitExceeded/{sharedId}")
    @Transactional
    public Response handleLimitExceeded(@PathParam("sharedId") long sharedId, SessionDTO sessionDTO) {
        try {
            sharedService.handleLimitExceeded(sessionDTO, sharedId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
