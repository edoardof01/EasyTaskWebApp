package domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

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
    @OneToOne(mappedBy="user")
    private Calendar calendar;
    @OneToMany(mappedBy ="user")
    private final ArrayList<Folder> folders = new ArrayList<>();
    @OneToMany
    private final ArrayList<Task> tasks = new ArrayList<>();

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
    public ArrayList<Folder> getFolders() {
        return folders;
    }
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task){
        tasks.add(task);
    }
    public void removeTask(Task task){
        tasks.remove(task);
    }
    // DA COMPLETARE, METODO RICHIAMATO POI
    public void incrementTopicScore(Topic topic){
        personalProfile.getTopics().put(topic, personalProfile.getTopics().get(topic)+1);
    }
    // FORSE DOVREMO FARE QUALCOSA CON IL FEED
    public void joinGroup(@NotNull Group group, Subtask subtask){
        if(!group.isComplete()){
            group.addMember(this);
            group.assignSubtaskToUser(this, subtask);
        }
    }
    public void addSubtaskToCalendar(Group group) {
        if (group.getTakenSubtasks().containsKey(this)) {
            Subtask subtask = group.getTakenSubtasks().get(this);
            if (this.calendar != null) {
                group.toCalendar(this);
                System.out.println("Subtask added to calendar for user: " + this.id);
            }
        } else {
            throw new IllegalArgumentException("No subtask assigned to this user in the group");
        }
    }



}
