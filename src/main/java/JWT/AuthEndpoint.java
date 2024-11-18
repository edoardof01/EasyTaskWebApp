package JWT;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthEndpoint {

    @Inject
    private AuthService authService;

    @POST
    @Path("/login")
    public Response login(CredentialsDTO credentials) {
        try {
            TokenResponse tokenResponse = authService.authenticate(credentials);
            return Response.ok(tokenResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}
