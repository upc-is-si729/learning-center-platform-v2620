package com.acme.center.platform.profiles.infrastructure.persistence.jpa.assemblers;

import com.acme.center.platform.profiles.domain.model.aggregates.Profile;
import com.acme.center.platform.profiles.domain.model.valueobjects.PersonName;
import com.acme.center.platform.profiles.domain.model.valueobjects.StreetAddress;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.embeddables.PersonNamePersistenceEmbeddable;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.embeddables.StreetAddressPersistenceEmbeddable;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;

/**
 * Static assembler between profile domain and persistence representations.
 */
public final class ProfilePersistenceAssembler {

    private ProfilePersistenceAssembler() {
    }

    public static Profile toDomainFromPersistence(ProfilePersistenceEntity entity) {
        if (entity == null) return null;
        return Profile.reconstitute(
                entity.getId(),
                toDomainFromPersistence(entity.getName()),
                entity.getEmailAddress(),
                toDomainFromPersistence(entity.getStreetAddress()));
    }

    public static ProfilePersistenceEntity toPersistenceFromDomain(Profile profile) {
        var entity = new ProfilePersistenceEntity();
        entity.setId(profile.getId());
        entity.setName(toPersistenceFromDomain(profile.getName()));
        entity.setEmailAddress(profile.getEmailAddressValue());
        entity.setStreetAddress(toPersistenceFromDomain(profile.getStreetAddressValue()));
        return entity;
    }

    private static PersonName toDomainFromPersistence(PersonNamePersistenceEmbeddable value) {
        return value == null ? null : new PersonName(value.getFirstName(), value.getLastName());
    }

    private static StreetAddress toDomainFromPersistence(StreetAddressPersistenceEmbeddable value) {
        return value == null ? null : new StreetAddress(value.getStreet(), value.getNumber(), value.getCity(), value.getPostalCode(), value.getCountry());
    }

    private static PersonNamePersistenceEmbeddable toPersistenceFromDomain(PersonName value) {
        return value == null ? null : new PersonNamePersistenceEmbeddable(value.firstName(), value.lastName());
    }

    private static StreetAddressPersistenceEmbeddable toPersistenceFromDomain(StreetAddress value) {
        return value == null ? null : new StreetAddressPersistenceEmbeddable(value.street(), value.number(), value.city(), value.postalCode(), value.country());
    }
}

