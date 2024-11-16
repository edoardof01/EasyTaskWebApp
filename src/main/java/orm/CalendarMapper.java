package orm;

import domain.Calendar;

public class CalendarMapper {

    public CalendarDTO toCalendarDTO(Calendar calendar) {
        if (calendar == null) return null;
        return new CalendarDTO(calendar);
    }
    public Calendar toCalendarEntity(CalendarDTO calendarDTO) {
        if (calendarDTO == null) return null;
        return new Calendar(
                calendarDTO.getUser()
        );
    }
}
