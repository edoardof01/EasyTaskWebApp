package domain;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
public class CommentedFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private boolean isCompleted;
    @OneToMany
    private ArrayList<Shared> shared;
    @OneToOne
    private User user;
    @OneToMany
    private ArrayList<Comment> comment;

    public CommentedFolder() {}

    public CommentedFolder(User user, ArrayList<Shared> shared, ArrayList<Comment> comment) {
        this.user = user;
        this.shared = shared;
        this.comment = comment;
        this.isCompleted = false;
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

}
