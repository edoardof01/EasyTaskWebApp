package orm;

import domain.*;

import java.util.List;

public class UserDTO {
    private final long id;
    private int age;
    private String profession;
    private List<String> qualifications;
    private String description;
    private  String sex;
    private  ProfileDTO personalProfile;
    private  Role userRole;



    public UserDTO(User user) {
        this.id = user.getId();
        this.age = user.getAge();
        this.profession = user.getProfession();
        this.qualifications = user.getQualifications();
        this.description = user.getDescription();
        this.sex = user.getSex().toString();
        this.personalProfile = new ProfileDTO(user.getPersonalProfile());
        this.userRole = user.getUserRole();

    }
    public long getId() {
        return id;
    }
    public Role getUserRole() {
        return userRole;
    }
    public void setUserRole(Role userRole) {
        this.userRole = userRole;
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
    public String getSex(){
        return sex;
    }
    public void setSex(String sex){
        this.sex = sex;
    }
    public ProfileDTO getPersonalProfile(){
        return personalProfile;
    }
    public void setPersonalProfile(ProfileDTO personalProfile){
        this.personalProfile = personalProfile;
    }

}
