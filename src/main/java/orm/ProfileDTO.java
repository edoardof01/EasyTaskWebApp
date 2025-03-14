package orm;
import domain.Profile;
import domain.Topic;

import java.util.Map;


public class ProfileDTO {
    private  long id;
    private  String username;

    private Map<Topic, Integer> topics;


    public ProfileDTO(Profile profile) {
        this.topics = profile.getTopics();
        this.username = profile.getUsername();

    }
    public Map<Topic, Integer> getTopics() {
        return topics;
    }
    public void setTopics(Map<Topic, Integer> topics) {
        this.topics = topics;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

}
