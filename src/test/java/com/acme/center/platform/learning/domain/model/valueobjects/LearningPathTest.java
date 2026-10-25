package com.acme.center.platform.learning.domain.model.valueobjects;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LearningPathTest {

    @Test
    void emptyLearningPathReturnsNullForFirstAndLastItems() {
        var learningPath = new LearningPath();

        assertTrue(learningPath.isEmpty());
        assertNull(learningPath.getFirstTutorialInLearningPath());
        assertNull(learningPath.getLastItemInLearningPath());
        assertTrue(learningPath.isSequenceConsistent());
    }

    @Test
    void sequencedOperationsRetrieveFirstAndLastItemsCorrectly() {
        var course = new Course("DDD in Practice", "Domain-Driven Design with Java");
        var learningPath = new LearningPath();

        learningPath.addItem(course, new TutorialId(1L));
        learningPath.addItem(course, new TutorialId(2L));
        learningPath.addItem(course, new TutorialId(3L));

        assertFalse(learningPath.isEmpty());
        assertEquals(new TutorialId(1L), learningPath.getFirstTutorialInLearningPath());
        assertNotNull(learningPath.getLastItemInLearningPath());
        assertEquals(new TutorialId(3L), learningPath.getLastItemInLearningPath().getTutorialId());
    }

    @Test
    void reverseSequencedLookupFindsPreviousItem() {
        var course = new Course("Spring Boot 4", "Modern Spring Boot");
        var learningPath = new LearningPath();

        learningPath.addItem(course, new TutorialId(10L));
        learningPath.addItem(course, new TutorialId(20L));
        learningPath.addItem(course, new TutorialId(30L));

        var prevTo30 = learningPath.getPreviousItem(new TutorialId(30L));
        assertTrue(prevTo30.isPresent());
        assertEquals(new TutorialId(20L), prevTo30.get().getTutorialId());

        var prevTo20 = learningPath.getPreviousItem(new TutorialId(20L));
        assertTrue(prevTo20.isPresent());
        assertEquals(new TutorialId(10L), prevTo20.get().getTutorialId());

        var prevTo10 = learningPath.getPreviousItem(new TutorialId(10L));
        assertTrue(prevTo10.isEmpty());

        var prevToNonExistent = learningPath.getPreviousItem(new TutorialId(999L));
        assertTrue(prevToNonExistent.isEmpty());
    }

    @Test
    void streamGathererWindowSlidingValidatesSequenceConsistency() {
        var course = new Course("Modern Java", "Java 27 Capabilities");
        var learningPath = new LearningPath();

        learningPath.addItem(course, new TutorialId(100L));
        learningPath.addItem(course, new TutorialId(200L));
        learningPath.addItem(course, new TutorialId(300L));

        assertTrue(learningPath.isSequenceConsistent());
    }
}
