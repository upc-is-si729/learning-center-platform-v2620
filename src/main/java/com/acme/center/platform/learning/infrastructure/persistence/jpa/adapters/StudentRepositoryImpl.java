package com.acme.center.platform.learning.infrastructure.persistence.jpa.adapters;

import com.acme.center.platform.learning.domain.model.aggregates.Student;
import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.ProfileId;
import com.acme.center.platform.learning.domain.repositories.StudentRepository;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.assemblers.StudentPersistenceAssembler;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories.StudentPersistenceRepository;
import com.acme.center.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository adapter that bridges the student domain repository port with Spring Data JPA.
 */
@Repository
public class StudentRepositoryImpl implements StudentRepository {

    private final StudentPersistenceRepository studentPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public StudentRepositoryImpl(
            StudentPersistenceRepository studentPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.studentPersistenceRepository = studentPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Optional<Student> findById(Long id) {
        return studentPersistenceRepository.findById(id).map(StudentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Student> findByAcmeStudentRecordId(AcmeStudentRecordId studentRecordId) {
        return studentPersistenceRepository.findByAcmeStudentRecordId(studentRecordId)
                .map(StudentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Student> findByProfileId(ProfileId profileId) {
        return studentPersistenceRepository.findByProfileId(profileId).map(StudentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Student save(Student student) {
        var saved = studentPersistenceRepository.save(StudentPersistenceAssembler.toPersistenceFromDomain(student));
        var savedStudent = StudentPersistenceAssembler.toDomainFromPersistence(saved);
        domainEventPublisher.publishAndClearEvents(student);
        return savedStudent;
    }

    @Override
    public boolean existsByAcmeStudentRecordId(AcmeStudentRecordId studentRecordId) {
        return studentPersistenceRepository.existsByAcmeStudentRecordId(studentRecordId);
    }
}

