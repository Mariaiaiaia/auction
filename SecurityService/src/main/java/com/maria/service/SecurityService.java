package com.maria.service;

import com.maria.entity.TokenDetails;
import com.maria.core.entity.SharedUser;
import com.maria.dto.AuthRequestDTO;
import com.maria.handler.AuthJwtHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class SecurityService {
    private final WebClient webClient;
    private final AuthJwtHandler jwtHandler;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public Mono<TokenDetails> login(AuthRequestDTO authRequestDTO){
        return webClient.get()
                .uri("users/find_user/{userEmail}", authRequestDTO.getEmail())
                .retrieve()
                .bodyToMono(SharedUser.class)
                .flatMap(authUser -> {
                    if (passwordEncoder.matches(authRequestDTO.getPassword(), authUser.getPassword())){
                        return jwtHandler.generateToken(authUser);
                    }else{
                        return Mono.error(new RuntimeException("invalid"));
                    }
                });
    }

    public Mono<Void> logout(ServerWebExchange exchange, Long userId){
        String token = Objects.requireNonNull(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .substring("Bearer ".length());
        String key = "token:" + token;

        return redisTemplate.opsForValue().set(key, userId.toString(), Duration.ofHours(1)).then();
    }
}
