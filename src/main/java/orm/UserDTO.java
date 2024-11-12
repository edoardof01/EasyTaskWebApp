package orm;

import domain.*;

import java.util.List;

public class UserDTO {
    private final long id;
    private int age;
    private String profession;
    private List<String> qualifications;
    private String description;
    private final Sex sex;
    private final Profile personalProfile;

    public UserDTO(long id, int age, String profession, List<String> qualifications, String description, Sex sex, Profile personalProfile) {
        this.id = id;
        this.age = age;
        this.profession = profession;
        this.qualifications = qualifications;
        this.description = description;
        this.sex = sex;
        this.personalProfile = personalProfile;
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.age = user.getAge();
        this.profession = user.getProfession();
        this.qualifications = user.getQualifications();
        this.description = user.getDescription();
        this.sex = user.getSex();
        this.personalProfile = user.getPersonalProfile();
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
    public String getProfession() {
        return profession;
    }
    public void setProfession(String profession) {
        this.profession = profession;
    }
    public List<String> getQualifications() {
        return qualifications;
    }
    public void setQualifications(List<String> qualifications) {
        this.qualifications = qualifications;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Sex getSex(){
        return sex;
    }
    public Profile getPersonalProfile(){
        return personalProfile;
    }

}
