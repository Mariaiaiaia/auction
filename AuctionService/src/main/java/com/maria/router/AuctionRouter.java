package com.maria.router;

import com.maria.constant.AuctionServiceRouterConstants;
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
