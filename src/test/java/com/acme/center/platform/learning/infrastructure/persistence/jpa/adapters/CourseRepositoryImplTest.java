package com.acme.center.platform.learning.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.CoursePersistenceEntity;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories.CoursePersistenceRepository;
import com.acme.center.platform.shared.infrastructure.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseRepositoryImplTest {

    @Mock
    private CoursePersistenceRepository coursePersistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CourseRepositoryImpl courseRepository;

    @BeforeEach
    void setUp() {
        var domainEventPublisher = new DomainEventPublisher(eventPublisher);
        courseRepository = new CourseRepositoryImpl(coursePersistenceRepository, domainEventPublisher);
    }

    @Test
    void saveShouldPersistCourseAndPublishDomainEvents() {
        // Arrange
        var course = new Course("Java 27 & DDD", "Course Description");

        var persistenceEntity = new CoursePersistenceEntity();
        persistenceEntity.setId(100L);
        persistenceEntity.setTitle("Java 27 & DDD");
        persistenceEntity.setDescription("Course Description");

        when(coursePersistenceRepository.save(any(CoursePersistenceEntity.class)))
                .thenReturn(persistenceEntity);

        // Act
        var saved = courseRepository.save(course);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getTitle()).isEqualTo("Java 27 & DDD");
        assertThat(course.domainEvents()).isEmpty();
    }

    @Test
    void findByIdShouldReturnCourseWhenFound() {
        // Arrange
        var persistenceEntity = new CoursePersistenceEntity();
        persistenceEntity.setId(100L);
        persistenceEntity.setTitle("Java 27 & DDD");
        persistenceEntity.setDescription("Course Description");

        when(coursePersistenceRepository.findById(100L)).thenReturn(Optional.of(persistenceEntity));

        // Act
        var found = courseRepository.findById(100L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(100L);
        assertThat(found.get().getTitle()).isEqualTo("Java 27 & DDD");
    }
}
