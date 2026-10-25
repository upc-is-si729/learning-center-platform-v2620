package com.acme.center.platform.learning.domain.model.aggregates;

import com.acme.center.platform.learning.domain.model.events.TutorialCompletedEvent;
import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import com.acme.center.platform.learning.domain.model.valueobjects.EnrollmentStatus;
import com.acme.center.platform.learning.domain.model.valueobjects.ProgressRecord;
import com.acme.center.platform.learning.domain.model.valueobjects.TutorialId;
import com.acme.center.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

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
    @Setter
    private Long id;

    /**
     * The Acme Student Record ID of the student enrolled in the course.
     */
    @Setter
    private AcmeStudentRecordId acmeStudentRecordId;

    /**
     * The course in which the student is enrolled.
     */
    @Setter
    private Course course;

    /**
     * The progress record for the enrollment, tracking tutorial completion.
     */
    @Setter
    private ProgressRecord progressRecord;

    /**
     * The current status of the enrollment.
     */
    @Setter
    private EnrollmentStatus status;

    /**
     * Default constructor for Enrollment.
     * Required for reconstruction from persistence.
     */
    public Enrollment() {
        // Required for reconstruction
    }

    /**
     * Constructor for Enrollment.
     * Initializes a new enrollment with the REQUESTED status and an empty progress record.
     * @param acmeStudentRecordId The Acme Student Record ID of the student.
     * @param course The course for the enrollment.
     */
    public Enrollment(AcmeStudentRecordId acmeStudentRecordId, Course course) {
        this.acmeStudentRecordId = acmeStudentRecordId;
        this.course = course;
        this.status = EnrollmentStatus.REQUESTED;
        this.progressRecord = new ProgressRecord();
    }

    /**
     * Confirms the enrollment.
     * Changes the status to CONFIRMED and initializes the progress record based on the course's learning path.
     */
    public void confirm() {
        this.status = EnrollmentStatus.CONFIRMED;
        this.progressRecord.initializeProgressRecord(this, course.getLearningPath());
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
     */
    public void completeTutorial(TutorialId tutorialId) {
        this.progressRecord.completeTutorial(tutorialId, course.getLearningPath());
        this.registerDomainEvent(new TutorialCompletedEvent(this, this.getId(), tutorialId));
    }
}
