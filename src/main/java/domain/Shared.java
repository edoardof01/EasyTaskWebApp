package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Entity
@DiscriminatorValue("shared")
public class Shared extends Task {

    private LocalDateTime dateOnFeed;
    private String userGuidance;
    private boolean isOnFeed = false;

    @OneToMany(mappedBy = "commentedTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();

    public Shared() {
    }

    public Shared(String name, User user, Topic topic, @Nullable LocalDateTime deadline,
                  String description, @Nullable List<Subtask> subtasks, List<Session> sessions, int percentageOfCompletion, int priority,
                  Timetable timeTable, int totalTime, List<StrategyInstance> strategies, List<Resource> resources,@Nullable String userGuidance) {
        super(name,user, description, subtasks, sessions, deadline, priority, totalTime, topic, timeTable, strategies, resources);
        this.userGuidance = userGuidance;
        this.isOnFeed = true;
        this.dateOnFeed = LocalDateTime.now();
    }

    public LocalDateTime getDateOnFeed() {
        return dateOnFeed;
    }
    public void setDateOnFeed(LocalDateTime dateOnFeed) {
        this.dateOnFeed = dateOnFeed;
    }
    public String getUserGuidance() {
        return userGuidance;
    }
    public void updateUserGuidance(String text) {
        this.userGuidance = text;
    }
    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    public boolean getIsOnFeed() {
        return isOnFeed;
    }
    public void setIsOnFeed(boolean isOnFeed) {
        this.isOnFeed = isOnFeed;
    }

    private void bestComment(Comment comment) {
        for (Task task : this.getUser().getTasks()) {
            if (task.equals(this)) {
                for (Comment c : this.getComments()) {
                    if (c.getIsBest()) {
                        throw new UnsupportedOperationException("The best comment has been already selected");
                    }
                }
                comment.setIsBest(true);
                comment.getAuthor().incrementTopicScore(this.getTopic());
            }
        }
    }


    @Override
    public void toCalendar() {
        commonToCalendarLogic(this.getUser());
        this.setIsOnFeed(true);

    }

    @Override
    public void handleLimitExceeded() {
        removeAndFreezeTask(this.getUser());
        this.setIsOnFeed(false);
    }

    @Override
    public void deleteTask() {
        if (this.getState() == TaskState.INPROGRESS) {
            this.getUser().getCalendar().removeSessions(this);
            this.setIsOnFeed(false);
        }
        if(this.getState() == TaskState.FINISHED) {
            this.getUser().getCalendar().removeSessions(this);
        }
    }

    @Override
    public void modifyTask() {
        commonModifyLogic(this.getUser());
    }

    @Override
    public void completeTaskBySessions() {
        this.commonCompleteBySessionsLogic(this.getUser());
        this.setIsOnFeed(false);
    }

    public void completeBySessionsAndChooseBestComment(Comment comment) {
        if(!comments.contains(comment)){
        throw new EntityNotFoundException("The comment selected is not found");
        }
        if(this.getState() != TaskState.INPROGRESS) {
         throw new IllegalStateException("The shared task is not in progress");
        }
        this.commonCompleteBySessionsLogic(this.getUser());
        this.setIsOnFeed(false);
        bestComment(comment);
    }

    @Override
    public void forcedCompletion() {
        this.commonForcedCompletionLogic(this.getUser());
        this.setIsOnFeed(false);
    }

    public void removeTaskJustFromFeed() {
        this.setIsOnFeed(false);
    }

    
}









