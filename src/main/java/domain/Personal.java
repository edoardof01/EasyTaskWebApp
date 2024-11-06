package domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;



@Entity
@DiscriminatorValue("personal")
public class Personal extends Task {

    public Personal(String name, Topic topic, TaskState state, LocalDateTime deadline,
                    String description, int percentageOfCompletion, int complexity, int priority,
                    Timetable timeTable, int totalTime,DefaultStrategy strategy,ArrayList<Resource> resources) {
        super(name,complexity,description,deadline,percentageOfCompletion,priority,totalTime,topic,state,timeTable,strategy,resources);
    }
    public Personal() {}

    @Override
    public void handleLimitExceeded() {
    }

    @Override
    public void toCalendar(Calendar calendar, User user) {
        commonToCalendarLogic(calendar, user);
    }


}
