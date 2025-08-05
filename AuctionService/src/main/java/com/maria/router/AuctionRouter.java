package com.maria.router;

import com.maria.constant.AuctionServiceRouterConstants;
import com.maria.core.entity.AuctionDTO;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import com.maria.dto.ErrorResponse;
import com.maria.handler.AuctionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperations;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AuctionRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = AuctionServiceRouterConstants.GET_AUCTION,
                    method = RequestMethod.GET,
                    beanClass = AuctionHandler.class,
                    beanMethod = "getInfo",
                    operation = @Operation(
                            operationId = "getAuctionById",
                            summary = "Get auction by ID",
                            description = "Retrieves an auction by its ID. Returns the auction only if it is public, the user has access rights, or the user is the seller.",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "auction ID"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Auction found",
                                            content = @Content(schema = @Schema(implementation = AuctionDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Auction not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction not found\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "User does not have access to this auction",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"You are not a participant in this auction\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Auction ID is invalid",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Invalid auction ID format\"}"))
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
                    path = AuctionServiceRouterConstants.GET_ALL_AUCTIONS_FOR_USER,
                    method = RequestMethod.GET,
                    beanClass = AuctionHandler.class,
                    beanMethod = "getAllAuctionsForUser",
                    operation = @Operation(
                            operationId = "getAllAuctionByUser",
                            summary = "Get all auctions for current user",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of user's auctions",
                                            content = @Content(mediaType = "application/json",
                                                    array = @ArraySchema(schema = @Schema(implementation = AuctionDTO.class)))
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
                    path = AuctionServiceRouterConstants.GET_SELLER_ID,
                    method = RequestMethod.GET,
                    beanClass = AuctionHandler.class,
                    beanMethod = "getSellerId",
                    operation = @Operation(
                            operationId = "getSellerIdByAuctionId",
                            summary = "Get seller ID (internal only)",
                            description = "Used by internal services to retrieve the seller ID by auction ID. Requires the 'X-Internal-Service' header.",
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
                                            description = "ID of the auction to find the seller"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Seller ID",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(type = "string", example = "1")
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Auction not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden if not internal",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction is not available\"}"))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = AuctionServiceRouterConstants.GET_PRIVATE_AUCTION,
                    method = RequestMethod.GET,
                    beanClass = AuctionHandler.class,
                    beanMethod = "getPrivateAuctionsForUser",
                    operation = @Operation(
                            operationId = "getPrivateAuctionsForUser",
                            summary = "Get private auctions accessible to the current user",
                            description = "Returns a list of private auctions the current authenticated user is allowed to see",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of accessible private auctions",
                                            content = @Content(mediaType = "application/json",
                                                    array = @ArraySchema(schema = @Schema(implementation = AuctionDTO.class)))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized - user is not authenticated"
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
                    path = AuctionServiceRouterConstants.GET_AUCTIONS_FOR_SELLER,
                    method = RequestMethod.GET,
                    beanClass = AuctionHandler.class,
                    beanMethod = "getAllAuctionsForUserSeller",
                    operation = @Operation(
                            operationId = "getAllAuctionsForUserSeller",
                            summary = "Get all auctions for the seller",
                            description = "Returns all auctions where the authenticated user is the seller.",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of auctions for the seller",
                                            content = @Content(mediaType = "application/json",
                                                    array = @ArraySchema(schema = @Schema(implementation = AuctionDTO.class)))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized - user is not authenticated"
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
                    path = AuctionServiceRouterConstants.DELETE_AUCTION,
                    method = RequestMethod.DELETE,
                    beanClass = AuctionHandler.class,
                    beanMethod = "delete",
                    operation = @Operation(
                            operationId = "deleteAuction",
                            summary = "Delete auction by ID",
                            description = "Deletes an auction by its ID. Only the seller who created the auction is allowed to delete it.",
                            parameters = {
                                    @Parameter(
                                            name = "auctionId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the auction to delete"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Auction successfully deleted"
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized - user is not authenticated"
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden - user is not the seller of the auction",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction is not available\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Auction not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction does not exist\"}"))
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
                    path = AuctionServiceRouterConstants.CLOSE_AUCTION,
                    method = RequestMethod.POST,
                    beanClass = AuctionHandler.class,
                    beanMethod = "closeAuction",
                    operation = @Operation(
                            operationId = "closeAuction",
                            summary = "Close auction",
                            description = "Closes the auction with the given ID. Only the auction's seller can perform this operation.",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the auction to close")
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Auction successfully closed",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = AuctionDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Auction not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden - user is not the seller of the auction",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction is not available\"}"))
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
                    path = AuctionServiceRouterConstants.UPDATE_AUCTION,
                    method = RequestMethod.PATCH,
                    beanClass = AuctionHandler.class,
                    beanMethod = "update",
                    operation = @Operation(
                            operationId = "updateAuction",
                            summary = "Update auction",
                            description = "Allows the seller to update certain fields of their auction, such as starting price, end date, or access level. Only available for auctions that have not started yet.",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "ID of the auction to update"
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = AuctionUpdateDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Auction successfully updated"
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden - user is not the seller of the auction",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction is not available\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Auction not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input data",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Start date must be in the future\"}"))
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
                    path = AuctionServiceRouterConstants.CREATE_AUCTION,
                    method = RequestMethod.POST,
                    beanClass = AuctionHandler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "createAuction",
                            summary = "Create a new auction",
                            description = "Allows an authenticated user to create a new auction. Requires item ID, starting price, start and end dates, and public access flag.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = CreateAuctionRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Auction successfully created",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = AuctionDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden - user is not the seller of the item",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"This item does not belong to this user\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Auction not found",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction does not exist\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input data",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Start date must be in the future\"}"))
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
                    path = AuctionServiceRouterConstants.GET_PUBLIC_ACTIVE_AUCTIONS,
                    method = RequestMethod.GET,
                    beanClass = AuctionHandler.class,
                    beanMethod = "getActivePublicAuctions",
                    operation = @Operation(
                            operationId = "getActivePublicAuctions",
                            summary = "Get all active public auctions",
                            description = "Returns a list of currently active public auctions available to all users.",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of active public auctions retrieved successfully",
                                            content = @Content(mediaType = "application/json",
                                                    array = @ArraySchema(schema = @Schema(implementation = AuctionDTO.class)))
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
                    path = AuctionServiceRouterConstants.SEND_INVITATION,
                    method = RequestMethod.POST,
                    beanClass = AuctionHandler.class,
                    beanMethod = "sendInvitationToUser",
                    operation = @Operation(
                            operationId = "sendInvitationToUser",
                            summary = "Send invitations to users for a private auction",
                            description = "Sends invitations to a list of user emails to access a private auction. Requires JWT token to identify sender.",
                            parameters = {
                                    @Parameter(name = "auctionId", description = "ID of the auction to invite users to", required = true, in = ParameterIn.PATH, example = "42")
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "List of email addresses to invite",
                                    content = @Content(mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(type = "string", format = "email")),
                                            examples = @ExampleObject(value = "[\"user1@example.com\", \"user2@example.com\"]"))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Invitations sent successfully"
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden - user is not the seller of the auction",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = "{\"error\": \"Auction is not available\"}"))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(mediaType = "application/json",
                                                    examples = @ExampleObject(value = "{\"error\": \"Service error occurred\"}"))
                                    )
                            }
                    )
            )
    })
    RouterFunction<ServerResponse> router(AuctionHandler auctionHandler) {
        return RouterFunctions
                .route(GET(AuctionServiceRouterConstants.GET_AUCTION).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getInfo)
                .andRoute(GET(AuctionServiceRouterConstants.GET_ALL_AUCTIONS_FOR_USER).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getAllAuctionsForUser)
                .andRoute(GET(AuctionServiceRouterConstants.GET_SELLER_ID).and(accept(MediaType.APPLICATION_JSON))
                        .and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))), auctionHandler::getSellerId)
                .andRoute(GET(AuctionServiceRouterConstants.GET_PRIVATE_AUCTION).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getPrivateAuctionsForUser)
                .andRoute(GET(AuctionServiceRouterConstants.GET_AUCTIONS_FOR_SELLER).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getAllAuctionsForUserSeller)
                .andRoute(DELETE(AuctionServiceRouterConstants.DELETE_AUCTION).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::delete)
                .andRoute(POST(AuctionServiceRouterConstants.CLOSE_AUCTION).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::closeAuction)
                .andRoute(PATCH(AuctionServiceRouterConstants.UPDATE_AUCTION).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::update)
                .andRoute(POST(AuctionServiceRouterConstants.CREATE_AUCTION).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::create)
                .andRoute(GET(AuctionServiceRouterConstants.GET_PUBLIC_ACTIVE_AUCTIONS).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getActivePublicAuctions)
                .andRoute(POST(AuctionServiceRouterConstants.SEND_INVITATION).and(accept(MediaType.APPLICATION_JSON)), auctionHandler::sendInvitationToUser);
    }
}

