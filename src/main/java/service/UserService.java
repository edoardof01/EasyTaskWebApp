package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import orm.*;

import java.util.List;

@ApplicationScoped
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    private UserDAO userDAO;

    @Inject
    private UserMapper userMapper;

    @Inject
    CommentMapper commentMapper;

    @Inject
    SharedDAO sharedDAO;

    @Inject
    CommentDAO commentDAO;

    @Inject
    CommentedFolderMapper commentedFolderDAO;

    @Inject
    private CalendarDAO calendarDAO;

    @Inject
    private FolderDAO folderDAO;

    @Inject
    private ProfileDAO profileDAO;


    public List<UserDTO> getAllUsers() {
        return userDAO.findAll().stream()
                .map(userMapper::toUserDTO)
                .toList();
    }


    @Transactional
    public UserDTO getUserById(long id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + id + " not found.");
        }
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO getUserByUsername(String username) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User with username " + username + " not found.");
        }
        return userMapper.toUserDTO(user);
    }


    @Transactional public UserDTO createUser(int age, Sex sex, String description, List<String> qualifications, String profession, Profile personalProfile) {
        UserDTO result = null;
        try {
            User user = new User(age, sex, description, qualifications, profession, personalProfile); // Debug
            logger.debug("Salvando l'utente: {}", user);
            userDAO.save(user);
            profileDAO.save(personalProfile);
            logger.debug("Utente salvato con ID: {}", user.getId());
            CommentedFolder commentedFolder = user.getCommentedFolder();
            commentedFolder.setUser(user);
            commentedFolderDAO.save(commentedFolder);
            logger.debug("CommentedFolder salvato con ID: {}", commentedFolder.getId());
            Calendar calendar = user.getCalendar();
            calendar.setUser(user);
            calendarDAO.save(calendar);
            logger.debug("Calendar salvato con ID: {}", calendar.getId());
            for (Folder folder : user.getFolders()) {
                folder.setUser(user);
                folderDAO.save(folder);
                logger.debug("Folder salvato con ID: {}", folder.getId());
            }
            result = userMapper.toUserDTO(user);
        } catch (Exception e) {
            logger.error("Errore durante la creazione dell'utente", e);
            throw new RuntimeException("Errore durante la creazione dell'utente", e);
        }
        return result;
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
