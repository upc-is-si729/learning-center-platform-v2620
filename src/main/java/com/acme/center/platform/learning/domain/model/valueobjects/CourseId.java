package com.acme.center.platform.learning.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;

/**
 * Value object representing a course identifier.
 *
 * <p>
 * This value object encapsulates the identity of a course aggregate within the learning domain,
 * allowing other aggregates (such as {@link com.acme.center.platform.learning.domain.model.aggregates.Enrollment})
 * to reference courses strictly by identity.
 * </p>
 *
 * @param courseId The course identifier. Cannot be null or less than 1.
 */
@NullMarked
public record CourseId(Long courseId) {

    /**
     * Compact constructor for {@link CourseId}.
     *
     * @param courseId The course identifier.
     * @throws IllegalArgumentException if {@code courseId} is null or less than 1.
     */
    public CourseId {
        if (courseId == null || courseId < 1) {
            throw new IllegalArgumentException("Course id cannot be null or less than 1");
        }
    }
}
