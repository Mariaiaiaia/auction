package com.maria.config;

import com.maria.AuthenticationGatewayFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GatewayConfig {
    private final AuthenticationGatewayFilter authenticationGatewayFilter;
    @Value("${uri.item-service}")
    private String itemServiceUri;
    @Value("${uri.user-service}")
    private String userServiceUri;
    @Value("${uri.security-service}")
    private String securityServiceUri;
    @Value("${uri.auction-service}")
    private String auctionServiceUri;
    @Value("${uri.notification-service}")
    private String notificationServiceUri;
    @Value("${uri.bid-service}")
    private String bidServiceUri;
    @Value("${uri.invitation-service}")
    private String invitationServiceUri;
    @Value("${path.user.register}")
    private String userRegisterPath;
    @Value("${path.user.service}")
    private String userServicePath;
    @Value("${path.security.login}")
    private String loginPath;
    @Value("${path.security.logout}")
    private String logoutPath;
    @Value("${path.item.service}")
    private String itemServicePath;
    @Value("${path.auction.service}")
    private String auctionServicePath;
    @Value("${path.notification.service}")
    private String notificationServicePath;
    @Value("${path.bid.service}")
    private String bidServicePath;
    @Value("${path.invitation.service}")
    private String invitationServicePath;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("user-service-route", r -> r.path(userRegisterPath)
                        .uri(userServiceUri))
                .route("user-service-route", r -> r.path(userServicePath)
                        .filters(f -> f.filter(authenticationGatewayFilter))
                        .uri(userServiceUri))
                .route("security-service-route", r -> r.path(loginPath)
                        .uri(securityServiceUri))
                .route("security-service-route", r -> r.path(logoutPath)
                        .filters(f -> f.filter(authenticationGatewayFilter))
                        .uri(securityServiceUri))
                .route("item-service-route", r -> r.path(itemServicePath)
                        .filters(f -> f.filter(authenticationGatewayFilter))
                        .uri(itemServiceUri))
                .route("auction-service-route", r -> r.path(auctionServicePath)
                        .filters(f -> f.filter(authenticationGatewayFilter))
                        .uri(auctionServiceUri))
                .route("notification-service-route", r -> r.path(notificationServicePath)
                        .filters(f -> f.filter(authenticationGatewayFilter))
                        .uri(notificationServiceUri))
                .route("bid-service-route", r -> r.path(bidServicePath)
                        .filters(f -> f.filter(authenticationGatewayFilter))
                        .uri(bidServiceUri))
                .route("invitation-service-route", r -> r.path(invitationServicePath)
                        .filters(f -> f.filter(authenticationGatewayFilter))
                        .uri(invitationServiceUri))
                .build();
    }
}




