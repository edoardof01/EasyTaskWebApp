package orm;

import domain.*;
import orm.PersonalDTO;
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

@Path("/personal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonalEndpoint {

    @Inject
    private PersonalService personalService;

    @GET
    public Response getAllSharedTasks() {
        List<PersonalDTO> sharedList = personalService.getAllPersonal();
        return Response.ok(sharedList).build();
    }

    @GET
    @Path("/{id}")
    public Response getPersonalById(@PathParam("id") long id) {
        PersonalDTO personalDTO = personalService.getPersonalById(id);

        if (personalDTO == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Task with ID " + id + " not found.")
                    .build();
        }

        return Response.ok(personalDTO).build();
    }

    @POST
    @Transactional
    public Response createPersonal(PersonalDTO personalDTO) {
        try {
            // Estrai i campi da personalDTO
            String name = personalDTO.getName();
            Topic topic = personalDTO.getTopic();
            LocalDateTime deadline = personalDTO.getDeadline();
            int totalTime = personalDTO.getTotalTime();
            Set<Timetable> timeSlots = personalDTO.getTimetable();
            Set<DefaultStrategy> strategies = personalDTO.getStrategies();
            int priority = personalDTO.getPriority();
            String description = personalDTO.getDescription();
            ArrayList<Resource> resources = personalDTO.getResources();
            ArrayList<Subtask> subtasks = personalDTO.getSubtasks();

            // Passa i campi estratti
            PersonalDTO createdPersonal = personalService.createPersonal(
                    name, topic, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, null, null
            );

            return Response.status(Response.Status.CREATED)
                    .entity(createdPersonal)
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
    public Response moveToCalendar(PersonalDTO personalDTO) {
        try {
            personalService.moveToCalendar(personalDTO);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/completeBySessions/{personalId}")
    @Transactional
    public Response completePersonalBySessions(@PathParam("personalId") long personalId) {
        try {
            personalService.completePersonalBySessions( personalId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/handleLimitExceeded/{personalId}")
    @Transactional
    public Response handleLimitExceeded(@PathParam("personalId") long personalId, SessionDTO sessionDTO) {
        try {
            personalService.handleLimitExceeded(sessionDTO, personalId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}