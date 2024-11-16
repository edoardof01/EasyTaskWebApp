package orm;

import domain.Comment;
import domain.User;

public class CommentDTO {
    private final long id;
    private String content;
    private final User author;

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.author = comment.getAuthor();
    }
    public long getId() {
        return id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public User getAuthor() {
        return author;
    }
}
