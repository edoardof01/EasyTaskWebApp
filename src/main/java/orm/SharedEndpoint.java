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

@Path("/shared")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SharedEndpoint {

    @Inject
    private SharedService sharedService;

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
        SharedDTO sharedDTO = sharedService.getSharedById(id);  // Ora restituisce direttamente SharedDTO
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Task with ID " + id + " not found.")
                .build();
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
            ArrayList<Resource> resources = sharedDTO.getResources();
            ArrayList<Subtask> subtasks = sharedDTO.getSubtasks();
            String userGuidance = sharedDTO.getUserGuidance();

            // Passa i campi estratti
            SharedDTO createdShared = sharedService.createShared(
                    name, topic, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, null, userGuidance
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
