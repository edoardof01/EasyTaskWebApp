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

    @OneToMany(mappedBy = "commentedTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();

    public Shared() {
    }

    public Shared(String name, User user, Topic topic, @Nullable LocalDateTime deadline,
                  String description, @Nullable List<Subtask> subtasks, List<Session> sessions, int percentageOfCompletion, int priority,
                  Timetable timeTable, int totalTime, List<StrategyInstance> strategies, List<Resource> resources,@Nullable String userGuidance) {
        super(name,user, description, subtasks, sessions, deadline, percentageOfCompletion, priority, totalTime, topic, timeTable, strategies, resources);
        Feed.getInstance().getShared().add(this);
        this.userGuidance = userGuidance;
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
        Feed.getInstance().getShared().add(this);
        Feed.getInstance().getContributors().add((this.getUser()));
        this.dateOnFeed = LocalDateTime.now();
    }

    @Override
    public void handleLimitExceeded() {
        // Rimuovo il task dal calendario, sposto il task dalla cartella InProgress a quella Freezed e lo rimuovo dal feed
        removeAndFreezeTask(this.getUser());
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));
    }

    @Override
    public void deleteTask() {
        if (this.getState() == TaskState.INPROGRESS) {
            this.getUser().getCalendar().removeSessions(this);
            Feed.getInstance().getShared().remove(this);
            Feed.getInstance().getContributors().remove((this.getUser()));
        }
        if(this.getState() == TaskState.FINISHED) {
            this.getUser().getCalendar().removeSessions(this);
        }

    }

    @Override
    public void modifyTask() {
        commonModifyLogic(this.getUser());
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));
    }

    @Override
    public void completeTaskBySessions() {
        this.commonCompleteBySessionsLogic(this.getUser());
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().remove((this.getUser()));
    }

    public void completeBySessionsAndChooseBestComment(Comment comment) {
         if(!comments.contains(comment)){
            throw new EntityNotFoundException("The comment selected is not found");
         }
         if(this.getState() != TaskState.INPROGRESS) {
             throw new IllegalStateException("The shared task is not in progress");
         }
        this.commonCompleteBySessionsLogic(this.getUser());
        bestComment(comment);
    }

    @Override
    public void forcedCompletion() {
        this.commonForcedCompletionLogic(this.getUser());
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));
    }

    //giustamente non rimuovo il task dal calendario
    public void removeTaskJustFromFeed() {
        // Rimuovi il task dal feed
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add(this.getUser());
    }

    
}









