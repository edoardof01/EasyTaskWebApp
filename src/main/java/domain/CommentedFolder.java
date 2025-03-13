package domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class CommentedFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @OneToMany
    private List<Shared> shared = new ArrayList<>();

    @OneToOne
    private User user;

    @OneToMany
    private List<Comment> comments;

    public CommentedFolder() {}

    public CommentedFolder(User user) {
        this.user = user;
    }

    public List<Comment> getComment() {
        return comments;
    }
    public void setComment(List<Comment> comments) {
        this.comments = comments;
    }
    public Long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public List<Shared> getShared() {
        return shared;
    }

}
