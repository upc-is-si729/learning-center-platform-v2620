package com.acme.center.platform.iam.interfaces.rest;

import com.acme.center.platform.iam.application.commandservices.UserCommandService;
import com.acme.center.platform.iam.application.queryservices.UserQueryService;
import com.acme.center.platform.iam.domain.model.aggregates.User;
import com.acme.center.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.center.platform.iam.domain.model.queries.GetAllUsersQuery;
import com.acme.center.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.acme.center.platform.iam.interfaces.rest.resources.SignUpResource;
import com.acme.center.platform.iam.interfaces.rest.resources.UserResource;
import com.acme.center.platform.shared.application.result.ApplicationError;
import com.acme.center.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserCommandService userCommandService;

    private UsersController usersController;

    @BeforeEach
    void setUp() {
        usersController = new UsersController(userQueryService, userCommandService);
    }

    @Test
    void createUserReturnsCreatedWhenSuccessful() {
        var user = new User("jane_doe", "hashedPassword");
        user.setId(2L);
        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Result.success(user));

        var resource = new SignUpResource("jane_doe", "password123", List.of("ROLE_USER"));
        ResponseEntity<?> response = usersController.createUser(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        var userResource = (UserResource) response.getBody();
        assertEquals(2L, userResource.id());
        assertEquals("jane_doe", userResource.username());
        verify(userCommandService).handle(any(SignUpCommand.class));
    }

    @Test
    void createUserReturnsConflictWhenUsernameExists() {
        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("USER_CONFLICT", "Username already exists")));

        var resource = new SignUpResource("jane_doe", "password123", List.of("ROLE_USER"));
        ResponseEntity<?> response = usersController.createUser(resource);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        var problem = (ProblemDetail) response.getBody();
        assertEquals(409, problem.getStatus());
        verify(userCommandService).handle(any(SignUpCommand.class));
    }

    @Test
    void getAllUsersReturnsListOfUsers() {
        var user = new User("alice", "hashedPassword");
        user.setId(3L);
        when(userQueryService.handle(any(GetAllUsersQuery.class))).thenReturn(List.of(user));

        ResponseEntity<List<UserResource>> response = usersController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("alice", response.getBody().get(0).username());
    }

    @Test
    void getUserByIdReturnsUserWhenFound() {
        var user = new User("bob", "hashedPassword");
        user.setId(4L);
        when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.of(user));

        ResponseEntity<UserResource> response = usersController.getUserById(4L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4L, response.getBody().id());
        assertEquals("bob", response.getBody().username());
    }

    @Test
    void getUserByIdReturnsNotFoundWhenUserDoesNotExist() {
        when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.empty());

        ResponseEntity<UserResource> response = usersController.getUserById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
