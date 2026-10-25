package com.acme.center.platform.learning.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourseIdTest {

    @Test
    void canonicalConstructorAcceptsValidId() {
        var courseId = new CourseId(42L);

        assertEquals(42L, courseId.courseId());
    }

    @Test
    void canonicalConstructorRejectsNullId() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new CourseId(null));

        assertEquals("Course id cannot be null or less than 1", exception.getMessage());
    }

    @Test
    void canonicalConstructorRejectsZeroOrNegativeId() {
        var exceptionZero = assertThrows(IllegalArgumentException.class, () -> new CourseId(0L));
        assertEquals("Course id cannot be null or less than 1", exceptionZero.getMessage());

        var exceptionNegative = assertThrows(IllegalArgumentException.class, () -> new CourseId(-5L));
        assertEquals("Course id cannot be null or less than 1", exceptionNegative.getMessage());
    }
}
