package orm;

import domain.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;

@ApplicationScoped
public class SharedMapper {
    public SharedDTO toSharedDTO(Shared shared) {
        if (shared == null) {
            return null;
        }
        return new SharedDTO(shared);
    }

    public Shared toSharedEntity(SharedDTO sharedDTO) {
        if (sharedDTO == null) {
            return null;
        }
        return new Shared(
                sharedDTO.getName(),
                sharedDTO.getTopic(),
                sharedDTO.getTaskState(),
                sharedDTO.getDeadline(),
                sharedDTO.getDescription(),
                sharedDTO.getPercentageOfCompletion(),
                sharedDTO.getComplexity(),
                sharedDTO.getPriority(),
                sharedDTO.getTimetable(),
                sharedDTO.getTotalTime(),
                sharedDTO.getStrategies(),
                sharedDTO.getResources()
        );
    }
    public void updateSharedFromDTO(SharedDTO sharedDTO, Shared shared) {
        if (sharedDTO == null || shared == null) {
            return;
        }

        // Aggiornamento delle proprietà principali
        shared.setName(sharedDTO.getName());
        shared.setDeadline(sharedDTO.getDeadline());
        shared.setDescription(sharedDTO.getDescription());
        shared.setPercentageOfCompletion(sharedDTO.getPercentageOfCompletion());
        shared.setComplexity(sharedDTO.getComplexity());
        shared.setPriority(sharedDTO.getPriority());
        shared.setTimetable(sharedDTO.getTimetable());
        shared.setTotalTime(sharedDTO.getTotalTime());

        // Aggiornamento dello stato e delle strategie
        shared.setState(sharedDTO.getTaskState());
        shared.setStrategies(sharedDTO.getStrategies());
        shared.setResources(sharedDTO.getResources());

        // Se c'è una proprietà che rappresenta la guida dell'utente, aggiornarla (facoltativo)
        if (sharedDTO.getUserGuidance() != null) {
            shared.updateUserGuidance(sharedDTO.getUserGuidance());
        }
    }
}


