package com.acme.center.platform.learning.infrastructure.persistence.jpa.converters;

import com.acme.center.platform.learning.domain.model.valueobjects.CourseId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts course IDs between the domain model and persistence column values.
 */
@Converter(autoApply = false)
public class CourseIdPersistenceConverter implements AttributeConverter<CourseId, Long> {

    @Override
    public Long convertToDatabaseColumn(CourseId attribute) {
        return attribute == null ? null : attribute.courseId();
    }

    @Override
    public CourseId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new CourseId(dbData);
    }
}
