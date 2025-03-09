package Endpoints;


import domain.Sex;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import orm.CommentDTO;
import orm.UserDTO;
import service.UserService;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserEndpoint {

    @Inject
    private UserService userService;


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

    @POST
    @Path("/create")
    public Response createUser(UserDTO userDTO, @Context SecurityContext securityContext) {
        try {
            // Estrai il nome utente dal token JWT
            String username = securityContext.getUserPrincipal().getName();

            // Verifica nel servizio se l'utente ha già un profilo
            if (userService.hasUserProfile(username)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("User profile already exists for this account.")
                        .build();
            }

            // Estrai i campi da UserDTO
            int age = userDTO.getAge();
            Sex sex = userDTO.getSex();
            String description = userDTO.getDescription();
            List<String> qualifications = userDTO.getQualifications();
            String profession = userDTO.getProfession();


            // Passa i campi estratti al servizio
            UserDTO createdUser = userService.createUser(
                    age, sex, description, qualifications, profession, username);

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
        }
    }

}

