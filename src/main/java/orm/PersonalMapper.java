package orm;
import orm.*;
import domain.*;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonalMapper {
    public PersonalDTO toPersonalDTO(Personal personal) {
        if(personal == null) {
            return null;
        }
        return new PersonalDTO(personal);
    }
    public Personal toPersonalEntity(PersonalDTO personalDTO) {
        if(personalDTO == null) {
            return null;
        }
        return new Personal(
                personalDTO.getName(),
                personalDTO.getTopic(),
                personalDTO.getTaskState(),
                personalDTO.getDeadline(),
                personalDTO.getDescription(),
                personalDTO.getPercentageOfCompletion(),
                personalDTO.getComplexity(),
                personalDTO.getPriority(),
                personalDTO.getTimetable(),
                personalDTO.getTotalTime(),
                personalDTO.getStrategies(),
                personalDTO.getResources()
        );
    }
    public void updateSharedFromDTO(PersonalDTO personalDTO, Personal personal) {
        if (personalDTO == null || personal == null) {
            return;
        }
        // Aggiornamento delle proprietà principali
        personal.setName(personalDTO.getName());
        personal.setDeadline(personalDTO.getDeadline());
        personal.setDescription(personalDTO.getDescription());
        personal.setPercentageOfCompletion(personalDTO.getPercentageOfCompletion());
        personal.setComplexity(personalDTO.getComplexity());
        personal.setPriority(personalDTO.getPriority());
        personal.setTimetable(personalDTO.getTimetable());
        personal.setTotalTime(personalDTO.getTotalTime());

        // Aggiornamento dello stato e delle strategie
        personal.setState(personalDTO.getTaskState());
        personal.setStrategies(personalDTO.getStrategies());
        personal.setResources(personalDTO.getResources());
    }


}
