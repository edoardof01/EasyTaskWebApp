package orm;

import domain.Comment;
import domain.Shared;
import domain.User;

public class CommentDTO {
    private final long id;
    private String content;
    private final User author;
    private final Shared sharedTask;

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.author = comment.getAuthor();
        this.sharedTask = comment.getCommentedTask();
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
    public Shared getSharedTask() {
        return sharedTask;
    }
    public User getAuthor() {
        return author;
    }
}
