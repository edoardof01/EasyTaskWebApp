package orm;

import domain.Calendar;
import domain.User;

public class CalendarDTO {
    private final long id;
    private final User user;
    public CalendarDTO(Calendar calendar) {
        this.id = calendar.getId();
        this.user = calendar.getUser();
    }
    public long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }

}
