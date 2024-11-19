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
    @OneToMany
    private List<Comment> comments;

    public Shared() {
    }

    public Shared(String name, User user, Topic topic, TaskState state, @Nullable LocalDateTime deadline,
                  String description, int percentageOfCompletion, int priority,
                  Set<Timetable> timeTable, int totalTime, Set<DefaultStrategy> strategies, List<Resource> resources) {
        super(name,user, description, deadline, percentageOfCompletion, priority, totalTime, topic, state, timeTable, strategies, resources);
        Feed.getInstance().getShared().add(this);
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

    public void bestComment(Comment comment) {
        if (this.getUser().getTasks().contains(this)) {
            for(Comment c : this.getComments()) {
                if(comment.getIsBest()){
                    throw new UnsupportedOperationException("The best comment has been already selected");
                }
            }
            comment.setIsBest(true);
            comment.getAuthor().incrementTopicScore(this.getTopic());
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
        removeAndFreezeTask(this.getUser(), this);
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));
    }

    @Override
    public void deleteTask() {
        this.getUser().getCalendar().removeSessions(this);
        List<Folder> folders = this.getUser().getFolders();
        boolean taskRemoved = false;
        for (Folder folder : folders) {
            for (Subfolder subfolder : folder.getSubfolders()) {
                if (!taskRemoved && subfolder.getTasks().contains(this)) {
                    subfolder.getTasks().remove(this);
                    taskRemoved = true;
                }
            }
        }
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));

    }

    @Override
    public void modifyTask() {
        commonModifyLogic(this.getUser());
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));
    }

    @Override
    public void completeTaskBySessions() {
        commonCompleteBySessionsLogic(this.getUser());
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));
    }

    public void completeBySessionsAndChooseBestComment(Comment comment) {
        if (comments.contains(comment)) {
            commonCompleteBySessionsLogic(getUser());
            Feed.getInstance().getShared().remove(this);
            Feed.getInstance().getContributors().add((this.getUser()));
            bestComment(comment);
        }
    }

    @Override
    public void forcedCompletion() {
        commonForcedCompletionLogic(this.getUser());
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));
    }


    public void removeTaskJustFromFeed() {
        // Rimuovi il task dal feed
        Feed.getInstance().getShared().remove(this);
        Feed.getInstance().getContributors().add((this.getUser()));

        List<Folder> folders = this.getUser().getFolders();
        boolean taskRemovedFromShared = false;

        for (Folder folder : folders) {
            // Rimuove il task solo se è nella cartella SHARED
            if (folder.getFolderType() == FolderType.SHARED && !taskRemovedFromShared) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (subfolder.getTasks().contains(this)) {
                        subfolder.getTasks().remove(this);
                        taskRemovedFromShared = true; // Indica che è stato rimosso da SHARED
                        break;
                    }
                }
            }

            // Aggiunge il task alla cartella PERSONAL se è stato rimosso da SHARED
            if (folder.getFolderType() == FolderType.PERSONAL && taskRemovedFromShared) {
                for (Subfolder subfolder : folder.getSubfolders()) {
                    if (subfolder.getType() == SubfolderType.INPROGRESS) {
                        subfolder.getTasks().add(this);
                        break; // Aggiunto, non serve continuare
                    }
                }
            }
        }
    }

    
}









