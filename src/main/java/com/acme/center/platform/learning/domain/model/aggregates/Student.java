package com.acme.center.platform.learning.domain.model.aggregates;

import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.ProfileId;
import com.acme.center.platform.learning.domain.model.valueobjects.StudentPerformanceMetricSet;
import com.acme.center.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

/**
 * Student aggregate root.
 *
 * <p>
 * Represents a student within the learning platform.
 * A student is identified by an Acme Student Record ID and is associated with a Profile ID.
 * It also tracks performance metrics such as total completed courses and tutorials.
 * </p>
 */
@Getter
public class Student extends AbstractDomainAggregateRoot<Student> {
    /**
     * The unique identifier for the student.
     */
    private Long id;

    /**
     * The Acme Student Record ID, which is the business identifier for the student.
     */
    private AcmeStudentRecordId acmeStudentRecordId;

    /**
     * The Profile ID associated with the student.
     */
    private ProfileId profileId;

    /**
     * The set of performance metrics for the student.
     */
    private StudentPerformanceMetricSet performanceMetricSet;

    /**
     * Default constructor for Student.
     * Initializes a new student with a unique Acme Student Record ID and empty performance metrics.
     */
    public Student() {
        this.acmeStudentRecordId = new AcmeStudentRecordId();
        this.performanceMetricSet = new StudentPerformanceMetricSet();
    }

    /**
     * Constructor for Student with a profile ID as Long.
     * @param profileId The profile ID value.
     */
    public Student(Long profileId) {
        this();
        this.profileId = new ProfileId(profileId);
    }

    /**
     * Constructor for Student with a ProfileId value object.
     * @param profileId The ProfileId value object.
     */
    public Student(ProfileId profileId) {
        this();
        this.profileId = profileId;
    }

    /**
     * Reconstitutes an existing student from persistence state.
     *
     * @param id The unique identifier.
     * @param acmeStudentRecordId The Acme Student Record ID.
     * @param profileId The Profile ID.
     * @param performanceMetricSet The student performance metrics.
     * @return The reconstituted Student instance.
     */
    public static Student reconstitute(
            Long id,
            AcmeStudentRecordId acmeStudentRecordId,
            ProfileId profileId,
            StudentPerformanceMetricSet performanceMetricSet) {
        var student = new Student();
        student.id = id;
        student.acmeStudentRecordId = acmeStudentRecordId;
        student.profileId = profileId;
        student.performanceMetricSet = performanceMetricSet != null
                ? performanceMetricSet
                : new StudentPerformanceMetricSet();
        return student;
    }

    /**
     * Updates the performance metrics when a course is completed.
     */
    public void updateMetricsOnCourseCompleted() {
        this.performanceMetricSet = this.performanceMetricSet.incrementTotalCompletedCourses();
    }

    /**
     * Updates the performance metrics when a tutorial is completed.
     */
    public void updateMetricsOnTutorialCompleted() {
        this.performanceMetricSet = this.performanceMetricSet.incrementTotalCompletedTutorials();
    }

    /**
     * Gets the Acme Student Record ID value.
     * @return The student record ID as a String.
     */
    public String getStudentRecordId() {
        return this.acmeStudentRecordId.studentRecordId();
    }

    /**
     * Gets the Profile ID value.
     * @return The profile ID as a Long.
     */
    public Long getProfileIdValue() {
        return this.profileId.profileId();
    }

    /**
     * Gets the total number of completed courses.
     * @return The total number of completed courses.
     */
    public int getTotalCompletedCourses() {
        return this.performanceMetricSet.totalCompletedCourses();
    }

    /**
     * Gets the total number of completed tutorials.
     * @return The total number of completed tutorials.
     */
    public int getTotalCompletedTutorials() {
        return this.performanceMetricSet.totalCompletedTutorials();
    }
}
