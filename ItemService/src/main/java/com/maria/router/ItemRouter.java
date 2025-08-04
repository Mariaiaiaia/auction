package com.maria.router;

import com.maria.constant.ItemServiceRouterConstants;
import com.maria.core.entity.AuctionDTO;
import com.maria.dto.ItemDTO;
import com.maria.entity.CreateItemRequest;
import com.maria.entity.ErrorResponse;
import com.maria.handler.ItemHandler;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Component
public class ItemRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = ItemServiceRouterConstants.GET_ITEM,
                    method = RequestMethod.GET,
                    beanClass = ItemHandler.class,
                    beanMethod = "getItem",
                    operation = @Operation(
                            operationId = "getItemById",
                            summary = "Get item by ID",
                            description = "Returns item details by its ID",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            description = "ID of the item to retrieve",
                                            required = true,
                                            example = "42"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Item found",
                                            content = @Content(schema = @Schema(implementation = ItemDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Item not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Item does not exist\"}"))
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
                    path = ItemServiceRouterConstants.CREATE_ITEM,
                    method = RequestMethod.POST,
                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                    beanClass = ItemHandler.class,
                    beanMethod = "createItem",
                    operation = @Operation(
                            operationId = "createItem",
                            summary = "Create a new item for auction",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            schema = @Schema(implementation = CreateItemRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Item successfully created",
                                            content = @Content(schema = @Schema(implementation = ItemDTO.class))
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
                    path = ItemServiceRouterConstants.GET_SELLER_ID,
                    method = RequestMethod.GET,
                    beanClass = ItemHandler.class,
                    beanMethod = "getSellerId",
                    operation = @Operation(
                            operationId = "getSellerId",
                            summary = "Get current authenticated seller ID",
                            description = "Used by internal services to retrieve the seller ID by item ID. Requires the 'X-Internal-Service' header.",
                            parameters = {
                                    @Parameter(
                                            name = "X-Internal-Service",
                                            in = ParameterIn.HEADER,
                                            required = true,
                                            description = "Must be 'true'"
                                    ),
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the item to find the seller"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Seller ID successfully retrieved",
                                            content = @Content(schema = @Schema(type = "integer", example = "101"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Item not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Item does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden if not internal"
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
    RouterFunction<ServerResponse> router(ItemHandler itemHandler) {
        return RouterFunctions
                .route(GET(ItemServiceRouterConstants.GET_ITEM).and(accept(MediaType.APPLICATION_JSON)), itemHandler::getItem)
                .andRoute(POST(ItemServiceRouterConstants.CREATE_ITEM).and(accept(MediaType.MULTIPART_FORM_DATA)), itemHandler::createItem)
                .andRoute(GET(ItemServiceRouterConstants.GET_SELLER_ID).and(accept(MediaType.APPLICATION_JSON))
                        .and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))), itemHandler::getSellerId);
    }
}
