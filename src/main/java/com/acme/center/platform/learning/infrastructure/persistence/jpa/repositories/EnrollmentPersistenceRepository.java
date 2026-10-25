package com.acme.center.platform.learning.infrastructure.persistence.jpa.repositories;

import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.CourseId;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.EnrollmentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for enrollment persistence entities.
 */
@Repository
public interface EnrollmentPersistenceRepository extends JpaRepository<EnrollmentPersistenceEntity, Long> {
    List<EnrollmentPersistenceEntity> findAllByAcmeStudentRecordId(AcmeStudentRecordId studentRecordId);

    List<EnrollmentPersistenceEntity> findAllByCourseId(CourseId courseId);

    Optional<EnrollmentPersistenceEntity> findByAcmeStudentRecordIdAndCourseId(AcmeStudentRecordId studentRecordId, CourseId courseId);
}

