package com.acme.center.platform.learning.application.queryservices;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.model.entities.LearningPathItem;
import com.acme.center.platform.learning.domain.model.queries.GetAllCoursesQuery;
import com.acme.center.platform.learning.domain.model.queries.GetCourseByIdQuery;
import com.acme.center.platform.learning.domain.model.queries.GetLearningPathItemByCourseIdAndTutorialIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for `course` read queries.
 */
public interface CourseQueryService {
    /**
     * Handles retrieval of a course by id.
     *
     * @param query course-id query
     * @return matching course, if found
     * @see GetCourseByIdQuery
     */
    Optional<Course> handle(GetCourseByIdQuery query);

    /**
     * Handles retrieval of all courses.
     *
     * @param query query marker
     * @return list of courses
     * @see GetAllCoursesQuery
     */
    List<Course> handle(GetAllCoursesQuery query);

    /**
     * Handles retrieval of a learning path item by course and tutorial ids.
     *
     * @param query course/tutorial query
     * @return matching learning path item, if found
     * @see GetLearningPathItemByCourseIdAndTutorialIdQuery
     */
    Optional<LearningPathItem> handle(GetLearningPathItemByCourseIdAndTutorialIdQuery query);
}

