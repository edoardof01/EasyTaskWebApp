package domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


public class Feed {

    private Long id;
    private Topic topicFilter;
    private boolean groupFilter;
    private boolean sharedFilter;
    private List<Group> groups = new ArrayList<>();
    private List<Shared> shared = new ArrayList<>();
    private List<User> contributors = new ArrayList<>();
    private static final Feed feedInstance = new Feed();

    public Feed() {}

    public static Feed getInstance() {
        return feedInstance;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {}
    public Topic getTopicFilter() {
        return topicFilter;
    }
    public void setTopicFilter(Topic topicFilter) {
        this.topicFilter = topicFilter;
    }
    public boolean isGroupFilter() {
        return groupFilter;
    }
    public void setGroupFilter(boolean groupFilter) {
        this.groupFilter = groupFilter;
    }
    public boolean isSharedFilter() {
        return sharedFilter;
    }
    public void setSharedFilter(boolean sharedFilter) {
        this.sharedFilter = sharedFilter;
    }
    public List<Group> getGroup() {
        return groups;
    }
    public void setGroup(List<Group> groups) {
        this.groups = groups;
    }
    public List<Shared> getShared() {
        return shared;
    }
    public void setShared(List<Shared> shared) {
        this.shared = shared;
    }
    public List<User> getContributors() {
        return contributors;
    }
    public void setContributors(List<User> contributors) {
        this.contributors = contributors;
    }

    public List<Task> getFilteredFeed() {
        List<Task> filteredFeed = new ArrayList<>();

        // Aggiungi task di gruppo se il filtro groupFilter è attivo
        if (groupFilter) {
            filteredFeed.addAll(groups);
        }

        // Aggiungi task condivisi se il filtro sharedFilter è attivo
        if (sharedFilter) {
            filteredFeed.addAll(shared);
        }

        // Applica il filtro per topic se è impostato
        if (topicFilter != null) {
            filteredFeed.removeIf(task -> !task.getTopic().equals(topicFilter));
        }

        return filteredFeed;
    }


}
