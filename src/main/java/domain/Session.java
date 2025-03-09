package domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;

import java.time.Duration;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(startDate, session.startDate) &&
                Objects.equals(endDate, session.endDate);

    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    public boolean overlaps(Session other) {
        return (this.startDate.isBefore(other.endDate) && this.endDate.isAfter(other.startDate));
    }

    public int getDurationMinutes() {
        if (startDate != null && endDate != null) {
            return (int) Duration.between(startDate, endDate).toMinutes(); // Restituisce la durata in minuti
        } else {
            throw new IllegalStateException("Start date or end date is null");
        }
    }

}
