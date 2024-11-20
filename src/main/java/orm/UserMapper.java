package orm;

import domain.Profile;
import domain.Role;
import domain.Sex;
import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
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
                Sex.valueOf(userDTO.getSex()),
                userDTO.getDescription(),
                userDTO.getQualifications(),
                userDTO.getProfession(),
                profile,
                Role.valueOf(userDTO.getUserRole())
        );
    }

    public void updateUserFromDTO(UserDTO userDTO, User user) {
        if (userDTO == null || user == null) {
            return;
        }
        user.setAge(userDTO.getAge());
        user.setProfession(userDTO.getProfession());
        user.setQualifications(userDTO.getQualifications());
        user.setDescription(userDTO.getDescription());
    }
}
