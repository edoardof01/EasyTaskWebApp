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
import service.PersonalService;
import service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/personal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class PersonalEndpoint {

    @Inject
    ResourceMapper resourceMapper;

    @Inject
    SubtaskMapper subtaskMapper;

    @Inject
    SessionMapper sessionMapper;

    @Inject
    PersonalService personalService;

    @Inject
    UserMapper userMapper;

    @Inject
    UserService userService;


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
    public Response createPersonal(PersonalDTO personalDTO) { //Passa l'id dello user, non lo userDTO
        try {
            // Estrai i campi da personalDTO
            String name = personalDTO.getName();
            Topic topic = personalDTO.getTopic();
            LocalDateTime deadline = personalDTO.getDeadline();
            int totalTime = personalDTO.getTotalTime();
            Timetable timeSlots = personalDTO.getTimetable();
            List<StrategyInstance> strategies = personalDTO.getStrategies();
            int priority = personalDTO.getPriority();
            String description = personalDTO.getDescription();
            List<Resource> resources = personalDTO.getResources().stream()
                    .map(resourceMapper::toResourceEntity)
                    .collect(Collectors.toList());
            List<Subtask> subtasks = personalDTO.getSubtasks().stream()
                    .map(subtaskMapper::toSubtaskEntity)
                    .collect(Collectors.toList());
            List<Session> sessions = personalDTO.getSessions().stream()
                    .map(sessionMapper::toSessionEntity)
                    .collect(Collectors.toList());


            // Passa i campi estratti
            PersonalDTO createdPersonal = personalService.createPersonal(
                    name, personalDTO.getUserId(), topic, deadline, totalTime, timeSlots, strategies, priority,  //ricorda che quì ora hai user.getId()
                    description, resources, subtasks, sessions,null, null
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


    @PUT
    @Path("/{personalId}")
    @Transactional
    public Response updatePersonal(@PathParam("personalId") long personalId, PersonalDTO personalDTO) {
        try {
            PersonalDTO updatedPersonal = personalService.modifyPersonal(
                    personalId,
                    personalDTO.getName(),
                    personalDTO.getTopic(),
                    personalDTO.getDeadline(),
                    personalDTO.getTotalTime(),
                    personalDTO.getTimetable(),
                    personalDTO.getStrategies(),
                    personalDTO.getPriority(),
                    personalDTO.getDescription(),
                    personalDTO.getResources().stream().map(resourceMapper::toResourceEntity).collect(Collectors.toList()),
                    personalDTO.getSubtasks().stream().map(subtaskMapper::toSubtaskEntity).collect(Collectors.toList()),
                    personalDTO.getSessions().stream().map(sessionMapper::toSessionEntity).collect(Collectors.toList()),
                    null,
                    null
            );
            return Response.ok(updatedPersonal).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/{personalId}")
    @Transactional
    public Response deletePersonal(@PathParam("personalId") long personalId) {
        try {
            personalService.deletePersonal(personalId);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/moveToCalendar")
    @Transactional
    public Response moveToCalendar(@QueryParam("personalId") long personalId) {
        try {
            personalService.moveToCalendar(personalId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/completeSession/{personalId}/")
    @Transactional
    public Response completeSession(@PathParam("personalId") long personalId, @QueryParam("sessionId") long sessionId) {
        try {
            personalService.completeSession(personalId, sessionId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/completeBySessions/{personalId}")
    @Transactional
    public Response completePersonalBySessions(@PathParam("personalId") long personalId) {
        try {
            personalService.completePersonalBySessions(personalId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
    @PUT
    @Path("/forceCompletion/{personalId}")
    @Transactional
    public Response forceCompletion(@PathParam("personalId") long personalId) {
        try {
            personalService.forceCompletion(personalId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();

        }
    }

    @PUT
    @Path("/handleLimitExceeded/{personalId}/")
    @Transactional
    public Response handleLimitExceeded(@PathParam("personalId") long personalId,@QueryParam("sessionId") long sessionId) {
        try {
            personalService.handleLimitExceeded(sessionId, personalId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}