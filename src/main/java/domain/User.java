package domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class User {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private long id;
    private int age;
    @Enumerated(EnumType.STRING)
    private Sex sex;
    @Lob
    private String description;
    @ElementCollection
    //@CollectionTable(name="qualifications")
    private List<String> qualifications;
    private String profession;
    @Embedded
    private Profile personalProfile;
    @OneToOne(mappedBy="user",cascade = CascadeType.ALL,orphanRemoval = true)
    private Calendar calendar;
    @OneToOne
    private CommentedFolder commentedFolder;
    @OneToMany(mappedBy ="user",cascade = CascadeType.ALL,orphanRemoval = true)
    private final List<Folder> folders = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    private final List<Task> tasks = new ArrayList<>();

    public User() {}

    public User(int age, Sex sex, String description, List<String> qualifications, String profession, Profile personalProfile) {
        this.age = age;
        this.description = description;
        this.qualifications = qualifications;
        this.profession = profession;
        this.personalProfile = personalProfile;

        // Creazione dei Folder per ciascun FolderType
        this.folders.add(new Folder(FolderType.PERSONAL,this));
        this.folders.add(new Folder(FolderType.SHARED, this));
        this.folders.add(new Folder(FolderType.GROUP,this));
        this.calendar = new Calendar();
        this.commentedFolder = new CommentedFolder();
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

    // DA COMPLETARE, METODO RICHIAMATO POI
    public void incrementTopicScore(Topic topic){
        personalProfile.getTopics().put(topic, personalProfile.getTopics().get(topic)+1);
    }

    public void joinGroup(@NotNull Group group, Subtask subtask){
        if(!group.getSubtasks().contains(subtask) || (group.getSubtasks().contains(subtask) && group.getTakenSubtasks().containsValue(subtask)) ){
            throw new IllegalArgumentException("Subtask does not exist or is already taken");
        }
        if(group.getIsComplete()){
            group.addMember(this);
            group.assignSubtaskToUser(this, subtask);
            group.getCalendar().addSessions(this, subtask);
        }
    }

    public void makeComment(String content , Shared shared){
        if(content.isEmpty()){
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if(shared == null || !Feed.getInstance().getShared().contains(shared)){
            return;
        }
        Comment comment = new Comment(content,this,shared);
        shared.getComments().add(comment);
        this.getCommentedFolder().getShared().add(shared);
    }
}

