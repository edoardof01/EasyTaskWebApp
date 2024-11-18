package orm;

import domain.Topic;
import domain.User;

import java.util.HashMap;
import java.util.Map;

public class ProfileDTO {
    private final String username;
    private final String password;
    private Map<Topic, Integer> topics = new HashMap<>();

    public ProfileDTO(User user) {
        this.password = user.getPersonalProfile().getPassword();
        this.topics = user.getPersonalProfile().getTopics();
        this.username = user.getPersonalProfile().getUsername();
    }
    public String getPassword() {
        return password;
    }
    public Map<Topic, Integer> getTopics() {
        return topics;
    }
    public String getUsername() {
        return username;
    }

}
