package com.acme.center.platform.iam.domain.model.aggregates;

import com.acme.center.platform.iam.domain.model.entities.Role;
import com.acme.center.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User aggregate root.
 */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {

    private Long id;
    private String username;
    private String password;
    private Set<Role> roles;

    public User() {
        this.roles = new HashSet<>();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public User(String username, String password, List<Role> roles) {
        this(username, password);
        addRoles(roles);
    }

    /**
     * Reconstitutes an existing user from the persistence state.
     *
     * @param id The unique identifier.
     * @param username The username.
     * @param password The encoded password.
     * @param roles The assigned roles.
     * @return The reconstituted User instance.
     */
    public static User reconstitute(Long id, String username, String password, Set<Role> roles) {
        var user = new User(username, password);
        user.id = id;
        user.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        return user;
    }

    /**
     * Add a role to the user.
     *
     * @param role the role to add
     * @return the user with the added role
     */
    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Add a list of roles to the user.
     *
     * @param roles the list of roles to add
     * @return the user with the added roles
     */
    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }
}
