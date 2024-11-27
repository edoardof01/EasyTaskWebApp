package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Entity
@DiscriminatorValue("personal")
public class Personal extends Task {

    public Personal(String name, @NotNull User user, Topic topic, @Nullable LocalDateTime deadline,
                    String description, @Nullable List<Subtask> subtasks, List<Session> sessions, int percentageOfCompletion, int priority,
                    Set<Timetable> timeTable, int totalTime, Set<DefaultStrategy> strategies, List<Resource> resources) {
        super(name, user, description,subtasks,sessions,deadline, percentageOfCompletion, priority, totalTime, topic, timeTable, strategies, resources);
    }

    public Personal() {}

    @Override
    public void handleLimitExceeded() {
        // Rimuovo il task dal calendario, sposto il task dalla cartella InProgress a quella Freezed
        removeAndFreezeTask(this.getUser(), this);
    }

    @Override
    public void toCalendar() {
        commonToCalendarLogic(this.getUser());
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
    }

    @Override
    public void modifyTask( ) {
        commonModifyLogic(this.getUser());
    }
    @Override
    public void completeTaskBySessions() {
        commonCompleteBySessionsLogic(this.getUser());
    }

    @Override
    public void forcedCompletion() {
        commonForcedCompletionLogic(this.getUser());
    }
}


