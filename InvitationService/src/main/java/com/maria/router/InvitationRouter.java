package com.maria.router;

import com.maria.constant.InvitationServiceRouterConstants;
import com.maria.entity.AcceptanceRequest;
import com.maria.entity.ErrorResponse;
import com.maria.entity.Invitation;
import com.maria.handler.InvitationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class InvitationRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = InvitationServiceRouterConstants.INVITATIONS,
                    method = RequestMethod.GET,
                    beanClass = InvitationHandler.class,
                    beanMethod = "invitationsForUser",
                    operation = @Operation(
                            operationId = "getInvitationsForUser",
                            summary = "Get invitations for user",
                            description = "Returns a list of invitations for the authenticated user",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of invitations successfully returned",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Invitation.class)))

                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized - invalid or missing JWT token"
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Service error",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Service error occurred\"}"))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = InvitationServiceRouterConstants.INVITATIONS_ACCEPTANCE,
                    method = RequestMethod.POST,
                    beanClass = InvitationHandler.class,
                    beanMethod = "respondToInvitation",
                    operation = @Operation(
                            operationId = "respondToInvitation",
                            summary = "Respond to an invitation",
                            description = "Allows a user to accept or reject an invitation to a private auction",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "User's response to an invitation",
                                    content = @Content(
                                            schema = @Schema(implementation = AcceptanceRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Response saved successfully"
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Invitation not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Invitation not found\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Service error",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Service error occurred\"}"))
                                    )
                            }
                    )
            )

    })
    RouterFunction<ServerResponse> router(InvitationHandler invitationHandler) {
        return RouterFunctions
                .route(GET(InvitationServiceRouterConstants.INVITATIONS).and(accept(MediaType.APPLICATION_JSON)), invitationHandler::invitationsForUser)
                .andRoute(POST(InvitationServiceRouterConstants.INVITATIONS_ACCEPTANCE).and(accept(MediaType.APPLICATION_JSON)), invitationHandler::respondToInvitation);
    }
}
