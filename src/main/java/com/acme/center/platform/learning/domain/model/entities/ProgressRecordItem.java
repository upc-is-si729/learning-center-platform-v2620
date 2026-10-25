package com.acme.center.platform.learning.domain.model.entities;

import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import com.acme.center.platform.learning.domain.model.valueobjects.ProgressStatus;
import com.acme.center.platform.learning.domain.model.valueobjects.TutorialId;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * ProgressRecordItem domain entity.
 *
 * <p>
 * Represents a record of a student's progress on a specific tutorial within an enrollment.
 * It tracks when the tutorial was started and completed, and its current progress status.
 * </p>
 */
@Getter
public class ProgressRecordItem {
    /**
     * The unique identifier for the progress record item.
     */
    private Long id;

    /**
     * The enrollment this progress record item belongs to.
     */
    @Setter
    private Enrollment enrollment;

    /**
     * The ID of the tutorial associated with this progress record item.
     */
    private TutorialId tutorialId;

    /**
     * The current status of the tutorial progress.
     */
    private ProgressStatus status;

    /**
     * The date and time when the tutorial was started.
     */
    private Date statedAt;

    /**
     * The date and time when the tutorial was completed.
     */
    private Date completedAt;

    /**
     * Constructor for ProgressRecordItem.
     * Initializes the status to NOT_STARTED.
     * @param enrollment The enrollment.
     * @param tutorialId The tutorial ID.
     */
    public ProgressRecordItem(Enrollment enrollment, TutorialId tutorialId) {
        this.enrollment = enrollment;
        this.tutorialId = tutorialId;
        this.status = ProgressStatus.NOT_STARTED;
    }

    /**
     * Reconstitutes an existing ProgressRecordItem from persistence.
     *
     * @param id The unique identifier.
     * @param enrollment The associated enrollment.
     * @param tutorialId The tutorial ID.
     * @param status The progress status.
     * @param statedAt The start date.
     * @param completedAt The completion date.
     * @return The reconstituted ProgressRecordItem.
     */
    public static ProgressRecordItem reconstitute(
            Long id,
            Enrollment enrollment,
            TutorialId tutorialId,
            ProgressStatus status,
            Date statedAt,
            Date completedAt) {
        var item = new ProgressRecordItem(enrollment, tutorialId);
        item.id = id;
        item.status = status;
        item.statedAt = statedAt;
        item.completedAt = completedAt;
        return item;
    }

    /**
     * Starts the tutorial.
     * Sets the status to STARTED and the start date to the current date and time.
     */
    public void start() {
        this.status = ProgressStatus.STARTED;
        this.statedAt = new Date();
    }

    /**
     * Completes the tutorial.
     * Sets the status to COMPLETED and the completion date to the current date and time.
     */
    public void complete() {
        this.status = ProgressStatus.COMPLETED;
        this.completedAt = new Date();
    }

    /**
     * Checks if the tutorial is completed.
     * @return true if the status is COMPLETED, false otherwise.
     */
    public boolean isCompleted() {
        return ProgressStatus.COMPLETED.equals(status);
    }

    /**
     * Checks if the tutorial is currently in progress.
     * @return true if the status is STARTED, false otherwise.
     */
    public boolean isInProgress() {
        return ProgressStatus.STARTED.equals(status);
    }

    /**
     * Checks if the tutorial has not yet been started.
     * @return true if the status is NOT_STARTED, false otherwise.
     */
    public boolean isNotStarted() {
        return ProgressStatus.NOT_STARTED.equals(status);
    }

    /**
     * Calculates the number of days elapsed since the tutorial was started.
     * If not started, returns 0. If not completed, calculates up to the current date.
     * @return The number of days elapsed.
     */
    public long calculateDaysElapsed() {
        if (ProgressStatus.NOT_STARTED.equals(status)) return 0;
        var defaultTimeZone = ZoneId.systemDefault();
        var fromDate = this.statedAt.toInstant().atZone(defaultTimeZone);
        var toDate = Objects.isNull(this.completedAt)
                ? LocalDate.now().atStartOfDay(defaultTimeZone).toInstant()
                : this.completedAt.toInstant();
        return Duration.between(fromDate, toDate).toDays();
    }
}
