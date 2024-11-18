package JWT;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JwtFilter implements ContainerRequestFilter {

    @Inject
    private JwtUtil jwtUtil;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            requestContext.abortWith(jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.UNAUTHORIZED).build());
            return;
        }

        Claims claims = jwtUtil.getClaims(token);
        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        // Configura il SecurityContext
        SecurityContext securityContext = new JwtSecurityContext(username, roles);
        requestContext.setSecurityContext(securityContext);
    }
}
