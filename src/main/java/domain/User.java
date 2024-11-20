package domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;

    private int age;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private Role userRole;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> qualifications;

    private String profession;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile personalProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Calendar calendar;

    @OneToOne(mappedBy = "user")
    private CommentedFolder commentedFolder;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<Folder> folders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<Task> tasks = new ArrayList<>();
    public User() {}

    public User(int age, Sex sex, String description, List<String> qualifications, String profession, Profile personalProfile, Role userRole) {
        this.age = age;
        this.description = description;
        this.sex = sex;
        this.qualifications = qualifications;
        this.profession = profession;
        this.personalProfile = personalProfile;
        this.userRole = userRole;

        // Creazione dei Folder per ciascun FolderType
        this.folders.add(new Folder(FolderType.PERSONAL,this));
        this.folders.add(new Folder(FolderType.SHARED, this));
        this.folders.add(new Folder(FolderType.GROUP,this));
        this.calendar = new Calendar();
        this.commentedFolder = new CommentedFolder();
    }

    public Role getUserRole() {
        return userRole;
    }
    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }
    public long getId() {
        return id;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getDescription() {
        return description;
    }
    public Sex getSex() {
        return sex;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<String> getQualifications() {
        return qualifications;
    }
    public void setQualifications(List<String> qualifications) {
        this.qualifications = qualifications;
    }
    public String getProfession() {
        return profession;
    }
    public void setProfession(String profession) {
        this.profession = profession;
    }
    public Profile getPersonalProfile() {
        return personalProfile;
    }
    public Calendar getCalendar() {
        return calendar;
    }
    public List<Folder> getFolders() {
        return folders;
    }
    public List<Task> getTasks() {
        return tasks;
    }
    public CommentedFolder getCommentedFolder() {
        return commentedFolder;
    }
    public void setPersonalProfile(Profile personalProfile) {
        this.personalProfile = personalProfile;
    }

    // DA COMPLETARE, METODO RICHIAMATO POI
    public void incrementTopicScore(Topic topic){
        personalProfile.getTopics().put(topic, personalProfile.getTopics().get(topic)+1);
    }

    public void makeComment(String content , Shared shared) {
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (shared == null || !Feed.getInstance().getShared().contains(shared)) {
            return;
        }
        Comment comment = new Comment(content, this, shared);
        shared.getComments().add(comment);
        this.getCommentedFolder().getShared().add(shared);
    }
}

