package com.acme.center.platform.learning.infrastructure.persistence.jpa.entities;

import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.CourseId;
import com.acme.center.platform.learning.domain.model.valueobjects.EnrollmentStatus;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.converters.AcmeStudentRecordIdPersistenceConverter;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.converters.CourseIdPersistenceConverter;
import com.acme.center.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for enrollments.
 */
@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
public class EnrollmentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = AcmeStudentRecordIdPersistenceConverter.class)
    @Column(name = "acme_student_id", nullable = false)
    private AcmeStudentRecordId acmeStudentRecordId;

    @Convert(converter = CourseIdPersistenceConverter.class)
    @Column(name = "course_id", nullable = false)
    private CourseId courseId;

    @OneToMany(mappedBy = "enrollment", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ProgressRecordItemPersistenceEntity> progressRecordItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;
}
