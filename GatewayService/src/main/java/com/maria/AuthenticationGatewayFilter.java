package com.maria;

import com.maria.core.security.SecurityContextRepository;
import com.maria.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthenticationGatewayFilter implements GatewayFilter {
    private final SecurityContextRepository securityContextRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String key = "token:" + token;

            return redisTemplate.opsForValue().get(key)
                    .flatMap(isInvalidToken -> Mono.error(new UnauthorizedException("Token is invalid")))
                    .switchIfEmpty(Mono.defer(() -> {
                        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                                .header(HttpHeaders.AUTHORIZATION, authHeader)
                                .build();
                        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                        return chain.filter(newExchange);
                    })).then();
        }

        return Mono.error(new UnauthorizedException("Authorization header is missing"));
    }
}






