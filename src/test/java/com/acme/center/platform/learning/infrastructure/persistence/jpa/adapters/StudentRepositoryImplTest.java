package com.acme.center.platform.learning.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.learning.domain.model.aggregates.Student;
import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.ProfileId;
import com.acme.center.platform.learning.domain.model.valueobjects.StudentPerformanceMetricSet;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.StudentPersistenceEntity;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories.StudentPersistenceRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentRepositoryImplTest {

    @Mock
    private StudentPersistenceRepository studentPersistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StudentRepositoryImpl studentRepository;

    @BeforeEach
    void setUp() {
        var domainEventPublisher = new DomainEventPublisher(eventPublisher);
        studentRepository = new StudentRepositoryImpl(studentPersistenceRepository, domainEventPublisher);
    }

    @Test
    void saveShouldPersistStudentAndPublishDomainEvents() {
        // Arrange
        var profileId = new ProfileId(1L);
        var student = new Student(profileId);
        var recordId = student.getAcmeStudentRecordId();

        var persistenceEntity = new StudentPersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setAcmeStudentRecordId(recordId);
        persistenceEntity.setProfileId(profileId);
        persistenceEntity.setPerformanceMetricSet(new StudentPerformanceMetricSet());

        when(studentPersistenceRepository.save(any(StudentPersistenceEntity.class)))
                .thenReturn(persistenceEntity);

        // Act
        var saved = studentRepository.save(student);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getAcmeStudentRecordId()).isEqualTo(recordId);
        assertThat(saved.getProfileId()).isEqualTo(profileId);
        assertThat(student.domainEvents()).isEmpty();
    }

    @Test
    void findByAcmeStudentRecordIdShouldReturnStudentWhenFound() {
        // Arrange
        var profileId = new ProfileId(1L);
        var recordId = new AcmeStudentRecordId("123e4567-e89b-12d3-a456-426614174000");

        var persistenceEntity = new StudentPersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setAcmeStudentRecordId(recordId);
        persistenceEntity.setProfileId(profileId);
        persistenceEntity.setPerformanceMetricSet(new StudentPerformanceMetricSet());

        when(studentPersistenceRepository.findByAcmeStudentRecordId(recordId))
                .thenReturn(Optional.of(persistenceEntity));

        // Act
        var found = studentRepository.findByAcmeStudentRecordId(recordId);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(10L);
        assertThat(found.get().getAcmeStudentRecordId()).isEqualTo(recordId);
    }
}
