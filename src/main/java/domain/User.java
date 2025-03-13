package domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;

    private int age;

    private boolean isProfileComplete;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(length = 1000)
    private String description;


    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> qualifications;

    private String profession;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(nullable = false)
    private Profile personalProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Calendar calendar;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CommentedFolder commentedFolder;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private  List<Task> tasks = new ArrayList<>();


    public User() {}

    public User(int age, Sex sex, String description, List<String> qualifications, String profession) {
        this.age = age;
        this.description = description;
        this.sex = sex;
        this.qualifications = qualifications;
        this.profession = profession;

        this.calendar = new Calendar();
        calendar.setUser(this);
        CommentedFolder commentedFolder = new CommentedFolder();
        commentedFolder.setUser(this);
        this.commentedFolder = commentedFolder;

    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public boolean isProfileComplete() {
        return isProfileComplete;
    }
    public void setProfileComplete(boolean isProfileComplete) {
        this.isProfileComplete = isProfileComplete;
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
    public void setSex(Sex sex){
        this.sex = sex;
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
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    public List<Task> getTasks() {
        return tasks;
    }
    public CommentedFolder getCommentedFolder() {
        return commentedFolder;
    }
    public void setCommentedFolder(CommentedFolder commentedFolder) {
        this.commentedFolder = commentedFolder;
    }
    public void setPersonalProfile(Profile personalProfile) {
        this.personalProfile = personalProfile;
    }


    public void incrementTopicScore(Topic topic){
        personalProfile.getTopics().put(topic, personalProfile.getTopics().get(topic)+1);
    }

    public Comment makeComment(String content, Shared shared) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (shared == null) {
            throw new IllegalArgumentException("Invalid shared task");
        }
        Comment comment = new Comment(content, this, shared);
        shared.getComments().add(comment);
        this.getCommentedFolder().getShared().add(shared);
        return comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return  age == user.age &&
                Objects.equals(sex, user.sex) &&
                Objects.equals(description, user.description) &&
                Objects.equals(qualifications, user.qualifications) &&
                Objects.equals(profession, user.profession) &&
                Objects.equals(personalProfile.getUsername(), user.personalProfile.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, sex, description, qualifications, profession, personalProfile.getUsername());
    }





}

