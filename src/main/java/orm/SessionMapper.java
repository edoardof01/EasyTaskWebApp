package orm;

import domain.Session;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionMapper {
    public SessionDTO toSessionDTO(Session session) {
        if (session == null) return null;
        return new SessionDTO(session);
    }
    public Session toSessionEntity(SessionDTO sessionDTO) {
        if (sessionDTO == null) return null;
        return new Session(
                sessionDTO.getStartDate(), sessionDTO.getEndDate()
        );
    }

    public void updateSessionFromDTO(Session session,SessionDTO sessionDTO) {
        if(session == null || sessionDTO == null) throw new NullPointerException();
        session.setStartDate(sessionDTO.getStartDate());
        session.setEndDate(sessionDTO.getEndDate());
        }


}
