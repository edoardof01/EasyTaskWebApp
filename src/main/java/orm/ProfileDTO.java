package orm;
import domain.Profile;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileDTO {
    private  String username;
    private  String password;
    private  String email;
    private Map<String, Integer> topics;
    private boolean emailVerified;
    private String verificationToken;

    public ProfileDTO() {}

    public ProfileDTO(Profile profile) {
        this.password = profile.getPassword();
        this.topics = profile.getTopics().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(), // Converti l' enum in stringa
                        Map.Entry::getValue // Mantieni il valore intero
                ));
        this.username = profile.getUsername();
        this.email = profile.getEmail();
        this.emailVerified = profile.isEmailVerified();
        this.verificationToken = profile.getVerificationToken();
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Map<String, Integer> getTopics() {
        return topics;
    }
    public void setTopics(Map<String, Integer> topics) {
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
