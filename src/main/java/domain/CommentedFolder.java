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
    private ArrayList<Shared> shared;
    @OneToOne
    private User user;
    @OneToMany
    private ArrayList<Comment> comment;

    public CommentedFolder() {}

    public CommentedFolder(User user) {
        this.user = user;
    }

    public ArrayList<Comment> getComment() {
        return comment;
    }
    public void setComment(ArrayList<Comment> comment) {
        this.comment = comment;
    }
    public Long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public List<Shared> getShared() {
        return shared;
    }

}
