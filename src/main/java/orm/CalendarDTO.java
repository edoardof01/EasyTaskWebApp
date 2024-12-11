package orm;

import domain.Calendar;
import domain.User;

public class CalendarDTO {
    private  long id;
    private  User user;

    public CalendarDTO() {}

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
