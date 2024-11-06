package domain;

import jakarta.persistence.*;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String description;
    @ManyToOne
    private User author;
    @OneToOne
    private Shared commentedTask;
    private boolean isBest=false;

    public Comment() {}
    public Comment(String description, User author) {
      this.description = description;
      this.author = author;
    }

    public Long getId() {
      return id;
    }
    public String getDescription() {
      return description;
    }
    public void setDescription(String description) {
      this.description = description;
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