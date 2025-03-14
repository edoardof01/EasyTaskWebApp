
package JWT;
import domain.RegisteredUser;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orm.RegisterDAO;
import orm.UserDAO;

@ApplicationScoped
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Inject
    private UserDAO userDAO;

    @Inject
    private RegisterDAO registerDAO;

    @Inject
    private JwtUtil jwtUtil;

    public TokenResponse authenticate(CredentialsDTO credentials) {
        logger.info("Authenticating user: {}", credentials.getUsername());

        // Recupera l'utente registrato dal database
        RegisteredUser registeredUser = registerDAO.findByUsername(credentials.getUsername());

        // Se l'utente esiste e la password è corretta, genera un token JWT
        if (registeredUser != null && BCrypt.checkpw(credentials.getPassword(), registeredUser.getPassword())) {
            String token = jwtUtil.generateToken(registeredUser.getUsername());
            return new TokenResponse(token);
        }

        // Se le credenziali sono errate, lancia un'eccezione
        throw new IllegalArgumentException("Invalid username or password");
    }
}
