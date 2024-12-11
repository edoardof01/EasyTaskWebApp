package orm;

import domain.Comment;
import domain.Shared;
import domain.User;
import jakarta.inject.Inject;

public class CommentMapper {

    @Inject
    private UserMapper userMapper;

    @Inject
    private SharedMapper sharedMapper;

    public CommentDTO toCommentDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDTO(comment);
    }
    public Comment toCommentEntity(CommentDTO commentDTO) {
        if (commentDTO == null) {
            return null;
        }
        User author = userMapper.toUserEntity(commentDTO.getAuthor());
        Shared shared = sharedMapper.toSharedEntity(commentDTO.getSharedTask());
        return new Comment(
                commentDTO.getContent(),
                author,
                shared
        );
    }
    public void updateCommentFromDTO(CommentDTO commentDTO, Comment comment) {
        if (comment == null || commentDTO == null) {
            return;
        }
        comment.setContent(commentDTO.getContent());
    }
}
