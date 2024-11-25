package domain;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


import java.util.Map;


@Entity
public class Profile {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    @NotNull
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Topic, Integer> topics;

    private String email;
    private boolean emailVerified;
    private String verificationToken;

    @OneToOne(mappedBy="personalProfile",cascade = CascadeType.ALL)
    private User user;

    public Profile() {}

    public Profile(String username,String password, Map<Topic,Integer> topics, String email, boolean emailVerified, String verificationToken) {
        this.password = password;
        this.topics = topics;
        this.username = username;
        this.email = email;
        this.emailVerified = emailVerified;
        this.verificationToken = verificationToken;
    }

    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public Map<Topic,Integer> getTopics(){
        return topics;
    }
    public void setTopics(Map<Topic,Integer> topics){
        this.topics = topics;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public boolean isEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    public String getVerificationToken() {
        return verificationToken;
    }
    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
