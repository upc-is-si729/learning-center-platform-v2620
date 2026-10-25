package com.acme.center.platform.learning.infrastructure.persistence.jpa.assemblers;

import com.acme.center.platform.learning.domain.model.aggregates.Student;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.StudentPersistenceEntity;

/**
 * Static assembler between student domain and persistence representations.
 */
public final class StudentPersistenceAssembler {

    private StudentPersistenceAssembler() {
    }

    public static Student toDomainFromPersistence(StudentPersistenceEntity entity) {
        if (entity == null) return null;
        return Student.reconstitute(
                entity.getId(),
                entity.getAcmeStudentRecordId(),
                entity.getProfileId(),
                entity.getPerformanceMetricSet());
    }

    public static StudentPersistenceEntity toPersistenceFromDomain(Student student) {
        if (student == null) return null;
        var entity = new StudentPersistenceEntity();
        // Only set ID if the student is being updated (has a non-null ID)
        // For new students, leave ID null to allow JPA to generate it
        if (student.getId() != null) {
            entity.setId(student.getId());
        }
        entity.setAcmeStudentRecordId(student.getAcmeStudentRecordId());
        entity.setProfileId(student.getProfileId());
        entity.setPerformanceMetricSet(student.getPerformanceMetricSet());
        return entity;
    }
}

