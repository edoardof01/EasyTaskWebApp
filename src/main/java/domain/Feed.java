package domain;

import jakarta.persistence.*;

import java.util.ArrayList;


public class Feed {

    private Long id;
    private Topic topicFilter;
    private boolean groupFilter;
    private boolean sharedFilter;
    private ArrayList<Group> groups;
    private ArrayList<Shared> shared;
    private ArrayList<User> contributors;
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
    public ArrayList<Group> getGroup() {
        return groups;
    }
    public ArrayList<Shared> getShared() {
        return shared;
    }
    public ArrayList<User> getContributors() {
        return contributors;
    }

    public ArrayList<Task> getFilteredFeed() {
        ArrayList<Task> filteredFeed = new ArrayList<>();

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
