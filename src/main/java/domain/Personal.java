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
                    Timetable timeTable, int totalTime, List<StrategyInstance> strategies, List<Resource> resources) {
        super(name, user, description,subtasks,sessions,deadline, percentageOfCompletion, priority, totalTime, topic, timeTable, strategies, resources);
    }

    public Personal() {}

    @Override
    public void handleLimitExceeded() {
        removeAndFreezeTask(this.getUser());
    }

    @Override
    public void toCalendar() {
        commonToCalendarLogic(this.getUser());
    }

    @Override
    public void deleteTask() {
        if (this.getState() == TaskState.INPROGRESS) {
            this.getUser().getCalendar().removeSessions(this);
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


