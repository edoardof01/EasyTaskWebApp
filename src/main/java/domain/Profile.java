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

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<Topic, Integer> topics;

    public Profile() {}

    public Profile(String username,Map<Topic,Integer> topics) {

        this.topics = topics;
        this.username = username;
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
    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
}
