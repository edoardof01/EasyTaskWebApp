package Endpoints;

import domain.Profile;
import domain.Role;
import domain.Sex;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import orm.CommentDTO;
import orm.ProfileMapper;
import orm.UserDTO;
import orm.UserMapper;
import service.UserService;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserEndpoint {

    @Inject
    private UserService userService;

    @Inject
    private ProfileMapper profileMapper;

    // Get all users
    @GET
    public Response getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return Response.ok(users).build();
    }

    // Get user by ID
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") long id) {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO != null) {
            return Response.ok(userDTO).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
    }

    @GET
    @Path("/username/{username}")
    public Response getUserByUsername(@PathParam("username") String username) {
        UserDTO userDTO = userService.getUserByUsername(username);
        if (userDTO != null) {
            return Response.ok(userDTO).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
    }

    // Create a new user
    @POST
    public Response createUser(UserDTO userDTO) {
        try {
            // Estrai i campi da UserDTO
            int age = userDTO.getAge();
            Sex sex = Sex.valueOf(userDTO.getSex());
            String description = userDTO.getDescription();
            List<String> qualifications = userDTO.getQualifications();
            String profession = userDTO.getProfession();
            Profile personalProfile = profileMapper.toProfileEntity(userDTO.getPersonalProfile());

            // Passa i campi estratti al servizio
            UserDTO createdUser = userService.createUser(
                    age, sex, description, qualifications, profession, personalProfile);

            return Response.status(Response.Status.CREATED)
                    .entity(createdUser)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }


    // Update user by ID
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") long id, UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        if (updatedUser != null) {
            return Response.ok(updatedUser).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
    }

    // Delete user by ID
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") long id) {
        boolean isDeleted = userService.deleteUser(id);
        if (isDeleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
    }

    @POST
    @Path("/{sharedId}/comments")
    public Response makeComment(
            @PathParam("sharedId") long sharedId,
            CommentDTO commentDTO) {
        try {
            // Chiamata al servizio per creare il commento
            CommentDTO createdComment = userService.makeComment(sharedId, commentDTO);

            return Response.status(Response.Status.CREATED)
                    .entity(createdComment)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            // Gestisce errori imprevisti
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the request.")
                    .build();
        }
    }






}

