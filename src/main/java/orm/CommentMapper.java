package orm;

import domain.Comment;

public class CommentMapper {

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
        return new Comment(
                commentDTO.getContent(),
                commentDTO.getAuthor(),
                commentDTO.getSharedTask()
        );
    }
    public void updateCommentFromDTO(CommentDTO commentDTO, Comment comment) {
        if (comment == null || commentDTO == null) {
            return;
        }
        comment.setContent(commentDTO.getContent());
    }
}
