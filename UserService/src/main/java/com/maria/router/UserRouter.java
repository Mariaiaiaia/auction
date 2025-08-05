package com.maria.router;

import com.maria.constant.UserServiceRouterConstants;
import com.maria.dto.UserDTO;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.entity.ErrorResponse;
import com.maria.entity.User;
import com.maria.handler.UserHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class UserRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = UserServiceRouterConstants.GET_INFO,
                    method = RequestMethod.GET,
                    beanClass = UserHandler.class,
                    beanMethod = "getInfo",
                    operation = @Operation(
                            operationId = "getUserInfo",
                            summary = "Get current user info",
                            description = "Returns the information of the currently authenticated user",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the user"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User info returned",
                                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "User not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"User does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "User ID is invalid",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Invalid user ID format\"}"))
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
                    path = UserServiceRouterConstants.UPDATE_USER,
                    method = RequestMethod.PATCH,
                    beanClass = UserHandler.class,
                    beanMethod = "update",
                    operation = @Operation(
                            operationId = "updateUser",
                            summary = "Update user information",
                            description = "Updates user fields like first name, last name, and email.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = UserUpdateDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User successfully updated",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = UserDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"User is not available\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "User not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"User does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input data",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Invalid user ID format\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Unexpected error occurred\"}"))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = UserServiceRouterConstants.DELETE_USER,
                    method = RequestMethod.DELETE,
                    beanClass = UserHandler.class,
                    beanMethod = "delete",
                    operation = @Operation(
                            operationId = "deleteUser",
                            summary = "Delete user by ID",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "User ID to delete",
                                            example = "123"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User successfully deleted"
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"User is not available\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "User not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"User does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input data",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Invalid user ID format\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Unexpected error occurred\"}"))
                                    )
                            }

                    )
            ),
            @RouterOperation(
                    path = UserServiceRouterConstants.FIND_USER_BY_EMAIL,
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    beanClass = UserHandler.class,
                    beanMethod = "findUserByEmail",
                    operation = @Operation(
                            operationId = "findUserByEmail",
                            summary = "Find user by email (internal call only)",
                            description = "Returns user details by email. Only accessible for internal services with proper header.",
                            parameters = {
                                    @Parameter(
                                            name = "X-Internal-Service",
                                            in = ParameterIn.HEADER,
                                            required = true,
                                            description = "Must be 'true'"
                                    ),
                                    @Parameter(
                                            name = "userEmail",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Email of the user"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User found",
                                            content = @Content(schema = @Schema(implementation = User.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "User not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"User does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden if not internal"
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Unexpected error occurred\"}"))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = UserServiceRouterConstants.REGISTER,
                    method = RequestMethod.POST,
                    beanClass = UserHandler.class,
                    beanMethod = "register",
                    operation = @Operation(
                            operationId = "registerUser",
                            summary = "Register a new user",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "User registration data",
                                    content = @Content(
                                            schema = @Schema(implementation = UserRegistrationDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User successfully registered",
                                            content = @Content(schema = @Schema(implementation = UserDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input data",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"A user with this email already exists\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Unexpected error occurred\"}"))
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> route(UserHandler userHandler) {
        return RouterFunctions
                .route(GET(UserServiceRouterConstants.GET_INFO).and(accept(MediaType.APPLICATION_JSON)), userHandler::getInfo)
                .andRoute(PATCH(UserServiceRouterConstants.UPDATE_USER).and(accept(MediaType.APPLICATION_JSON)), userHandler::update)
                .andRoute(DELETE(UserServiceRouterConstants.DELETE_USER).and(accept(MediaType.APPLICATION_JSON)), userHandler::delete)
                .andRoute(GET(UserServiceRouterConstants.FIND_USER_BY_EMAIL).and(accept(MediaType.APPLICATION_JSON)).and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))), userHandler::findUserByEmail)
                .andRoute(POST(UserServiceRouterConstants.REGISTER).and(accept(MediaType.APPLICATION_JSON)), userHandler::register);
    }
}