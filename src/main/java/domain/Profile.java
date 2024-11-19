package domain;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;


import java.util.Map;


@Embeddable
public class Profile {
    private String username;
    private String password;
    @ElementCollection
    private Map<Topic,Integer> topics;
    private String email;
    private boolean emailVerified;
    private String verificationToken;

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
}
