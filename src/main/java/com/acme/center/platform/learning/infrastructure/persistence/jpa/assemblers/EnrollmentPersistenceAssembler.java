package com.acme.center.platform.learning.infrastructure.persistence.jpa.assemblers;

import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import com.acme.center.platform.learning.domain.model.entities.ProgressRecordItem;
import com.acme.center.platform.learning.domain.model.valueobjects.ProgressRecord;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.EnrollmentPersistenceEntity;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.ProgressRecordItemPersistenceEntity;

import java.util.ArrayList;

/**
 * Static assembler between enrollment domain and persistence representations.
 */
public final class EnrollmentPersistenceAssembler {

    private EnrollmentPersistenceAssembler() {
    }

    public static Enrollment toDomainFromPersistence(EnrollmentPersistenceEntity entity) {
        if (entity == null) return null;

        var course = CoursePersistenceAssembler.toDomainFromPersistence(entity.getCourse());

        var progressRecord = new ProgressRecord();
        var progressItems = new ArrayList<ProgressRecordItem>();

        var enrollment = Enrollment.reconstitute(
                entity.getId(),
                entity.getAcmeStudentRecordId(),
                course,
                entity.getStatus(),
                progressRecord);

        if (entity.getProgressRecordItems() != null) {
            for (var item : entity.getProgressRecordItems()) {
                var progressRecordItem = ProgressRecordItem.reconstitute(
                        item.getId(),
                        enrollment,
                        item.getTutorialId(),
                        item.getStatus(),
                        item.getStatedAt(),
                        item.getCompletedAt());
                progressItems.add(progressRecordItem);
            }
        }
        progressRecord.setProgressRecordItems(progressItems);

        return enrollment;
    }

    public static EnrollmentPersistenceEntity toPersistenceFromDomain(Enrollment enrollment) {
        if (enrollment == null) return null;

        var entity = new EnrollmentPersistenceEntity();
        // Only set ID if the enrollment is being updated (has a non-null ID)
        // For new enrollments, leave ID null to allow JPA to generate it
        if (enrollment.getId() != null) {
            entity.setId(enrollment.getId());
        }
        entity.setAcmeStudentRecordId(enrollment.getAcmeStudentRecordId());
        entity.setCourse(CoursePersistenceAssembler.toPersistenceFromDomain(enrollment.getCourse()));
        entity.setStatus(enrollment.getStatus());

        var progressItems = new ArrayList<ProgressRecordItemPersistenceEntity>();
        for (var item : enrollment.getProgressRecord().getProgressRecordItems()) {
            var persistenceItem = new ProgressRecordItemPersistenceEntity();
            // Only set ID if the item is being updated (has a non-null ID)
            // For new items, leave ID null to allow JPA to generate it
            if (item.getId() != null) {
                persistenceItem.setId(item.getId());
            }
            persistenceItem.setEnrollment(entity);
            persistenceItem.setTutorialId(item.getTutorialId());
            persistenceItem.setStatus(item.getStatus());
            persistenceItem.setStatedAt(item.getStatedAt());
            persistenceItem.setCompletedAt(item.getCompletedAt());
            progressItems.add(persistenceItem);
        }
        entity.setProgressRecordItems(progressItems);

        return entity;
    }
}

