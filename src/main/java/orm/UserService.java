package orm;

import domain.*;
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

    @Inject
    GroupDAO groupDAO;

    @Inject
    GroupMapper groupMapper;

    @Inject
    SubtaskDAO subtaskDAO;

    @Inject
    CommentMapper commentMapper;

    @Inject
    SharedDAO sharedDAO;

    @Inject
    CommentDAO commentDAO;

    @Inject
    CommentedFolderDAO commentedFolderDAO;




    public List<UserDTO> getAllUsers() {
        return userDAO.findAll().stream()
                .map(userMapper::toUserDTO)
                .toList();
    }

    public UserDTO getUserById(long id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + id + " not found.");
        }
        return userMapper.toUserDTO(user);
    }


    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toUserEntity(userDTO);
        userDAO.save(user);
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(long id, UserDTO userDTO) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + id + " not found.");
        }
        userMapper.updateUserFromDTO(userDTO,user);
        userDAO.update(user);
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public boolean deleteUser(long id) {
        if (userDAO.findById(id) == null) {
            return false;
        }
        userDAO.delete(id);
        return true;
    }

    @Transactional
    public UserDTO joinGroup( long userId,long groupId, long subtaskId ) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        Group group = groupDAO.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " not found.");
        }
        Subtask subtask = subtaskDAO.findById(subtaskId);
        if (subtask == null){
            throw new IllegalArgumentException("Subtask with ID " + subtaskId + " not found.");
        }
        user.joinGroup(group, subtask);
        subtaskDAO.update(subtask);
        groupDAO.update(group);
        userDAO.update(user);
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public CommentDTO makeComment(long sharedId, CommentDTO commentDTO) {
        User user = userDAO.findById(commentDTO.getAuthor().getId());
        Shared shared = sharedDAO.findById(sharedId);
        if (user == null) {
            throw new IllegalArgumentException("User with the ID specified not found.");
        }
        Comment comment = commentMapper.toCommentEntity(commentDTO);
        user.makeComment(comment.getContent(),shared);
        sharedDAO.update(shared);
        userDAO.update(user);
        commentDAO.save(comment);
        commentedFolderDAO.update(user.getCommentedFolder());

        return commentMapper.toCommentDTO(comment);
    }


}
