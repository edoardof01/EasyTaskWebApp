package timerScheduler;
import domain.*;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import service.PersonalService;
import java.time.ZoneId;
import java.util.*;

@Stateless
public class SessionCheckerTime {

    @Inject
    PersonalService personalService;


    @Schedule(minute = "*", hour = "*", persistent = false)
    public void checkSessions() {
        List<Personal> personalList = personalService.getAllPersonalEntities();

        for (Personal personalEntity : personalList) {
            List<Session> sessionsOrdered = personalEntity.getSessions().stream()
                    .sorted(Comparator.comparing(Session::getStartDate))
                    .toList();
            for (int i = 1; i < sessionsOrdered.size(); i++) {
                Session currentSession = sessionsOrdered.get(i);
                Session previousSession = sessionsOrdered.get(i - 1);
                if (previousSession.getState() != SessionState.SKIPPED && System.currentTimeMillis() > currentSession.getStartDate()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()) {
                    // Ora personalEntity.getId() e previousSession.getId() non sono più null
                    personalService.handleLimitExceeded(previousSession.getId(), personalEntity.getId());
                }
            }
        }
    }
}
