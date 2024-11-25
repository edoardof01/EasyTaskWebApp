package orm;
import domain.Profile;
import domain.Topic;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileMapper {


    public ProfileMapper() {}

    public ProfileDTO toProfileDTO(Profile profile) {
        if(profile == null) return null;
        return new ProfileDTO(profile);
    }

    public Profile toProfileEntity(ProfileDTO profileDTO) {
        if (profileDTO == null) return null;

        // Conversione della mappa String -> Topic
        Map<Topic, Integer> topics = profileDTO.getTopics().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Topic.valueOf(entry.getKey()), // Converti la stringa in enum
                        Map.Entry::getValue // Mantieni il valore intero
                ));

        return new Profile(
                profileDTO.getUsername(),
                profileDTO.getPassword(),
                topics, // Usa la mappa convertita
                profileDTO.getEmail(),
                profileDTO.isEmailVerified(),
                profileDTO.getVerificationToken()
        );
    }
    public void updateProfileFromDTO(Profile profile, ProfileDTO profileDTO) {
        if(profile == null || profileDTO == null) return;
        Map<Topic, Integer> topics = profileDTO.getTopics().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Topic.valueOf(entry.getKey()), // Converti la stringa in enum
                        Map.Entry::getValue // Mantieni il valore intero
                ));
        profile.setUsername(profileDTO.getUsername());
        profile.setPassword(profileDTO.getPassword());
        profile.setTopics(topics);
        profile.setEmail(profileDTO.getEmail());
        profile.setEmailVerified(profileDTO.isEmailVerified());
        profile.setVerificationToken(profileDTO.getVerificationToken());
    }

}
