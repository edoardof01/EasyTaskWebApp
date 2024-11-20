package orm;

import domain.Profile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class ProfileMapper {

    @Inject
    EntityManager em;

    public ProfileMapper() {}

    public ProfileDTO toUserDTO(Profile profile) {
        if(profile == null) return null;
        return new ProfileDTO(profile);
    }
    public Profile toProfileEntity(ProfileDTO profileDTO) {
        if(profileDTO == null) return null;
        return new Profile(
                profileDTO.getUsername(),
                profileDTO.getPassword(),
                profileDTO.getTopics(),
                profileDTO.getEmail(),
                profileDTO.isEmailVerified(),
                profileDTO.getVerificationToken()
        );
    }
    public void updateProfileFromDTO(Profile profile, ProfileDTO profileDTO) {
        if(profile == null || profileDTO == null) return;
        profile.setUsername(profileDTO.getUsername());
        profile.setPassword(profileDTO.getPassword());
        profile.setTopics(profileDTO.getTopics());
        profile.setEmail(profileDTO.getEmail());
        profile.setEmailVerified(profileDTO.isEmailVerified());
        profile.setVerificationToken(profileDTO.getVerificationToken());
    }

}
