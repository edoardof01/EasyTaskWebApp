package JWT;

import domain.Group;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import orm.GroupDAO;
import orm.UserDAO;
import java.util.List;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserDAO userDAO; // DAO per accedere agli utenti

    @Inject
    private GroupDAO groupDAO; // DAO per accedere ai gruppi

    @Inject
    private JwtUtil jwtUtil;

    // Login con groupId aggiunto
    public String login(String username, String password, long groupId) {
        User user = userDAO.findByUsername(username);

        if (user == null || !user.getPersonalProfile().getPassword().equals(password)) { // Usa hashing per password in produzione
            throw new SecurityException("Invalid username or password");
        }

        // Recupera il gruppo
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new SecurityException("Group with ID " + groupId + " not found");
        }

        // Verifica se l'utente è membro del gruppo
        if (!group.getMembers().contains(user)) {
            throw new SecurityException("User is not a member of this group");
        }

        // Recupera i ruoli dell'utente per il gruppo specifico
        List<String> roles = group.getRolesNamesForUser(user);

        return jwtUtil.generateToken(user.getPersonalProfile().getUsername(), roles);
    }
}

