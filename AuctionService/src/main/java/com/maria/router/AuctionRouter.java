package com.maria.router;

import com.maria.handler.AuctionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AuctionRouter {

    @Bean
    RouterFunction<ServerResponse> router(AuctionHandler auctionHandler){
        return RouterFunctions
                .route(GET("/auctions/{id}").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getInfo)
                .andRoute(GET("/auctions/my_auctions").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getAllAuctionsForUser)
                .andRoute(GET("/auctions/private/").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getPrivateAuctionsForUser)
                .andRoute(GET("/auctions/sell/").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getAllAuctionsForUserSeller)
                .andRoute(DELETE("/auctions/{id}").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::delete)
                .andRoute(POST("/auctions/close/{id}").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::closeAuction)
                .andRoute(PATCH("/auctions/{id}").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::update)
                .andRoute(POST("/auctions").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::create)
                .andRoute(GET("/auctions").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::getActivePublicAuctions)
                .andRoute(POST("/auctions/invitation/{auctionId}").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::sendInvitationToUser)
                .andRoute(POST("/auctions/remove_user/{auctionId}").and(accept(MediaType.APPLICATION_JSON)), auctionHandler::removeUserFromAuction);
    }
}
