package service;

import domain.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import orm.*;

import java.util.List;

@ApplicationScoped
public class UserService {

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


    @Transactional
    public UserDTO createUser(int age, Sex sex, String description, List<String> qualifications, String profession, String username) {
        User user = userDAO.findByUsername(username);
        // Verifica se l'utente ha già un profilo
        if (user.isProfileComplete()) {
            throw new IllegalArgumentException("User profile already exists for this account.");
        }
        if(age<16){
            throw new IllegalArgumentException("you must be at least 16 years old.");
        }
        if(age>100){
            throw new IllegalArgumentException("you can't be that old.");
        }

        // Crea il profilo e lo associa all'utente
        user.setAge(age);
        user.setSex(sex);
        user.setDescription(description);
        user.setQualifications(qualifications);
        user.setProfession(profession);
        user.setProfileComplete(true);
        userDAO.update(user);
        CommentedFolder commentedFolder = new CommentedFolder();
        user.setCommentedFolder(commentedFolder);
        commentedFolder.setUser(user);
        commentedFolderDAO.save(commentedFolder);
        Calendar calendar = new Calendar();
        user.setCalendar(calendar);
        calendar.setUser(user);
        calendarDAO.save(calendar);

        return userMapper.toUserDTO(user);
    }


    public boolean hasUserProfile(String username) {
        User user = userDAO.findByUsername(username);
        return user.isProfileComplete();
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
        User user = userDAO.findById(commentDTO.getAuthorId());

        if (user == null) {
            throw new IllegalArgumentException("User with ID " + commentDTO.getAuthorId() + " not found.");
        }

        Shared shared = sharedDAO.findById(sharedId);
        System.out.println("Shared correctly created. Name: " + shared.getName());
        User taskOwner = userDAO.findById(shared.getUser().getId());
        // Delego la logica al domain model
        Comment comment = user.makeComment(commentDTO.getContent(), shared);
        comment.setAuthor(user);


        commentDAO.save(comment);

        // Persistenza
        userDAO.update(user);
        userDAO.update(taskOwner);
        sharedDAO.update(shared); // Aggiorna il task condiviso con il nuovo commento

       // Salva il commento
        System.out.println("Comment created in User.makeComment:");
        System.out.println("Author: " + comment.getAuthor().getPersonalProfile().getUsername());
        System.out.println("Content: " + comment.getContent());
        System.out.println("Shared Task: " + shared.getName());

        return commentMapper.toCommentDTO(comment);
    }

    @Transactional
    public List<CommentDTO> getCommentsOfShared(long userId, long sharedId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        Shared shared = sharedDAO.findById(sharedId);
        if (shared == null) {
            throw new IllegalArgumentException("Shared task with ID " + sharedId + " not found.");
        }
        CommentedFolder commentedFolder = user.getCommentedFolder();
        if (commentedFolder == null) {
            throw new IllegalArgumentException("No commented folder found for user with ID " + userId);
        }
        List<Comment> comments = commentedFolder.getCommentsForShared(shared);
        return comments.stream()
                .map(commentMapper::toCommentDTO)
                .toList();
    }


}




