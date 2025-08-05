package com.maria.router;

import com.maria.constant.SecurityServiceRouterConstants;
import com.maria.dto.AuthRequestDTO;
import com.maria.dto.AuthResponseDTO;
import com.maria.entity.ErrorResponse;
import com.maria.handler.SecurityHandler;
import io.swagger.v3.oas.annotations.Operation;
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
public class SecurityRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = SecurityServiceRouterConstants.LOGIN,
                    method = RequestMethod.POST,
                    beanClass = SecurityHandler.class,
                    beanMethod = "login",
                    operation = @Operation(
                            operationId = "login",
                            summary = "User login",
                            description = "Authenticates a user and returns a JWT token",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = AuthRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful login",
                                            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized - user is not authenticated",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Password does not match\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Service error occurred\"}"))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = SecurityServiceRouterConstants.LOGOUT,
                    method = RequestMethod.POST,
                    beanClass = SecurityHandler.class,
                    beanMethod = "logout",
                    operation = @Operation(
                            operationId = "logout",
                            summary = "User logout",
                            description = "Invalidates the JWT token",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful logout"
                                    )
                            }
                    )
            )

    })
    RouterFunction<ServerResponse> route(SecurityHandler securityHandler) {
        return RouterFunctions
                .route(POST(SecurityServiceRouterConstants.LOGIN).and(accept(MediaType.APPLICATION_JSON)), securityHandler::login)
                .andRoute(POST(SecurityServiceRouterConstants.LOGOUT).and(accept(MediaType.APPLICATION_JSON)), securityHandler::logout);
    }
}