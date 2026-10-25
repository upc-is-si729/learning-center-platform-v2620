package com.acme.center.platform.learning.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.CourseId;
import com.acme.center.platform.learning.domain.repositories.EnrollmentRepository;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.assemblers.EnrollmentPersistenceAssembler;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories.EnrollmentPersistenceRepository;
import com.acme.center.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the enrollment domain repository port with Spring Data JPA.
 *
 * <p>Dispatches domain events registered on the {@link Enrollment} aggregate root
 * via {@link DomainEventPublisher} after persistence.</p>
 */
@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {

    private final EnrollmentPersistenceRepository enrollmentPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public EnrollmentRepositoryImpl(
            EnrollmentPersistenceRepository enrollmentPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.enrollmentPersistenceRepository = enrollmentPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return enrollmentPersistenceRepository.findById(id).map(EnrollmentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Enrollment> findAll() {
        return enrollmentPersistenceRepository.findAll().stream()
                .map(EnrollmentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Enrollment> findAllByAcmeStudentRecordId(AcmeStudentRecordId studentRecordId) {
        return enrollmentPersistenceRepository.findAllByAcmeStudentRecordId(studentRecordId).stream()
                .map(EnrollmentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Enrollment> findAllByCourseId(Long courseId) {
        return enrollmentPersistenceRepository.findAllByCourseId(new CourseId(courseId)).stream()
                .map(EnrollmentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<Enrollment> findByAcmeStudentRecordIdAndCourseId(AcmeStudentRecordId studentRecordId, Long courseId) {
        return enrollmentPersistenceRepository.findByAcmeStudentRecordIdAndCourseId(studentRecordId, new CourseId(courseId))
                .map(EnrollmentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        var savedEntity = enrollmentPersistenceRepository.save(EnrollmentPersistenceAssembler.toPersistenceFromDomain(enrollment));
        var savedEnrollment = EnrollmentPersistenceAssembler.toDomainFromPersistence(savedEntity);
        domainEventPublisher.publishAndClearEvents(enrollment);
        return savedEnrollment;
    }
}

