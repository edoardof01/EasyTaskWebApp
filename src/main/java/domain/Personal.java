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
    public void handleLimitExceeded(User user){
        // Rimuovo il task dal calendario, sposto il task dalla cartella InProgress a quella Freezed
        removeAndFreezeTask(user, this);
    }

    @Override
    public void toCalendar(User user) {
        commonToCalendarLogic(user);
    }

}
