
package registration;

import domain.Profile;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import orm.UserDAO;

import java.util.UUID;

@ApplicationScoped
public class RegisterService {

    @Inject
    private UserDAO userDAO;



    public void register(RegistrationDTO registrationDTO) {
        if (userDAO.findByUsername(registrationDTO.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        String hashedPassword = BCrypt.hashpw(registrationDTO.getPassword(), BCrypt.gensalt());

        // Crea il Profile
        Profile profile = new Profile(
                registrationDTO.getUsername(),
                hashedPassword,
                null /*, ci sarebbe la mail quì */
        );

        // Crea l'utente e imposta il profile
        User newUser = new User();
        newUser.setPersonalProfile(profile);

        // Salva l'utente nel database
        userDAO.save(newUser);

    }

   /* public void confirmEmail(String token) {
        User user = userDAO.findByVerificationToken(token);
        if (user == null) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        // Aggiorna lo stato dell'email dell'utente
        Profile profile = user.getPersonalProfile();
        userDAO.update(user);
    }*/
}

