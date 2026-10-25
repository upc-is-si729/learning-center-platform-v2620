package com.acme.center.platform.learning.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.repositories.CourseRepository;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.assemblers.CoursePersistenceAssembler;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories.CoursePersistenceRepository;
import com.acme.center.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the course domain repository port with Spring Data JPA.
 */
@Repository
public class CourseRepositoryImpl implements CourseRepository {

    private final CoursePersistenceRepository coursePersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CourseRepositoryImpl(
            CoursePersistenceRepository coursePersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.coursePersistenceRepository = coursePersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Optional<Course> findById(Long id) {
        return coursePersistenceRepository.findById(id).map(CoursePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Course> findAll() {
        return coursePersistenceRepository.findAll().stream().map(CoursePersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public Optional<Course> findByTitle(String title) {
        return coursePersistenceRepository.findByTitle(title).map(CoursePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Course save(Course course) {
        var saved = coursePersistenceRepository.save(CoursePersistenceAssembler.toPersistenceFromDomain(course));
        var savedCourse = CoursePersistenceAssembler.toDomainFromPersistence(saved);
        domainEventPublisher.publishAndClearEvents(course);
        return savedCourse;
    }

    @Override
    public boolean existsById(Long id) {
        return coursePersistenceRepository.existsById(id);
    }

    @Override
    public boolean existsByTitle(String title) {
        return coursePersistenceRepository.existsByTitle(title);
    }

    @Override
    public boolean existsByTitleAndIdIsNot(String title, Long id) {
        return coursePersistenceRepository.existsByTitleAndIdIsNot(title, id);
    }

    @Override
    public void deleteById(Long id) {
        coursePersistenceRepository.deleteById(id);
    }
}

