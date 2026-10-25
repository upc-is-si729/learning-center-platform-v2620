package com.acme.center.platform.profiles.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.profiles.domain.model.aggregates.Profile;
import com.acme.center.platform.profiles.domain.model.events.ProfileCreatedEvent;
import com.acme.center.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.acme.center.platform.profiles.domain.model.valueobjects.PersonName;
import com.acme.center.platform.profiles.domain.model.valueobjects.StreetAddress;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.embeddables.PersonNamePersistenceEmbeddable;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.embeddables.StreetAddressPersistenceEmbeddable;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import com.acme.center.platform.profiles.infrastructure.persistence.jpa.repositories.ProfilePersistenceRepository;
import com.acme.center.platform.shared.infrastructure.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileRepositoryImplTest {

    @Mock
    private ProfilePersistenceRepository profilePersistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ProfileRepositoryImpl profileRepository;

    @BeforeEach
    void setUp() {
        var domainEventPublisher = new DomainEventPublisher(eventPublisher);
        profileRepository = new ProfileRepositoryImpl(profilePersistenceRepository, domainEventPublisher);
    }

    @Test
    void saveShouldPublishProfileCreatedEventWhenNewProfileIsCreated() {
        // Arrange
        var profile = new Profile("Jane", "Doe", "jane.doe@example.com", "Main St", "123", "Springfield", "12345", "USA");

        var persistenceEntity = new ProfilePersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setName(new PersonNamePersistenceEmbeddable("Jane", "Doe"));
        persistenceEntity.setEmailAddress(new EmailAddress("jane.doe@example.com"));
        persistenceEntity.setStreetAddress(new StreetAddressPersistenceEmbeddable("Main St", "123", "Springfield", "12345", "USA"));

        when(profilePersistenceRepository.save(any(ProfilePersistenceEntity.class)))
                .thenReturn(persistenceEntity);

        // Act
        var saved = profileRepository.save(profile);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10L);

        var captor = ArgumentCaptor.forClass(ProfileCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().profileId()).isEqualTo(10L);
        assertThat(captor.getValue().email()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void saveShouldNotPublishProfileCreatedEventWhenUpdatingExistingProfile() {
        // Arrange
        var profile = Profile.reconstitute(
                10L,
                new PersonName("Jane", "Doe"),
                new EmailAddress("jane.doe@example.com"),
                new StreetAddress("Main St", "123", "Springfield", "12345", "USA")
        );

        var persistenceEntity = new ProfilePersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setName(new PersonNamePersistenceEmbeddable("Jane", "Smith"));
        persistenceEntity.setEmailAddress(new EmailAddress("jane.doe@example.com"));
        persistenceEntity.setStreetAddress(new StreetAddressPersistenceEmbeddable("Main St", "123", "Springfield", "12345", "USA"));

        when(profilePersistenceRepository.save(any(ProfilePersistenceEntity.class)))
                .thenReturn(persistenceEntity);

        // Act
        var saved = profileRepository.save(profile);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10L);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void findByEmailAddressShouldReturnProfileWhenFound() {
        // Arrange
        var email = new EmailAddress("jane.doe@example.com");
        var persistenceEntity = new ProfilePersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setName(new PersonNamePersistenceEmbeddable("Jane", "Doe"));
        persistenceEntity.setEmailAddress(email);
        persistenceEntity.setStreetAddress(new StreetAddressPersistenceEmbeddable("Main St", "123", "Springfield", "12345", "USA"));

        when(profilePersistenceRepository.findByEmailAddress(email)).thenReturn(Optional.of(persistenceEntity));

        // Act
        var found = profileRepository.findByEmailAddress(email);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(10L);
        assertThat(found.get().getEmailAddress()).isEqualTo("jane.doe@example.com");
    }
}
