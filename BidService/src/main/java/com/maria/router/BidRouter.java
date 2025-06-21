package com.maria.router;

import com.maria.constant.BidServiceRouterConstants;
import com.maria.handler.BidHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class BidRouter {
    @Bean
    public RouterFunction<ServerResponse> router(BidHandler bidHandler) {
        return RouterFunctions
                .route(POST(BidServiceRouterConstants.NEW_BID).and(accept(MediaType.APPLICATION_JSON)), bidHandler::placeBid)
                .andRoute(GET(BidServiceRouterConstants.GET_BIDDERS_ID).and(accept(MediaType.APPLICATION_JSON))
                        .and(request -> "true".equals(request.headers().firstHeader("X-Internal-Service"))), bidHandler::getBiddersIdFromAuction)
                .andRoute(GET(BidServiceRouterConstants.USER_BIDS).and(accept(MediaType.APPLICATION_JSON)), bidHandler::getAllBidsForUser)
                .andRoute(GET(BidServiceRouterConstants.ALL_BIDS_FOR_AUCTION).and(accept(MediaType.APPLICATION_JSON)), bidHandler::getAllBidsForAuction);
    }
}
