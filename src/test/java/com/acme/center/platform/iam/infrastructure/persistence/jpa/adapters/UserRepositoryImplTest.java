package com.acme.center.platform.iam.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.iam.domain.model.aggregates.User;
import com.acme.center.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.acme.center.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import com.acme.center.platform.shared.infrastructure.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private UserPersistenceRepository userPersistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() {
        var domainEventPublisher = new DomainEventPublisher(eventPublisher);
        userRepository = new UserRepositoryImpl(userPersistenceRepository, domainEventPublisher);
    }

    @Test
    void saveShouldPersistUserAndPublishDomainEvents() {
        // Arrange
        var user = new User("admin", "password123");

        var persistenceEntity = new UserPersistenceEntity();
        persistenceEntity.setId(1L);
        persistenceEntity.setUsername("admin");
        persistenceEntity.setPassword("password123");
        persistenceEntity.setRoles(Collections.emptySet());

        when(userPersistenceRepository.save(any(UserPersistenceEntity.class)))
                .thenReturn(persistenceEntity);

        // Act
        var saved = userRepository.save(user);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(user.domainEvents()).isEmpty();
    }

    @Test
    void findByUsernameShouldReturnUserWhenFound() {
        // Arrange
        var persistenceEntity = new UserPersistenceEntity();
        persistenceEntity.setId(1L);
        persistenceEntity.setUsername("admin");
        persistenceEntity.setPassword("password123");
        persistenceEntity.setRoles(Collections.emptySet());

        when(userPersistenceRepository.findByUsername("admin")).thenReturn(Optional.of(persistenceEntity));

        // Act
        var found = userRepository.findByUsername("admin");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(1L);
        assertThat(found.get().getUsername()).isEqualTo("admin");
    }
}
