package orm;


import domain.*;

import java.time.LocalDateTime;

public class SessionDTO {

    private long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SessionState state;

    public SessionDTO() {
    }
    public SessionDTO(Session session) {
        this.startDate = session.getStartDate();
        this.endDate = session.getEndDate();
        this.state = SessionState.PROGRAMMED;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public SessionState getState() {
        return state;
    }
    public void setState(SessionState state) {
        this.state = state;
    }


}
