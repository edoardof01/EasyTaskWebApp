package service;

import orm.GroupDAO;
import orm.SharedDAO;
import orm.TaskDAO;
import domain.Task;
import domain.Topic;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.List;

@SessionScoped
public class FeedService implements Serializable {

    private Topic topicFilter;
    private boolean groupFilter;
    private boolean sharedFilter;

    @Inject
    private TaskDAO taskDAO;

    @Inject
    private GroupDAO groupDAO;

    @Inject
    private SharedDAO sharedDAO;

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

    public List<Task> getFilteredFeed() {
        return taskDAO.findFiltered(topicFilter, groupFilter, sharedFilter);
    }

    public void resetFilters() {
        this.topicFilter = null;
        this.groupFilter = false;
        this.sharedFilter = false;
    }


}
