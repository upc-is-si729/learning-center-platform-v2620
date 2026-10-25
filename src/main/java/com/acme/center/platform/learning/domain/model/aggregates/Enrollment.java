package com.acme.center.platform.learning.domain.model.aggregates;

import com.acme.center.platform.learning.domain.model.events.TutorialCompletedEvent;
import com.acme.center.platform.learning.domain.model.valueobjects.*;
import com.acme.center.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.Objects;

/**
 * Enrollment aggregate root.
 *
 * <p>Represents a learner enrollment in a specific course and governs enrollment state
 * transitions and tutorial completion progress.</p>
 */
@Getter
public class Enrollment extends AbstractDomainAggregateRoot<Enrollment> {
    /**
     * The unique identifier for the enrollment.
     */
    private Long id;

    /**
     * The Acme Student Record ID of the student enrolled in the course.
     */
    private AcmeStudentRecordId acmeStudentRecordId;

    /**
     * The course ID in which the student is enrolled.
     */
    private CourseId courseId;

    /**
     * The progress record for the enrollment, tracking tutorial completion.
     */
    private ProgressRecord progressRecord;

    /**
     * The current status of the enrollment.
     */
    private EnrollmentStatus status;

    /**
     * Protected default constructor for Enrollment.
     */
    protected Enrollment() {
        this.progressRecord = new ProgressRecord();
    }

    /**
     * Constructor for Enrollment.
     * Initializes a new enrollment with the REQUESTED status and an empty progress record.
     * @param acmeStudentRecordId The Acme Student Record ID of the student.
     * @param courseId The course ID for the enrollment.
     */
    public Enrollment(AcmeStudentRecordId acmeStudentRecordId, CourseId courseId) {
        this.acmeStudentRecordId = Objects.requireNonNull(acmeStudentRecordId, "studentRecordId is required");
        this.courseId = Objects.requireNonNull(courseId, "courseId is required");
        this.status = EnrollmentStatus.REQUESTED;
        this.progressRecord = new ProgressRecord();
    }

    /**
     * Reconstitutes an existing enrollment from persistence state.
     *
     * @param id The unique identifier.
     * @param acmeStudentRecordId The Acme Student Record ID.
     * @param courseId The course ID for the enrollment.
     * @param status The current enrollment status.
     * @param progressRecord The progress record.
     * @return The reconstituted Enrollment instance.
     */
    public static Enrollment reconstitute(
            Long id,
            AcmeStudentRecordId acmeStudentRecordId,
            CourseId courseId,
            EnrollmentStatus status,
            ProgressRecord progressRecord) {
        var enrollment = new Enrollment(acmeStudentRecordId, courseId);
        enrollment.id = id;
        enrollment.status = status;
        enrollment.progressRecord = progressRecord != null ? progressRecord : new ProgressRecord();
        return enrollment;
    }

    /**
     * Confirms the enrollment.
     * Changes the status to CONFIRMED and initializes the progress record based on the provided learning path.
     *
     * @param learningPath The learning path of the course.
     */
    public void confirm(LearningPath learningPath) {
        this.status = EnrollmentStatus.CONFIRMED;
        this.progressRecord.initializeProgressRecord(this, learningPath);
    }

    /**
     * Rejects the enrollment.
     * Changes the status to REJECTED.
     */
    public void reject() {
        this.status = EnrollmentStatus.REJECTED;
    }

    /**
     * Cancels the enrollment.
     * Changes the status to `CANCELED`.
     */
    public void cancel() {
        this.status = EnrollmentStatus.CANCELED;
    }

    /**
     * Checks if the enrollment is confirmed.
     * @return true if the enrollment is confirmed, false otherwise.
     */
    public boolean isConfirmed() {
        return this.status == EnrollmentStatus.CONFIRMED;
    }

    /**
     * Checks if the enrollment is rejected.
     * @return true if the enrollment is rejected, false otherwise.
     */
    public boolean isRejected() {
        return this.status == EnrollmentStatus.REJECTED;
    }

    /**
     * Checks if the enrollment is canceled.
     * @return true if the enrollment is canceled, false otherwise.
     */
    public boolean isCancelled() {
        return this.status == EnrollmentStatus.CANCELED;
    }

    /**
     * Gets the name of the enrollment status in lowercase format.
     * @return The status name.
     */
    public String getStatusName() {
        return this.status.name().toLowerCase();
    }

    /**
     * Calculates the days elapsed since the enrollment began.
     * @return The number of days elapsed.
     */
    public long calculateDaysElapsed() {
        return progressRecord.calculateDaysElapsedForEnrollment(this);
    }

    /**
     * Marks a tutorial as completed for this enrollment.
     * Registers a TutorialCompletedEvent.
     * @param tutorialId The ID of the tutorial to complete.
     * @param learningPath The learning path of the course.
     */
    public void completeTutorial(TutorialId tutorialId, LearningPath learningPath) {
        this.progressRecord.completeTutorial(tutorialId, learningPath);
        this.registerDomainEvent(new TutorialCompletedEvent(this, this.getId(), tutorialId));
    }
}
