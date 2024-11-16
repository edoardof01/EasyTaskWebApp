package domain;

import jakarta.persistence.*;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String content;
    @ManyToOne
    private User author;
    @OneToOne
    private Shared commentedTask;
    private boolean isBest = false;

    public Comment() {}
    public Comment(String content, User author) {
      this.content = content;
      this.author = author;
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
    public Shared getCommentedTask() {
      return commentedTask;
    }


    public boolean getIsBest() {
        return isBest;
    }
    public void setIsBest(boolean best) {
        isBest = best;
    }
}