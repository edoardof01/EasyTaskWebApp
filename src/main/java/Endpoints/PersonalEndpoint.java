package Endpoints;

import domain.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import orm.*;
import service.PersonalService;

import java.time.LocalDateTime;
import java.util.*;
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



    @GET
    public Response getAllPersonalTasks() {
        List<PersonalDTO> personalList = personalService.getAllPersonal();
        return Response.ok(personalList).build();
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

    @GET
    @Path("/sessions")
    public Response getAllSessions() {
        List<PersonalDTO> allPersonal = personalService.getAllPersonal();
        List<SessionWithTaskDTO> allSessionDTOs = new ArrayList<>();

        for (PersonalDTO personal : allPersonal) {

            Map<String, SessionDTO> topLevelMap = new HashMap<>();
            for (SessionDTO topSession : personal.getSessions()) {
                String key = topSession.getStartDate() + "#" + topSession.getEndDate();
                topLevelMap.put(key, topSession);
            }

            // Estende le sessioni coi dati necessari
            List<SessionWithTaskDTO> partialList = new ArrayList<>();
            for (SessionDTO topSession : personal.getSessions()) {
                SessionWithTaskDTO dto = new SessionWithTaskDTO();
                dto.setId(topSession.getId());
                dto.setStartDate(topSession.getStartDate());
                dto.setEndDate(topSession.getEndDate());
                dto.setState(topSession.getState());

                dto.setTaskId(personal.getId());
                dto.setTaskName(personal.getName());
                dto.setTaskType("PERSONAL");

                // Nessun subtask di default
                dto.setSubtaskId(null);
                dto.setSubtaskName(null);

                partialList.add(dto);
            }

            // Per ogni sessione del subtask, se esiste una sessione “equivalente” tra i top-level,
            //    aggiorna subtaskId e subtaskName del DTO corrispondente
            if (personal.getSubtasks() != null) {
                for (SubtaskDTO subtask : personal.getSubtasks()) {
                    if (subtask.getSubSessions() != null) {
                        for (SessionDTO subSession : subtask.getSubSessions()) {
                            String key = subSession.getStartDate() + "#" + subSession.getEndDate();

                            // Se la mappa top-level ha una sessione equivalente cerca in partialList
                            if (topLevelMap.containsKey(key)) {
                                for (SessionWithTaskDTO existing : partialList) {
                                    boolean sameStart = existing.getStartDate().equals(subSession.getStartDate());
                                    boolean sameEnd   = existing.getEndDate().equals(subSession.getEndDate());
                                    if (sameStart && sameEnd) {
                                        existing.setSubtaskId(subtask.getId());
                                        existing.setSubtaskName(subtask.getName());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4) Aggiungiamo i DTO creati per questo task alla lista globale
            allSessionDTOs.addAll(partialList);
        }
        return Response.ok(allSessionDTOs).build();
    }




    @POST
    public Response createPersonal(PersonalDTO personalDTO) {
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

            PersonalDTO createdPersonal = personalService.createPersonal(
                    name, personalDTO.getUserId(), topic, deadline, totalTime, timeSlots, strategies, priority,
                    description, resources, subtasks, sessions
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
    @Path("/freeze/{personalId}")
    public Response freezePersonal(@PathParam("personalId") long personalId) {
        try {
            personalService.freezeTask(personalId);
            return Response.status(Response.Status.OK).build();
        }
        catch(Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    @PUT
    @Path("/{personalId}")
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
                    personalDTO.getSessions().stream().map(sessionMapper::toSessionEntity).collect(Collectors.toList())
            );
            return Response.ok(updatedPersonal).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/{personalId}")
    public Response deletePersonal(@PathParam("personalId") long personalId) {
        try {
            personalService.deletePersonal(personalId);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }
    @PUT
    @Path("/moveToCalendar")
    public Response moveToCalendar(@QueryParam("personalId") long personalId) {
        try {
            personalService.moveToCalendar(personalId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }
    @PUT
    @Path("/completeSession/{personalId}/")
    public Response completeSession(@PathParam("personalId") long personalId, @QueryParam("sessionId") long sessionId) {
        try {
            personalService.completeSession(personalId, sessionId);
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/completeBySessions/{personalId}")
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