package JWT;

import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import orm.UserDAO;

import java.util.List;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserDAO userDAO; // DAO per accedere agli utenti

    @Inject
    private JwtUtil jwtUtil;

    public String login(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user == null || !user.getPersonalProfile().getPassword().equals(password)) { // Usa hashing per password in produzione
            throw new SecurityException("Invalid username or password");
        }

        return jwtUtil.generateToken(user.getPersonalProfile().getUsername());
    }
}

