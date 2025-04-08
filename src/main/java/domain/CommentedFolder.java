package domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Comment> getCommentsForShared(Shared shared) {
        if (shared == null || comments == null) {
            return Collections.emptyList();
        }
        return comments.stream()
                .filter(c -> shared.equals(c.getCommentedTask()))
                .collect(Collectors.toList());
    }

}
