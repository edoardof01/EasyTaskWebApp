package orm;

import domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    private UserDAO userDAO;

    @Inject
    private UserMapper userMapper;

    public List<UserDTO> getAllUsers() {
        return userDAO.findAll().stream()
                .map(userMapper::toUserDTO)
                .toList();
    }

    public Optional<UserDTO> getUserById(long id) {
        return Optional.ofNullable(userDAO.findById(id))
                .map(userMapper::toUserDTO);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toUserEntity(userDTO);
        userDAO.save(user);
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public Optional<UserDTO> updateUser(long id, UserDTO userDTO) {
        Optional<User> userOptional = Optional.ofNullable(userDAO.findById(id));
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        User user = userOptional.get();
        userMapper.updateUserFromDTO(userDTO, user);
        userDAO.update(user);
        return Optional.of(userMapper.toUserDTO(user));
    }

    @Transactional
    public boolean deleteUser(long id) {
        if (userDAO.findById(id) == null) {
            return false;
        }
        userDAO.delete(id);
        return true;
    }
}
