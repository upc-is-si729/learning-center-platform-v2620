package com.acme.center.platform.learning.domain.model.valueobjects;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.model.entities.LearningPathItem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Gatherers;

/**
 * LearningPath value object.
 *
 * <p>
 * Represents the sequence of tutorials that make up a course.
 * It is implemented as a collection of {@link LearningPathItem}s, which form a linked structure.
 * </p>
 */
public class LearningPath {
    /**
     * The list of items in the learning path.
     */
    @Getter
    private List<LearningPathItem> learningPathItems;

    /**
     * Default constructor.
     * Initializes an empty learning path.
     */
    public LearningPath() {
        this.learningPathItems = new ArrayList<>();
    }

    /**
     * Sets the list of learning path items.
     * @param learningPathItems The list of learning path items.
     */
    public void setLearningPathItems(List<LearningPathItem> learningPathItems) {
        this.learningPathItems = learningPathItems == null ? new ArrayList<>() : learningPathItems;
    }

    /**
     * Gets a learning path item by its ID.
     * @param itemId The ID of the item.
     * @return The learning path item if found, null otherwise.
     */
    private LearningPathItem getLearningPathItemWithId(Long itemId) {
        return this.getFirstLearningPathItemWhere(item -> item.getId().equals(itemId));
    }

    /**
     * Gets a learning path item by its tutorial ID.
     * @param tutorialId The ID of the tutorial.
     * @return The learning path item if found, null otherwise.
     */
    public LearningPathItem getLearningPathItemWithTutorialId(TutorialId tutorialId) {
        return this.getFirstLearningPathItemWhere(item -> item.getTutorialId().equals(tutorialId));
    }

    /**
     * Gets the ID of the next tutorial in the learning path.
     * @param currentTutorialId The ID of the current tutorial.
     * @return The ID of the next tutorial if it exists, null otherwise.
     */
    public TutorialId getNextTutorialInLearningPath(TutorialId currentTutorialId) {
        LearningPathItem item = getLearningPathItemWithTutorialId(currentTutorialId);
        if (item == null) return null;
        LearningPathItem nextItem = item.getNextItem();
        return !Objects.isNull(nextItem) ? nextItem.getTutorialId() : null;
    }

    /**
     * Checks if the given tutorial is the last one in the learning path.
     * @param currentTutorialId The ID of the tutorial to check.
     * @return true if it is the last tutorial, false otherwise.
     */
    public boolean isLastTutorialInLearningPath(TutorialId currentTutorialId) {
        return Objects.isNull(getNextTutorialInLearningPath(currentTutorialId));
    }

    /**
     * Gets the ID of the first tutorial in the learning path.
     * @return The ID of the first tutorial.
     */
    public TutorialId getFirstTutorialInLearningPath() {
        return isEmpty() ? null : learningPathItems.getFirst().getTutorialId();
    }

    /**
     * Gets the last item in the learning path using SequencedCollection.
     * @return The last learning path item, or null if empty.
     */
    public LearningPathItem getLastItemInLearningPath() {
        return isEmpty() ? null : learningPathItems.getLast();
    }

    /**
     * Gets the previous learning path item before the specified tutorial using reverse sequenced traversal.
     *
     * @param tutorialId the tutorial ID
     * @return optional containing the previous LearningPathItem if present
     */
    public Optional<LearningPathItem> getPreviousItem(TutorialId tutorialId) {
        LearningPathItem target = null;
        for (LearningPathItem item : learningPathItems.reversed()) {
            if (target != null) {
                return Optional.of(item);
            }
            if (item.getTutorialId().equals(tutorialId)) {
                target = item;
            }
        }
        return Optional.empty();
    }

    /**
     * Verifies that consecutive elements in the learning path maintain proper forward linking
     * using Stream Gatherers sliding window.
     *
     * @return true if the sequence is consistent, false otherwise
     */
    public boolean isSequenceConsistent() {
        if (learningPathItems.size() < 2) {
            return true;
        }
        return learningPathItems.stream()
                .gather(Gatherers.windowSliding(2))
                .allMatch(window -> window.getFirst().getNextItem() == window.getLast());
    }

    /**
     * Gets the first learning path item that matches the given predicate.
     * @param predicate The predicate to match.
     * @return The matching learning path item if found, null otherwise.
     */
    private LearningPathItem getFirstLearningPathItemWhere(Predicate<LearningPathItem> predicate) {
        return learningPathItems.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if the learning path is empty.
     * @return true if the learning path is empty, false otherwise.
     */
    public boolean isEmpty() {
        return learningPathItems.isEmpty();
    }

    /**
     * Adds a new item to the learning path.
     * @param course The course the item belongs to.
     * @param tutorialId The ID of the tutorial to add.
     * @param nextItem The next item in the sequence.
     */
    public void addItem(Course course, TutorialId tutorialId, LearningPathItem nextItem) {
        LearningPathItem learningPathItem = new LearningPathItem(course, tutorialId, nextItem);
        learningPathItems.add(learningPathItem);
    }

    /**
     * Adds a new item to the end of the learning path using SequencedCollection.
     * @param course The course the item belongs to.
     * @param tutorialId The ID of the tutorial to add.
     */
    public void addItem(Course course, TutorialId tutorialId) {
        LearningPathItem learningPathItem = new LearningPathItem(course, tutorialId, null);
        LearningPathItem originalLastItem = !isEmpty() ? learningPathItems.getLast() : null;
        learningPathItems.add(learningPathItem);
        if (!Objects.isNull(originalLastItem)) originalLastItem.updateNextItem(learningPathItem);
    }

    /**
     * Adds a new item to the learning path before a specific tutorial.
     * @param course The course the item belongs to.
     * @param tutorialId The ID of the tutorial to add.
     * @param nextTutorialId The ID of the tutorial that should follow the added tutorial.
     */
    public void addItem(Course course, TutorialId tutorialId, TutorialId nextTutorialId) {
        LearningPathItem nextItem = getLearningPathItemWithTutorialId(nextTutorialId);
        addItem(course, tutorialId, nextItem);
    }
}
