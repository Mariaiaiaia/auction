package com.maria.router;

import com.maria.constant.NotificationServiceRouterConstants;
import com.maria.entity.ErrorResponse;
import com.maria.entity.Notification;
import com.maria.handler.NotificationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class NotificationRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = NotificationServiceRouterConstants.GET_ALL_NOTIFICATIONS,
                    method = RequestMethod.GET,
                    beanClass = NotificationHandler.class,
                    beanMethod = "getNotifications",
                    operation = @Operation(
                            operationId = "getAllNotifications",
                            summary = "Get all notifications for the current user",
                            description = "Returns a list of all notifications for the authenticated user",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of notifications returned successfully",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Notification.class)))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized - user is not authenticated"
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = NotificationServiceRouterConstants.DELETE_NOTIFICATION,
                    method = RequestMethod.DELETE,
                    beanClass = NotificationHandler.class,
                    beanMethod = "deleteNotification",
                    operation = @Operation(
                            operationId = "deleteNotification",
                            summary = "Delete a notification by ID",
                            description = "Deletes a specific notification for the authenticated user",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            description = "Notification ID to delete",
                                            required = true,
                                            schema = @Schema(type = "string")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Notification deleted successfully"
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Notification not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Notification does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized - JWT token is missing or invalid"
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden - user is not the seller of the auction",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Notification is not available\"}"))
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
            )
    })
    RouterFunction<ServerResponse> router(NotificationHandler notificationHandler) {
        return RouterFunctions
                .route(GET(NotificationServiceRouterConstants.GET_ALL_NOTIFICATIONS).and(accept(MediaType.APPLICATION_JSON)), notificationHandler::getNotifications)
                .andRoute(DELETE(NotificationServiceRouterConstants.DELETE_NOTIFICATION).and(accept(MediaType.APPLICATION_JSON)), notificationHandler::deleteNotification);
    }
}
