package com.acme.center.platform.learning.domain.model.valueobjects;

import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import com.acme.center.platform.learning.domain.model.entities.ProgressRecordItem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Gatherers;

/**
 * ProgressRecord value object.
 *
 * <p>
 * Represents the progress record for a student's enrollment in a course.
 * It contains a collection of {@link ProgressRecordItem}s, each tracking the progress of a specific tutorial.
 * </p>
 */
public class ProgressRecord {

    /**
     * The list of items in the progress record.
     */
    @Getter
    private List<ProgressRecordItem> progressRecordItems;

    /**
     * Default constructor.
     * Initializes an empty progress record.
     */
    public ProgressRecord() {
        progressRecordItems = new ArrayList<>();
    }

    /**
     * Sets the list of progress record items.
     * @param progressRecordItems The list of progress record items.
     */
    public void setProgressRecordItems(List<ProgressRecordItem> progressRecordItems) {
        this.progressRecordItems = progressRecordItems == null ? new ArrayList<>() : progressRecordItems;
    }

    /**
     * Gets the latest progress record item using SequencedCollection.
     * @return the latest ProgressRecordItem, or null if empty
     */
    public ProgressRecordItem getLatestProgressRecordItem() {
        return progressRecordItems.isEmpty() ? null : progressRecordItems.getLast();
    }

    /**
     * Gets the initial progress record item using SequencedCollection.
     * @return the first ProgressRecordItem, or null if empty
     */
    public ProgressRecordItem getInitialProgressRecordItem() {
        return progressRecordItems.isEmpty() ? null : progressRecordItems.getFirst();
    }

    /**
     * Initializes the progress record for an enrollment.
     * Adds the first tutorial from the learning path to the progress record.
     * @param enrollment The enrollment associated with the progress record.
     * @param learningPath The learning path of the course.
     */
    public void initializeProgressRecord(Enrollment enrollment, LearningPath learningPath) {
        if (learningPath.isEmpty()) return;
        TutorialId tutorialId = learningPath.getFirstTutorialInLearningPath();
        ProgressRecordItem progressRecordItem = new ProgressRecordItem(enrollment, tutorialId);
        progressRecordItems.add(progressRecordItem);
    }

    /**
     * Gets a progress record item by its tutorial ID.
     * @param tutorialId The ID of the tutorial.
     * @return The progress record item if found, null otherwise.
     */
    private ProgressRecordItem getProgressRecordItemWithTutorialId(TutorialId tutorialId) {
        return progressRecordItems.stream()
                .filter(progressRecordItem -> progressRecordItem.getTutorialId().equals(tutorialId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if there is any tutorial currently in progress.
     * @return true if there is a tutorial in progress, false otherwise.
     */
    private boolean hasAnItemInProgress() {
        return progressRecordItems.stream().anyMatch(ProgressRecordItem::isInProgress);
    }

    /**
     * Starts a tutorial in the progress record.
     * @param tutorialId The ID of the tutorial to start.
     * @throws IllegalStateException if a tutorial is already in progress, or the tutorial is already started/completed.
     * @throws IllegalArgumentException if the tutorial is not found in the progress record.
     */
    public void startTutorial(TutorialId tutorialId) {
        if (hasAnItemInProgress()) throw new IllegalStateException("A tutorial is already in progress");

        ProgressRecordItem progressRecordItem = getProgressRecordItemWithTutorialId(tutorialId);
        if (progressRecordItem != null) {
            if (progressRecordItem.isNotStarted()) progressRecordItem.start();
            else throw new IllegalStateException("Tutorial with given Id is already started or completed");
        } else throw new IllegalArgumentException("Tutorial with given Id not found in progress record");
    }

    /**
     * Completes a tutorial in the progress record and adds the next tutorial from the learning path.
     * @param tutorialId The ID of the tutorial to complete.
     * @param learningPath The learning path of the course.
     * @throws IllegalArgumentException if the tutorial is not found in the progress record.
     */
    public void completeTutorial(TutorialId tutorialId, LearningPath learningPath) {
        ProgressRecordItem progressRecordItem = getProgressRecordItemWithTutorialId(tutorialId);
        if (progressRecordItem != null) progressRecordItem.complete();
        else throw new IllegalArgumentException("Tutorial with given Id not found in progress record");
        if (learningPath.isLastTutorialInLearningPath(tutorialId)) return;
        TutorialId nextTutorialId = learningPath.getNextTutorialInLearningPath(tutorialId);
        if (nextTutorialId != null) {
            ProgressRecordItem nextProgressRecordItem = new ProgressRecordItem(progressRecordItem.getEnrollment(), nextTutorialId);
            progressRecordItems.add(nextProgressRecordItem);
        }
    }

    /**
     * Calculates the total days elapsed for an enrollment based on all its progress record items.
     * @param enrollment The enrollment.
     * @return The total number of days elapsed.
     */
    public long calculateDaysElapsedForEnrollment(Enrollment enrollment) {
        return progressRecordItems.stream()
                .filter(progressRecordItem -> Objects.equals(progressRecordItem.getEnrollment(), enrollment))
                .mapToLong(ProgressRecordItem::calculateDaysElapsed)
                .sum();
    }

    /**
     * Progressive milestone record representing cumulative elapsed days at a tutorial.
     *
     * @param tutorialId the tutorial ID
     * @param cumulativeDaysElapsed cumulative days up to this milestone
     */
    public record ProgressMilestone(TutorialId tutorialId, long cumulativeDaysElapsed) {}

    /**
     * Calculates progressive timeline milestones across all started/completed tutorials
     * using Stream Gatherers cumulative scan.
     *
     * @return ordered list of progressive milestones
     */
    public List<ProgressMilestone> calculateProgressMilestones() {
        return progressRecordItems.stream()
                .filter(item -> !item.isNotStarted())
                .gather(Gatherers.scan(
                        () -> new ProgressMilestone(new TutorialId(0L), 0L),
                        (acc, item) -> new ProgressMilestone(
                                item.getTutorialId(),
                                acc.cumulativeDaysElapsed() + item.calculateDaysElapsed()
                        )
                ))
                .filter(milestone -> milestone.tutorialId().tutorialId() > 0L)
                .toList();
    }
}
