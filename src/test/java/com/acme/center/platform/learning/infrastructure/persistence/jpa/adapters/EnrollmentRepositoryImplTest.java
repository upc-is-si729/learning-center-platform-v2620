package com.acme.center.platform.learning.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import com.acme.center.platform.learning.domain.model.events.TutorialCompletedEvent;
import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.TutorialId;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.CoursePersistenceEntity;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.EnrollmentPersistenceEntity;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories.EnrollmentPersistenceRepository;
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
class EnrollmentRepositoryImplTest {

    @Mock
    private EnrollmentPersistenceRepository enrollmentPersistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EnrollmentRepositoryImpl enrollmentRepository;

    @BeforeEach
    void setUp() {
        enrollmentRepository = new EnrollmentRepositoryImpl(enrollmentPersistenceRepository, eventPublisher);
    }

    @Test
    void saveShouldPublishDomainEventsAndClearThem() {
        // Arrange
        var course = new Course();
        course.setId(100L);
        course.setTitle("Spring Boot DDD");
        course.setDescription("DDD Course");
        var tutorialId = new TutorialId(200L);
        course.addTutorialToLearningPath(tutorialId);

        var studentRecordId = new AcmeStudentRecordId("123e4567-e89b-12d3-a456-426614174000");
        var enrollment = Enrollment.reconstitute(10L, studentRecordId, course, null, null);
        enrollment.confirm();

        // Trigger domain event registration
        enrollment.completeTutorial(tutorialId);
        assertThat(enrollment.domainEvents()).hasSize(1);

        var courseEntity = new CoursePersistenceEntity();
        courseEntity.setId(100L);
        courseEntity.setTitle("Spring Boot DDD");
        courseEntity.setDescription("DDD Course");

        var persistenceEntity = new EnrollmentPersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setAcmeStudentRecordId(studentRecordId);
        persistenceEntity.setCourse(courseEntity);
        persistenceEntity.setStatus(enrollment.getStatus());

        when(enrollmentPersistenceRepository.save(any(EnrollmentPersistenceEntity.class)))
                .thenReturn(persistenceEntity);

        // Act
        var saved = enrollmentRepository.save(enrollment);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10L);

        var eventCaptor = ArgumentCaptor.forClass(TutorialCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        var publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getEnrollmentId()).isEqualTo(10L);
        assertThat(publishedEvent.getTutorialId()).isEqualTo(tutorialId);

        // Events on the aggregate should now be cleared
        assertThat(enrollment.domainEvents()).isEmpty();
    }

    @Test
    void findByIdShouldDelegateToPersistenceRepository() {
        // Arrange
        var courseEntity = new CoursePersistenceEntity();
        courseEntity.setId(100L);
        courseEntity.setTitle("Course 1");
        courseEntity.setDescription("Description 1");

        var studentRecordId = new AcmeStudentRecordId("123e4567-e89b-12d3-a456-426614174000");
        var persistenceEntity = new EnrollmentPersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setAcmeStudentRecordId(studentRecordId);
        persistenceEntity.setCourse(courseEntity);

        when(enrollmentPersistenceRepository.findById(10L)).thenReturn(Optional.of(persistenceEntity));

        // Act
        var result = enrollmentRepository.findById(10L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getAcmeStudentRecordId()).isEqualTo(studentRecordId);
    }
}
