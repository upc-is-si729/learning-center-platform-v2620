package com.acme.center.platform.learning.application.commandservices;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.model.commands.AddTutorialToCourseLearningPathCommand;
import com.acme.center.platform.learning.domain.model.commands.CreateCourseCommand;
import com.acme.center.platform.learning.domain.model.commands.DeleteCourseCommand;
import com.acme.center.platform.learning.domain.model.commands.UpdateCourseCommand;
import com.acme.center.platform.shared.application.result.ApplicationError;
import com.acme.center.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@link Course} aggregate.
 */
public interface CourseCommandService {
    /**
     * Handles course creation.
     *
     * @param command command containing initial course data
     * @return created course identifier or an application error
     * @see CreateCourseCommand
     */
    Result<Long, ApplicationError> handle(CreateCourseCommand command);

    /**
     * Handler for course update.
     *
     * @param command command containing target course id and new data
     * @return updated course aggregate or an application error
     * @see UpdateCourseCommand
     */
    Result<Course, ApplicationError> handle(UpdateCourseCommand command);

    /**
     * Handles course deletion.
     *
     * @param command command containing target course id
     * @return deleted course identifier or an application error
     * @see DeleteCourseCommand
     */
    Result<Long, ApplicationError> handle(DeleteCourseCommand command);

    /**
     * Handles tutorial addition to a course learning path.
     *
     * @param command command containing target course id and tutorial id
     * @return updated course identifier or an application error
     * @see AddTutorialToCourseLearningPathCommand
     */
    Result<Long, ApplicationError> handle(AddTutorialToCourseLearningPathCommand command);
}

