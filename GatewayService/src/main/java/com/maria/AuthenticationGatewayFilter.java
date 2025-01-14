package com.maria;

import com.maria.core.security.SecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.net.URI;


@RequiredArgsConstructor
@Component
public class AuthenticationGatewayFilter implements GatewayFilter {
    private final SecurityContextRepository securityContextRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return securityContextRepository.load(exchange)
                .flatMap(securityContext -> {
                    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String key = "token:" + token;

                        return redisTemplate.opsForValue().get(key)
                                .flatMap(isInvalidToken -> {
                                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return exchange.getResponse().setComplete();
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                                            .header(HttpHeaders.AUTHORIZATION, authHeader)
                                            .build();
                                    ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                                    return chain.filter(newExchange);
                                }));
                    }

                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().setLocation(URI.create("http://localhost:8083/api/login"));
                    return response.setComplete();
                });
    }
}
