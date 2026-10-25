package com.acme.center.platform.iam.interfaces.rest;

import com.acme.center.platform.iam.application.commandservices.UserCommandService;
import com.acme.center.platform.iam.domain.model.aggregates.User;
import com.acme.center.platform.iam.domain.model.commands.SignInCommand;
import com.acme.center.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.acme.center.platform.iam.interfaces.rest.resources.SignInResource;
import com.acme.center.platform.shared.application.result.ApplicationError;
import com.acme.center.platform.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokensControllerTest {

    @Mock
    private UserCommandService userCommandService;

    private TokensController tokensController;

    @BeforeEach
    void setUp() {
        tokensController = new TokensController(userCommandService);
    }

    @Test
    void generateTokenReturnsOkWhenCredentialsValid() {
        var user = new User("john_doe", "hashedPassword");
        user.setId(1L);
        var token = "jwt.token.value";
        when(userCommandService.handle(any(SignInCommand.class)))
                .thenReturn(Result.success(new ImmutablePair<>(user, token)));

        var resource = new SignInResource("john_doe", "password123");
        ResponseEntity<?> response = tokensController.generateToken(resource);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        var authUser = (AuthenticatedUserResource) response.getBody();
        assertEquals(1L, authUser.id());
        assertEquals("john_doe", authUser.username());
        assertEquals("jwt.token.value", authUser.token());
        verify(userCommandService).handle(any(SignInCommand.class));
    }

    @Test
    void generateTokenReturnsErrorWhenCredentialsInvalid() {
        when(userCommandService.handle(any(SignInCommand.class)))
                .thenReturn(Result.failure(ApplicationError.notFound("USER_NOT_FOUND", "User not found")));

        var resource = new SignInResource("invalid_user", "wrong_pass");
        ResponseEntity<?> response = tokensController.generateToken(resource);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        var problem = (ProblemDetail) response.getBody();
        assertEquals(404, problem.getStatus());
        verify(userCommandService).handle(any(SignInCommand.class));
    }
}
