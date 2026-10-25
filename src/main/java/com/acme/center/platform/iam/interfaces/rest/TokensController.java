package com.acme.center.platform.iam.interfaces.rest;

import com.acme.center.platform.iam.application.commandservices.UserCommandService;
import com.acme.center.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.acme.center.platform.iam.interfaces.rest.resources.SignInResource;
import com.acme.center.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.acme.center.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.acme.center.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TokensController
 * <p>
 *     REST controller that handles authentication and security token generation.
 *     Exposes endpoint:
 *     <ul>
 *         <li>POST /api/v1/tokens</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tokens", description = "Authentication and security token management endpoints")
public class TokensController {
    private final UserCommandService userCommandService;

    public TokensController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Handles the token creation (sign-in) request.
     *
     * @param signInResource the sign-in request body with username and password.
     * @return the authenticated user resource with JWT token.
     */
    @PostMapping
    @Operation(
        summary = "Create authentication token (Sign-in)",
        description = "Authenticates a user with provided credentials and returns a JWT token for subsequent requests."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Token generated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthenticatedUserResource.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid credentials or malformed request",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found with provided username",
                content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> generateToken(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var result = userCommandService.handle(signInCommand);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                auth -> AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(auth.getLeft(), auth.getRight()),
                HttpStatus.OK
        );
    }
}
