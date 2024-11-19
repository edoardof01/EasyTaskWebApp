//package JWT;
//
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//import jakarta.ws.rs.container.ContainerRequestContext;
//import jakarta.ws.rs.container.ContainerRequestFilter;
//import jakarta.ws.rs.core.Response;
//import jakarta.ws.rs.ext.Provider;
//import io.jsonwebtoken.Claims;
//
//@ApplicationScoped
//@Provider
//public class JwtFilter implements ContainerRequestFilter {
//
//    @Inject
//    private JwtUtil jwtUtil;
//
//    @Override
//    public void filter(ContainerRequestContext requestContext) {
//
//        String path = requestContext.getUriInfo().getPath();
//        if (path.equals("register")) {
//            return;
//        }
//        String token = requestContext.getHeaderString("Authorization");
//
//        // Controlla se il token è presente e valido
//        if (token == null || !jwtUtil.validateToken(token)) {
//            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or missing token").build());
//            return;
//        }
//
//        // Ottieni i claims dal token
//        Claims claims = jwtUtil.getClaims(token);
//
//        // Verifica se l' issuer del token è corretto
//        if (!claims.getIssuer().equals("your-application")) {
//            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token issuer").build());
//            return;
//        }
//
//        // Imposta il SecurityContext con il nome utente
//        String username = claims.getSubject();
//        requestContext.setSecurityContext(new JwtSecurityContext(username));
//    }
//}
