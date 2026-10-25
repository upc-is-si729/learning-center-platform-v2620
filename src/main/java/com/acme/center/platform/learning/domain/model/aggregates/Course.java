package com.acme.center.platform.learning.domain.model.aggregates;

import com.acme.center.platform.learning.domain.model.commands.CreateCourseCommand;
import com.acme.center.platform.learning.domain.model.valueobjects.LearningPath;
import com.acme.center.platform.learning.domain.model.valueobjects.TutorialId;
import com.acme.center.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

/**
 * Course aggregate root.
 *
 * <p>
 * Represents a course within the learning platform.
 * A course is defined by a title, a description, and a learning path consisting of tutorials.
 * </p>
 */
@Getter
public class Course extends AbstractDomainAggregateRoot<Course> {
    /**
     * The unique identifier for the course.
     */
    private Long id;

    /**
     * The title of the course.
     */
    private String title;

    /**
     * The description of the course.
     */
    private String description;

    /**
     * The learning path of the course.
     */
    private LearningPath learningPath;

    /**
     * Default constructor for Course.
     * Initializes an empty title, description, and a new learning path.
     */
    public Course() {
        this.title = Strings.EMPTY;
        this.description = Strings.EMPTY;
        this.learningPath = new LearningPath();
    }

    /**
     * Constructor for Course with title and description.
     * @param title The title.
     * @param description The description.
     */
    public Course(String title, String description) {
        this.title = title != null ? title : Strings.EMPTY;
        this.description = description != null ? description : Strings.EMPTY;
        this.learningPath = new LearningPath();
    }

    /**
     * Reconstitutes an existing course from persistence state.
     *
     * @param id The unique identifier.
     * @param title The title.
     * @param description The description.
     * @param learningPath The learning path.
     * @return The reconstituted Course instance.
     */
    public static Course reconstitute(Long id, String title, String description, LearningPath learningPath) {
        var course = new Course(title, description);
        course.id = id;
        course.learningPath = learningPath != null ? learningPath : new LearningPath();
        return course;
    }

    /**
     * Updates the course information.
     * @param title The new title.
     * @param description The new description.
     * @return The updated Course instance.
     */
    public Course updateInformation(String title, String description) {
        this.title = title;
        this.description = description;
        return this;
    }

    /**
     * Constructor for Course with a CreateCourseCommand.
     * @param command The CreateCourseCommand.
     */
    public Course(CreateCourseCommand command) {
        this.title = command.title();
        this.description = command.description();
        this.learningPath = new LearningPath();
    }

    /**
     * Adds a tutorial to the learning path.
     * @param tutorialId The ID of the tutorial to add.
     */
    public void addTutorialToLearningPath(TutorialId tutorialId) {
        this.learningPath.addItem(this, tutorialId);
    }

    /**
     * Adds a tutorial to the learning path after a specific tutorial.
     * @param tutorialId The ID of the tutorial to add.
     * @param nextTutorialId The ID of the tutorial that should follow the added tutorial.
     */
    public void addTutorialToLearningPath(TutorialId tutorialId, TutorialId nextTutorialId) {
        this.learningPath.addItem(this, tutorialId, nextTutorialId);
    }
}
