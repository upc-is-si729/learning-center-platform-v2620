package com.acme.center.platform.learning.application.internal.outboundservices.acl;

import com.acme.center.platform.learning.domain.model.valueobjects.ProfileId;
import com.acme.center.platform.profiles.interfaces.acl.ProfilesContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ACL service used by the `Learning` bounded context to interact with Profiles capabilities.
 */
@Service
public class ExternalProfileService {
    private final ProfilesContextFacade profilesContextFacade;

    /**
     * Creates the service with the Profiles ACL facade dependency.
     *
     * @param profilesContextFacade profiles bounded-context facade
     */
    public ExternalProfileService(ProfilesContextFacade profilesContextFacade) {
        this.profilesContextFacade = profilesContextFacade;
    }

    /**
     * Fetches a profile identifier by email.
     *
     * @param email profile email address
     * @return optional profile identifier
     */
    public Optional<ProfileId> fetchProfileByEmail(String email) {
        var profileId = profilesContextFacade.fetchProfileIdByEmail(email);
        return profileId == 0L ? Optional.empty() : Optional.of(new ProfileId(profileId));
    }

    /**
     * Creates a profile through the Profiles bounded context.
     *
     * @return optional created profile identifier
     */
    public Optional<ProfileId> createProfile(
            String firstName,
            String lastName,
            String email,
            String street,
            String number,
            String city,
            String postalCode,
            String country) {
        var profileId = profilesContextFacade.createProfile(firstName, lastName, email, street, number, city, postalCode, country);
        return profileId == 0L ? Optional.empty() : Optional.of(new ProfileId(profileId));
    }
}
