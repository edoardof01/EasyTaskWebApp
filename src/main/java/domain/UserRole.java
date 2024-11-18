package domain;

import jakarta.persistence.*;

@Entity
public class UserRole {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Group group;

    @Enumerated(EnumType.STRING)
    private Role role;

    public UserRole() {}

    public UserRole(User user, Group group, Role role) {
        this.user = user;
        this.group = group;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Group getGroup() {
        return group;
    }

    public Role getRole() {
        return role;
    }
}
