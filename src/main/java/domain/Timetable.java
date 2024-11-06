package domain;

import java.time.LocalTime;

public enum Timetable {
    MORNING("06-12"),
    AFTERNOON("12-18"),
    EVENING("18-24"),
    NIGHT("24-06");

    Timetable(String s) {
    }
}
