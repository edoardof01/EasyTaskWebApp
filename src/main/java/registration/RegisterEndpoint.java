
package registration;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/register")
public class RegisterEndpoint {

    @Inject
    private RegisterService registerService;

    @POST
    @Consumes("application/json")
    public Response register(RegistrationDTO registrationDTO) {
        try {
            registerService.register(registrationDTO);
            return Response.status(Response.Status.CREATED).
                    entity("Registration successful.").
                    build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(e.getMessage()).
                    build();
        }
    }

}

