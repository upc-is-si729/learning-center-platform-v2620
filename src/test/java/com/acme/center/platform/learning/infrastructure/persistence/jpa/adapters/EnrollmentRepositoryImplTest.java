package com.acme.center.platform.learning.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import com.acme.center.platform.learning.domain.model.events.TutorialCompletedEvent;
import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.CourseId;
import com.acme.center.platform.learning.domain.model.valueobjects.LearningPath;
import com.acme.center.platform.learning.domain.model.valueobjects.TutorialId;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.EnrollmentPersistenceEntity;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories.EnrollmentPersistenceRepository;
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
class EnrollmentRepositoryImplTest {

    @Mock
    private EnrollmentPersistenceRepository enrollmentPersistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EnrollmentRepositoryImpl enrollmentRepository;

    @BeforeEach
    void setUp() {
        var domainEventPublisher = new DomainEventPublisher(eventPublisher);
        enrollmentRepository = new EnrollmentRepositoryImpl(enrollmentPersistenceRepository, domainEventPublisher);
    }

    @Test
    void saveShouldPublishDomainEventsAndClearThem() {
        // Arrange
        var course = Course.reconstitute(100L, "Spring Boot DDD", "DDD Course", new LearningPath());
        var tutorialId = new TutorialId(200L);
        course.addTutorialToLearningPath(tutorialId);
        var learningPath = course.getLearningPath();

        var courseId = new CourseId(100L);
        var studentRecordId = new AcmeStudentRecordId("123e4567-e89b-12d3-a456-426614174000");
        var enrollment = Enrollment.reconstitute(10L, studentRecordId, courseId, null, null);
        enrollment.confirm(learningPath);

        // Trigger domain event registration
        enrollment.completeTutorial(tutorialId, learningPath);
        assertThat(enrollment.domainEvents()).hasSize(1);

        var persistenceEntity = new EnrollmentPersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setAcmeStudentRecordId(studentRecordId);
        persistenceEntity.setCourseId(courseId);
        persistenceEntity.setStatus(enrollment.getStatus());

        when(enrollmentPersistenceRepository.save(any(EnrollmentPersistenceEntity.class)))
                .thenReturn(persistenceEntity);

        // Act
        var saved = enrollmentRepository.save(enrollment);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getCourseId()).isEqualTo(courseId);

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
        var courseId = new CourseId(100L);
        var studentRecordId = new AcmeStudentRecordId("123e4567-e89b-12d3-a456-426614174000");
        var persistenceEntity = new EnrollmentPersistenceEntity();
        persistenceEntity.setId(10L);
        persistenceEntity.setAcmeStudentRecordId(studentRecordId);
        persistenceEntity.setCourseId(courseId);

        when(enrollmentPersistenceRepository.findById(10L)).thenReturn(Optional.of(persistenceEntity));

        // Act
        var result = enrollmentRepository.findById(10L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getAcmeStudentRecordId()).isEqualTo(studentRecordId);
        assertThat(result.get().getCourseId()).isEqualTo(courseId);
    }
}
