package com.acme.center.platform.learning.domain.model.commands;

/**
 * Command to create a course.
 *
 * @param title The title of the course. Cannot be null or blank.
 * @param description The description of the course. Cannot be null or blank.
 */
public record CreateCourseCommand(String title, String description) {
    /**
     * Compact constructor for CreateCourseCommand.
     * Validates that title and description are not null or blank.
     * @throws IllegalArgumentException if the title or description is null or blank.
     */
    public CreateCourseCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description cannot be null or blank");
        }
    }
}
