package com.maria.handler;

import com.maria.dto.AuthRequestDTO;
import com.maria.dto.AuthResponseDTO;
import com.maria.service.SecurityService;
import com.maria.validator.SecurityServiceValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SecurityHandler {
    private final SecurityService service;
    private final SecurityServiceValidation securityServiceValidation;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(AuthRequestDTO.class)
                .flatMap(securityServiceValidation::validateAuthRequestDTO)
                .flatMap(service::login)
                .flatMap(tokenDetails -> {
                    AuthResponseDTO authResponse = AuthResponseDTO.builder()
                            .userId(tokenDetails.getUserId())
                            .token(tokenDetails.getToken())
                            .expiresAt(tokenDetails.getExpiresAt())
                            .issuedAt(tokenDetails.getIssuedAt())
                            .build();
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(authResponse);
                })
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication ->
                        service.logout(request.exchange(), Long.valueOf(authentication.getPrincipal().toString())))
                .flatMap(count -> ServerResponse.ok().build());
    }
}