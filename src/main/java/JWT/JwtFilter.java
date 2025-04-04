package JWT;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import io.jsonwebtoken.Claims;
import orm.RegisterDAO;
import service.UserService;
@ApplicationScoped
@Provider
public class JwtFilter implements ContainerRequestFilter {

    @Inject
    private JwtUtil jwtUtil;
    @Inject
    private UserService userService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        if (isPublicEndpoint(path)) return;
        String token = requestContext.getHeaderString("Authorization");

        if (token == null || !jwtUtil.validateToken(token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or missing token").build());
            return;
        }
        Claims claims = jwtUtil.getClaims(token);
        // Verifica l'Issuer del token
        if (!claims.getIssuer().equals("EasyTask")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid token issuer").build());
            return;
        }
        String username = claims.getSubject();
        if (!isProfileComplete(username)) {
            // Se il profilo non è completo, permetti solo l'accesso a /user/create
            if (!isProfileCreationEndpoint(path)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("You must complete your profile to access this resource.")
                        .build());
                return;
            }
        } else {
            // Se il profilo è completo, impedisci l'accesso all' Endpoint /user/create
            if (isProfileCreationEndpoint(path)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("Profile already completed. You cannot create a profile again.")
                        .build());
                return;
            }
        }
        // Imposta il SecurityContext
        requestContext.setSecurityContext(new JwtSecurityContext(username));
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/login") || path.startsWith("/register");
    }

    private boolean isProfileCreationEndpoint(String path) {
        return path.startsWith("/users/create");
    }

    private boolean isProfileComplete(String username) {
        return userService.hasUserProfile(username);
    }

}
