/*
package JWT;
import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

public class JwtSecurityContext implements SecurityContext {

    private final String username;

    public JwtSecurityContext(String username) {
        this.username = username;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> username; // Ritorna un Principal con il nome utente
    }

    @Override
    public boolean isUserInRole(String role) {
        // Nessuna gestione dei ruoli nella tua applicazione
        return false;
    }

    @Override
    public boolean isSecure() {
        // Puoi personalizzare questa logica se hai HTTPS o altre misure di sicurezza
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer"; // Schema standard per token JWT
    }
}
*/
