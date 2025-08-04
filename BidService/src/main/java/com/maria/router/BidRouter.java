package com.maria.router;

import com.maria.constant.BidServiceRouterConstants;
import com.maria.dto.ErrorResponse;
import com.maria.dto.PlaceBidRequest;
import com.maria.entity.Bid;
import com.maria.handler.BidHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
public class BidRouter {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = BidServiceRouterConstants.NEW_BID,
                    method = RequestMethod.POST,
                    beanClass = BidHandler.class,
                    beanMethod = "placeBid",
                    operation = @Operation(
                            operationId = "placeNewBid",
                            summary = "Place a new bid",
                            description = "Allows a user to place a new bid on an auction. The bid must be higher than the current bid and the auction must be active.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = PlaceBidRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Bid placed successfully"
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
                    path = BidServiceRouterConstants.GET_BIDDERS_ID,
                    method = RequestMethod.GET,
                    beanClass = BidHandler.class,
                    beanMethod = "getBiddersIdFromAuction",
                    operation = @Operation(
                            operationId = "getBiddersIdFromAuction",
                            summary = "Get IDs of users who placed bids on a specific auction (internal use only)",
                            description = "Returns a list of user IDs who have placed bids on the specified auction. This endpoint is intended for internal service-to-service communication and requires the 'X-Internal-Service' header to be set to 'true'.",
                            parameters = {
                                    @Parameter(
                                            name = "auctionId",
                                            description = "ID of the auction",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "integer", example = "11")
                                    ),
                                    @Parameter(
                                            name = "X-Internal-Service",
                                            description = "Custom header to indicate internal service call",
                                            required = true,
                                            in = ParameterIn.HEADER
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successfully returned list of user IDs",
                                            content = @Content(array = @ArraySchema(schema = @Schema(type = "integer")),
                                                    examples = {
                                                            @ExampleObject(
                                                                    name = "UserIdsExample",
                                                                    summary = "Example list of user IDs",
                                                                    value = "[101, 202, 303]"
                                                            )
                                                    })
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden if not internal"
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Auction not found"
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
                    path = BidServiceRouterConstants.USER_BIDS,
                    method = RequestMethod.GET,
                    beanClass = BidHandler.class,
                    beanMethod = "getAllBidsForUser",
                    operation = @Operation(
                            operationId = "getAllBidsForUser",
                            summary = "Get all bids of the current user",
                            description = "Returns a list of all bids placed by the currently authenticated user.",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successfully retrieved list of user's bids",
                                            content = @Content(
                                                    array = @ArraySchema(schema = @Schema(implementation = Bid.class)),
                                                    examples = {
                                                            @ExampleObject(
                                                                    name = "UserBidsExample",
                                                                    summary = "Example response",
                                                                    value = "[{\"bidId\":1,\"userId\":12,\"bidAmount\":100.0,\"auctionId\":5},{\"bidId\":2,\"userId\":12,\"bidAmount\":150.0,\"auctionId\":6}]"
                                                            )
                                                    }
                                            )
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
                    path = BidServiceRouterConstants.ALL_BIDS_FOR_AUCTION,
                    method = RequestMethod.GET,
                    beanClass = BidHandler.class,
                    beanMethod = "getAllBidsForAuction",
                    operation = @Operation(
                            operationId = "getAllBidsForAuction",
                            summary = "Get all bids for a specific auction",
                            description = "Returns a list of all bids placed on the auction with the specified ID.",
                            parameters = {
                                    @Parameter(
                                            name = "auctionId",
                                            description = "ID of the auction",
                                            required = true,
                                            in = ParameterIn.QUERY,
                                            schema = @Schema(type = "integer", example = "42")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successfully returned list of bids",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Bid.class)))
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
                                                    examples = @ExampleObject(value = "{\"error\": \"Only the seller can see the bids for his auction\"}"))
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
    public RouterFunction<ServerResponse> router(BidHandler bidHandler) {
        return RouterFunctions
                .route(POST(BidServiceRouterConstants.NEW_BID).and(accept(MediaType.APPLICATION_JSON)), bidHandler::placeBid)
                .andRoute(GET(BidServiceRouterConstants.GET_BIDDERS_ID).and(accept(MediaType.APPLICATION_JSON))
                        .and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))), bidHandler::getBiddersIdFromAuction)
                .andRoute(GET(BidServiceRouterConstants.USER_BIDS).and(accept(MediaType.APPLICATION_JSON)), bidHandler::getAllBidsForUser)
                .andRoute(GET(BidServiceRouterConstants.ALL_BIDS_FOR_AUCTION).and(accept(MediaType.APPLICATION_JSON)), bidHandler::getAllBidsForAuction);
    }
}
