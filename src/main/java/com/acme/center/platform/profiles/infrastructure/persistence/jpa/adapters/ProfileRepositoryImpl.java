package com.acme.center.platform.profiles.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.profiles.domain.model.aggregates.Profile;
import com.acme.center.platform.profiles.domain.model.events.ProfileCreatedEvent;
import com.acme.center.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.acme.center.platform.profiles.domain.repositories.ProfileRepository;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.assemblers.ProfilePersistenceAssembler;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.repositories.ProfilePersistenceRepository;
import com.acme.center.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the domain profile repository port with Spring Data JPA.
 *
 * <p>Also acts as the event-publishing boundary: after a brand-new {@link Profile} is
 * persisted (and the JPA-assigned id is therefore available), a {@link ProfileCreatedEvent}
 * is dispatched via {@link DomainEventPublisher}.</p>
 */
@Repository
public class ProfileRepositoryImpl implements ProfileRepository {

    private final ProfilePersistenceRepository profilePersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ProfileRepositoryImpl(
            ProfilePersistenceRepository profilePersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.profilePersistenceRepository = profilePersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Optional<Profile> findById(Long id) {
        return profilePersistenceRepository.findById(id).map(ProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Profile> findByEmailAddress(EmailAddress emailAddress) {
        return profilePersistenceRepository.findByEmailAddress(emailAddress).map(ProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Profile> findAll() {
        return profilePersistenceRepository.findAll().stream().map(ProfilePersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public Profile save(Profile profile) {
        boolean isNew = profile.getId() == null;
        var savedEntity = profilePersistenceRepository.save(ProfilePersistenceAssembler.toPersistenceFromDomain(profile));
        var savedProfile = ProfilePersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedProfile.onCreated();
            domainEventPublisher.publishAndClearEvents(savedProfile);
        }
        return savedProfile;
    }

    @Override
    public boolean existsByEmailAddress(EmailAddress emailAddress) {
        return profilePersistenceRepository.countByEmailAddress(emailAddress) > 0;
    }
}




