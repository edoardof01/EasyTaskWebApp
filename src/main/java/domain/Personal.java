package domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;



@Entity
@DiscriminatorValue("personal")
public class Personal extends Task {

    public Personal(String name, Topic topic, TaskState state,@Nullable LocalDateTime deadline,
                    String description, int percentageOfCompletion, int complexity, int priority,
                    ArrayList<Timetable> timeTable, int totalTime, DefaultStrategy strategy, ArrayList<Resource> resources) {
        super(name, complexity, description, deadline, percentageOfCompletion, priority, totalTime, topic, state, timeTable, strategy, resources);
    }

    public Personal() {
    }

    @Override
    public void handleLimitExceeded(User user) {
        // Rimuovo il task dal calendario, sposto il task dalla cartella InProgress a quella Freezed
        removeAndFreezeTask(user, this);
    }

    @Override
    public void toCalendar(User user) {
        commonToCalendarLogic(user);
    }

    @Override
    public void deleteTask(User user) {
        user.getCalendar().removeSessions(this);
        ArrayList<Folder> folders = user.getFolders();
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
    public void modifyTask(User user) {
        commonModifyLogic(user);
    }
    @Override
    public void completeTaskBySessions(User user) {
        commonCompleteBySessionsLogic(user);
    }

    @Override
    public void forcedCompletion(User user) {
        commonForcedCompletionLogic(user);
    }
}


