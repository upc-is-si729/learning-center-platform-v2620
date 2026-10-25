package com.acme.center.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.acme.center.platform.iam.domain.model.aggregates.User;
import com.acme.center.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;

import java.util.HashSet;

/**
 * Static assembler between IAM user domain and persistence representations.
 */
public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {
    }

    public static User toDomainFromPersistence(UserPersistenceEntity entity) {
        if (entity == null) return null;
        var roles = entity.getRoles() == null
                ? new HashSet<com.acme.center.platform.iam.domain.model.entities.Role>()
                : entity.getRoles().stream()
                .map(RolePersistenceAssembler::toDomainFromPersistence)
                .collect(java.util.stream.Collectors.toSet());
        return User.reconstitute(entity.getId(), entity.getUsername(), entity.getPassword(), roles);
    }

    public static UserPersistenceEntity toPersistenceFromDomain(User user) {
        if (user == null) return null;
        var entity = new UserPersistenceEntity();
        // Only set ID if the user is being updated (has a non-null ID)
        // For new users, leave ID null to allow JPA to generate it
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setRoles(user.getRoles() == null
                ? new HashSet<>()
                : user.getRoles().stream()
                .map(RolePersistenceAssembler::toPersistenceFromDomain)
                .collect(java.util.stream.Collectors.toSet()));
        return entity;
    }
}

