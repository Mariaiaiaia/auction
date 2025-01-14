package com.maria.router;

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
    public RouterFunction<ServerResponse> router(BidHandler bidHandler){
        return RouterFunctions
                .route(POST("/bids/new").and(accept(MediaType.APPLICATION_JSON)), bidHandler::placeBid)
                .andRoute(GET("/bids/bidders_id/{auctionId}").and(accept(MediaType.APPLICATION_JSON)), bidHandler::getBiddersIdFromAuction)
                .andRoute(GET("/bids/user_bids").and(accept(MediaType.APPLICATION_JSON)), bidHandler::getAllBidsForUser)
                .andRoute(GET("/bids/auction_bids").and(accept(MediaType.APPLICATION_JSON)), bidHandler::getAllBidsForAuction);
    }
}
