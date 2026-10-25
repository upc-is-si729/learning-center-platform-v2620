package com.acme.center.platform.learning.infrastructure.persistence.jpa.assemblers;

import com.acme.center.platform.learning.domain.model.aggregates.Course;
import com.acme.center.platform.learning.domain.model.entities.LearningPathItem;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.CoursePersistenceEntity;
import com.acme.center.platform.learning.infrastructure.persistence.jpa.entities.LearningPathItemPersistenceEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Static assembler between course domain and persistence representations.
 */
public final class CoursePersistenceAssembler {

    private CoursePersistenceAssembler() {
    }

    public static Course toDomainFromPersistence(CoursePersistenceEntity entity) {
        if (entity == null) return null;

        var learningPath = new com.acme.center.platform.learning.domain.model.valueobjects.LearningPath();
        var course = Course.reconstitute(entity.getId(), entity.getTitle(), entity.getDescription(), learningPath);

        if (entity.getLearningPathItems() != null) {
            var domainItemsById = new HashMap<Long, LearningPathItem>();
            for (var item : entity.getLearningPathItems()) {
                var domainItem = LearningPathItem.reconstitute(item.getId(), course, item.getTutorialId(), null);
                domainItemsById.put(item.getId(), domainItem);
            }
            for (var item : entity.getLearningPathItems()) {
                var current = domainItemsById.get(item.getId());
                if (item.getNextItem() != null) {
                    current.updateNextItem(domainItemsById.get(item.getNextItem().getId()));
                }
            }

            List<LearningPathItem> orderedItems = new ArrayList<>();
            for (var item : entity.getLearningPathItems()) {
                orderedItems.add(domainItemsById.get(item.getId()));
            }
            learningPath.setLearningPathItems(orderedItems);
        }

        return course;
    }

    public static CoursePersistenceEntity toPersistenceFromDomain(Course course) {
        if (course == null) return null;

        var entity = new CoursePersistenceEntity();
        // Only set ID if the course is being updated (has a non-null ID)
        // For new courses, leave ID null to allow JPA to generate it
        if (course.getId() != null) {
            entity.setId(course.getId());
        }
        entity.setTitle(course.getTitle());
        entity.setDescription(course.getDescription());

        var persistenceByDomain = new HashMap<LearningPathItem, LearningPathItemPersistenceEntity>();
        for (var item : course.getLearningPath().getLearningPathItems()) {
            var persistenceItem = new LearningPathItemPersistenceEntity();
            // Only set ID if the item is being updated (has a non-null ID)
            // For new items, leave ID null to allow JPA to generate it
            if (item.getId() != null) {
                persistenceItem.setId(item.getId());
            }
            persistenceItem.setCourse(entity);
            persistenceItem.setTutorialId(item.getTutorialId());
            persistenceByDomain.put(item, persistenceItem);
        }
        for (var item : course.getLearningPath().getLearningPathItems()) {
            var current = persistenceByDomain.get(item);
            current.setNextItem(item.getNextItem() == null ? null : persistenceByDomain.get(item.getNextItem()));
        }

        List<LearningPathItemPersistenceEntity> orderedPersistenceItems = new ArrayList<>();
        for (var item : course.getLearningPath().getLearningPathItems()) {
            orderedPersistenceItems.add(persistenceByDomain.get(item));
        }
        entity.setLearningPathItems(orderedPersistenceItems);

        return entity;
    }
}
