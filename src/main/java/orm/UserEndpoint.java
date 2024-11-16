package orm;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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

    // Create a new user
    @POST
    public Response createUser(UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return Response.status(Response.Status.CREATED).entity(createdUser).build();
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
    @Path("/{userId}/groups/{groupId}/subtasks/{subtaskId}")
    public Response joinGroup(
            @PathParam("userId") long userId,
            @PathParam("groupId") long groupId,
            @PathParam("subtaskId") long subtaskId) {
        try {
            UserDTO updatedUser = userService.joinGroup(userId, groupId, subtaskId);
            return Response.ok(updatedUser).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
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

