package orm;

import domain.Comment;
import domain.Shared;
import domain.User;

public class CommentDTO {
    private  long id;
    private String content;
    private long authorId;
    private UserDTO author;
    private long sharedId;
    private SharedDTO sharedTask;

    public CommentDTO(){}


    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.author = new UserDTO(comment.getAuthor());
        this.sharedTask = new SharedDTO(comment.getCommentedTask());
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
    public SharedDTO getSharedTask() {
        return sharedTask;
    }
    public UserDTO getAuthor() {
        return author;
    }
    public long getAuthorId(){
        return authorId;
    }
    public long getSharedId() {
        return sharedId;
    }
}
