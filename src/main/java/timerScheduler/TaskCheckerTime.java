package timerScheduler;
import domain.Personal;
import domain.TaskState;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import service.PersonalService;
import java.time.ZoneId;
import java.util.*;

@Stateless
public class TaskCheckerTime {

    @Inject
    PersonalService personalService;

    @Schedule(minute = "5", hour = "*", persistent = false)
    public void checkTaskDeadline() {
        // 1) Chiedi a personalService la lista di entità Personal vere
        List<Personal> personalList = personalService.getAllPersonalEntities();

        for(Personal personal : personalList) {
            if(personal.getState() != TaskState.FREEZED && personal.getDeadline() != null && System.currentTimeMillis()>personal.getDeadline().atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()) {
                if (personal.getState() == TaskState.INPROGRESS) {
                    personalService.freezeTask(personal.getId());
                }
            }
        }
    }
}
