package com.acme.center.platform.learning.domain.model.valueobjects;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressRecordTest {

    @Test
    void emptyProgressRecordReturnsNullForLatestAndInitialItems() {
        var progressRecord = new ProgressRecord();

        assertNull(progressRecord.getLatestProgressRecordItem());
        assertNull(progressRecord.getInitialProgressRecordItem());
        assertTrue(progressRecord.calculateProgressMilestones().isEmpty());
    }

    @Test
    void sequencedOperationsRetrieveInitialAndLatestProgressItems() {
        var course = new Course("DDD Modeling", "Domain Driven Design");
        var learningPath = new LearningPath();
        learningPath.addItem(course, new TutorialId(1L));
        learningPath.addItem(course, new TutorialId(2L));
        learningPath.addItem(course, new TutorialId(3L));

        var enrollment = new Enrollment(new AcmeStudentRecordId(), new CourseId(1L));
        var progressRecord = new ProgressRecord();
        progressRecord.initializeProgressRecord(enrollment, learningPath);

        assertNotNull(progressRecord.getInitialProgressRecordItem());
        assertEquals(new TutorialId(1L), progressRecord.getInitialProgressRecordItem().getTutorialId());
        assertEquals(new TutorialId(1L), progressRecord.getLatestProgressRecordItem().getTutorialId());

        progressRecord.startTutorial(new TutorialId(1L));
        progressRecord.completeTutorial(new TutorialId(1L), learningPath);

        assertEquals(new TutorialId(1L), progressRecord.getInitialProgressRecordItem().getTutorialId());
        assertNotNull(progressRecord.getLatestProgressRecordItem());
        assertEquals(new TutorialId(2L), progressRecord.getLatestProgressRecordItem().getTutorialId());
    }

    @Test
    void streamGathererScanCalculatesProgressMilestones() {
        var course = new Course("Java 27 Streams", "Gatherers in depth");
        var learningPath = new LearningPath();
        learningPath.addItem(course, new TutorialId(10L));
        learningPath.addItem(course, new TutorialId(20L));

        var enrollment = new Enrollment(new AcmeStudentRecordId(), new CourseId(2L));
        var progressRecord = new ProgressRecord();
        progressRecord.initializeProgressRecord(enrollment, learningPath);

        progressRecord.startTutorial(new TutorialId(10L));
        progressRecord.completeTutorial(new TutorialId(10L), learningPath);

        progressRecord.startTutorial(new TutorialId(20L));

        var milestones = progressRecord.calculateProgressMilestones();
        assertFalse(milestones.isEmpty());
        assertEquals(2, milestones.size());
        assertEquals(new TutorialId(10L), milestones.get(0).tutorialId());
        assertEquals(new TutorialId(20L), milestones.get(1).tutorialId());
    }
}
