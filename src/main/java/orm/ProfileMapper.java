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
        return new Profile(
                profileDTO.getUsername(),
                profileDTO.getTopics()
        );
    }
    public void updateProfileFromDTO(Profile profile, ProfileDTO profileDTO) {
        if(profile == null || profileDTO == null) return;
        profile.setUsername(profileDTO.getUsername());
        profile.setTopics(profileDTO.getTopics());

    }

}
