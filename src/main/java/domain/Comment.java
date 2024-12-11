package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String content;

    @ManyToOne
    private User author;
    @ManyToOne
    private Shared commentedTask;

    private boolean isBest = false;

    public Comment() {}
    public Comment(String content, User author, Shared commentedTask) {
      this.content = content;
      this.author = author;
      this.commentedTask = commentedTask;
    }

    public Long getId() {
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
    public void setAuthor(User author) {
        this.author = author;
    }
    public Shared getCommentedTask() {
      return commentedTask;
    }
    public void setCommentedTask(Shared commentedTask) {
        this.commentedTask = commentedTask;
    }
    public boolean getIsBest() {
        return isBest;
    }
    public void setIsBest(boolean best) {
        isBest = best;
    }
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", author=" + (author != null ? author.getPersonalProfile().getUsername() : "null") +
                ", commentedTask=" + (commentedTask != null ? commentedTask.getId() : "null") +
                ", isBest=" + isBest +
                '}';
    }

}