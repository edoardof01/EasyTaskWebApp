package orm;

import domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    private long id;
    private int age;
    private String profession;
    private List<String> qualifications;
    private String description;
    private Sex sex;
    private ProfileDTO personalProfile;
    private boolean isProfileCompleted;

    public UserDTO() {
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.age = user.getAge();
        this.profession = user.getProfession();
        this.qualifications = user.getQualifications();
        this.description = user.getDescription();
        this.sex = user.getSex();
        this.personalProfile = new ProfileDTO(user.getPersonalProfile());
        this.isProfileCompleted = user.isProfileComplete();

    }

    public UserDTO(long id, int age, String profession, List<String> qualifications, String descriptions, Sex sex, ProfileDTO personalProfile, Role userRole) {
        this.id = id;
        this.age = age;
        this.profession = profession;
        this.qualifications = qualifications;
        this.description = descriptions;
        this.sex = sex;
        this.personalProfile = personalProfile;



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
    public void setSex(Sex sex){
        this.sex = sex;
    }
    public ProfileDTO getPersonalProfile(){
        return personalProfile;
    }
    public void setPersonalProfile(ProfileDTO personalProfile){
        this.personalProfile = personalProfile;
    }

    public boolean isProfileCompleted() {
        return isProfileCompleted;
    }
    public void setProfileCompleted(boolean isProfileCompleted) {
        this.isProfileCompleted = isProfileCompleted;
    }


}
