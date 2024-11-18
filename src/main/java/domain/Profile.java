package domain;
import domain.Topic;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

import java.util.Map;


@Embeddable
public class Profile {
    private String username;
    private String password;
    @ElementCollection
    private Map<Topic,Integer> topics;
    @OneToOne(mappedBy="personalProfile",fetch= FetchType.EAGER)
    private User owner;

    public Profile() {}

    public Profile(String username,String password, Map<Topic,Integer> topics) {
        this.password = password;
        this.topics = topics;
        this.username = username;
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
    public User getOwner() {
        return owner;
    }
    public String getUsername() {
        return username;
    }
}
