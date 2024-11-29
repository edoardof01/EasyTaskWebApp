package domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private SessionState state;

    public Session() {}

    public Session(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = SessionState.PROGRAMMED;
    }

    public Long getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(startDate, session.startDate) &&
                Objects.equals(endDate, session.endDate) &&
                state == session.state;  //EVENTUALMENTE DA RIAGGIUNGERE (PROBLEMI CON COMPLETEBYSESSIONS)
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate, state);
    }

    public boolean overlaps(Session other) {
        // Verifica se le sessioni si sovrappongono
        return (this.startDate.isBefore(other.endDate) && this.endDate.isAfter(other.startDate));
    }


}
