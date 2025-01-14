package com.maria.config;

import com.maria.AuthenticationGatewayFilter;
import com.maria.core.security.SecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@RequiredArgsConstructor
@Configuration
public class GatewayConfig {
    private final SecurityContextRepository securityContextRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("user-service-route", r -> r.path("/users/register")
                        .uri("http://localhost:8082"))
                .route("user-service-route", r -> r.path("/users/**")
                        .filters(f -> f.filter(new AuthenticationGatewayFilter(securityContextRepository, redisTemplate)))
                        .uri("http://localhost:8082"))
                .route("security-service-route", r -> r.path("/api/login")
                        .uri("http://localhost:8083"))
                .route("security-service-route", r -> r.path("/api/logout")
                        .filters(f -> f.filter(new AuthenticationGatewayFilter(securityContextRepository, redisTemplate)))
                        .uri("http://localhost:8083"))
                .route("item-service-route", r -> r.path("/items/**")
                        .filters(f -> f.filter(new AuthenticationGatewayFilter(securityContextRepository, redisTemplate)))
                        .uri("http://localhost:8085"))
                .route("auction-service-route", r -> r.path("/auctions/**")
                        .filters(f -> f.filter(new AuthenticationGatewayFilter(securityContextRepository, redisTemplate)))
                        .uri("http://localhost:8084"))
                .route("notification-service-route", r -> r.path("/notifications/**")
                        .filters(f -> f.filter(new AuthenticationGatewayFilter(securityContextRepository, redisTemplate)))
                        .uri("http://localhost:8086"))
                .route("bid-service-route", r -> r.path("/bids/**")
                        .filters(f -> f.filter(new AuthenticationGatewayFilter(securityContextRepository, redisTemplate)))
                        .uri("http://localhost:8087"))
                .route("invitation-service-route", r -> r.path("/invitations/**")
                        .filters(f -> f.filter(new AuthenticationGatewayFilter(securityContextRepository, redisTemplate)))
                        .uri("http://localhost:8088"))
                .build();
    }
}
