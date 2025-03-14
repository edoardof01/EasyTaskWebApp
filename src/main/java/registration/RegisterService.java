
package registration;

import domain.Profile;
import domain.RegisteredUser;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import orm.ProfileDAO;
import orm.RegisterDAO;
import orm.UserDAO;

import java.util.UUID;

@ApplicationScoped
public class RegisterService {

    @Inject
    private UserDAO userDAO;

    @Inject
    private RegisterDAO registerDAO;

    @Transactional
    public void register(RegistrationDTO registrationDTO) {
        if (registerDAO.findByUsername(registrationDTO.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        String hashedPassword = BCrypt.hashpw(registrationDTO.getPassword(), BCrypt.gensalt());

        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setUsername(registrationDTO.getUsername());
        registeredUser.setPassword(hashedPassword);
        registerDAO.save(registeredUser);


        // Crea il Profile
        Profile profile = new Profile(
                registrationDTO.getUsername(),
                null
        );

        // Crea l'utente e imposta il profile
        User newUser = new User();
        newUser.setPersonalProfile(profile);

        // Salva l'utente nel database
        userDAO.save(newUser);

    }

}

