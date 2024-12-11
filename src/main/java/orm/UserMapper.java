package orm;

import domain.Profile;
import domain.Role;
import domain.Sex;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class UserMapper {

    @Inject
    ProfileMapper profileMapper;

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user);
    }

    public User toUserEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        Profile profile = profileMapper.toProfileEntity(userDTO.getPersonalProfile());
        return new User(
                userDTO.getAge(),
                userDTO.getSex(),
                userDTO.getDescription(),
                userDTO.getQualifications(),
                userDTO.getProfession()
                /*profile*/
        );
    }

    public void updateUserFromDTO(UserDTO userDTO, User user) {
        if (userDTO == null || user == null) {
            return;
        }
        // Aggiorna i campi semplici
        user.setAge(userDTO.getAge());
        user.setProfession(userDTO.getProfession());
        user.setDescription(userDTO.getDescription());
        user.setSex(userDTO.getSex());

        // Gestione della lista qualifications
        if (userDTO.getQualifications() != null) {
            user.getQualifications().clear();  // Rimuove tutte le qualifiche attuali
            user.getQualifications().addAll(userDTO.getQualifications());  // Aggiunge le nuove qualifiche
        }else{
            user.getQualifications().clear();}

        Profile profile = user.getPersonalProfile();
        profileMapper.updateProfileFromDTO(profile, userDTO.getPersonalProfile());

    }
}
