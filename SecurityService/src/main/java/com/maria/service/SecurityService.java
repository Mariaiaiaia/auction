package com.maria.service;

import com.maria.constant.SecurityServiceConstants;
import com.maria.core.entity.SharedUser;
import com.maria.entity.TokenDetails;
import com.maria.dto.AuthRequestDTO;
import com.maria.exception.PasswordException;
import com.maria.handler.AuthJwtHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class SecurityService {
    private final WebClient webClient;
    private final AuthJwtHandler jwtHandler;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    @Value("${url.user-service-find-user}")
    private String userServiceFindUserUrl;

    public Mono<TokenDetails> login(AuthRequestDTO authRequestDTO) {
        return webClient
                .get()
                .uri(userServiceFindUserUrl, authRequestDTO.getEmail())
                .header("X-Internal-Service", "true")
                .retrieve()
                .bodyToMono(SharedUser.class)
                .flatMap(authUser -> {
                    if (passwordEncoder.matches(authRequestDTO.getPassword(), authUser.getPassword())) {
                        return jwtHandler.generateToken(authUser);
                    } else {
                        return Mono.error(new PasswordException(SecurityServiceConstants.PASS_NOT_MATCH));
                    }
                })
                .onErrorMap(ex -> {
                    log.error(SecurityServiceConstants.LOG_LOGIN_FAILED, ex.getMessage());
                    if (ex instanceof PasswordException) {
                        return ex;
                    }
                    return new Exception(SecurityServiceConstants.LOGIN_FAILED);
                });
    }

    public Mono<Void> logout(ServerWebExchange exchange, Long userId) {
        String token = Objects.requireNonNull(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .substring("Bearer ".length());
        String key = "token:" + token;

        return redisTemplate.opsForValue().set(key, userId.toString(), Duration.ofHours(1)).then();
    }
}
