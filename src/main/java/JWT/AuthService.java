package JWT;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orm.UserDAO;

@ApplicationScoped
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Inject
    private UserDAO userDAO;

    @Inject
    private JwtUtil jwtUtil;

    public TokenResponse authenticate(CredentialsDTO credentials) {
        // Log per tracciare l'inizio del processo di autenticazione
        logger.info("Authenticating user: {}", credentials.getUsername());

        User user = userDAO.findByUsername(credentials.getUsername());
        if (user == null || !BCrypt.checkpw(credentials.getPassword(), user.getPersonalProfile().getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!user.getPersonalProfile().isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified");
        }

        String token = jwtUtil.generateToken(user.getPersonalProfile().getUsername());
        return new TokenResponse(token);
    }
}
