package JWT;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.List;

public class JwtSecurityContext implements SecurityContext {

    private final String username;
    private final List<String> roles;

    public JwtSecurityContext(String username, List<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> username;
    }

    @Override
    public boolean isUserInRole(String role) {
        return roles.contains(role);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}

