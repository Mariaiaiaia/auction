package com.maria.core.security;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private JwtHandler jwtHandler;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        if(jwtHandler.verify(authToken)) {
            Claims claims = jwtHandler.getClaimsFromToken(authToken);
            Long userId = Long.valueOf(claims.getSubject());
            List<String> role = claims.get("role", List.class);
            List<SimpleGrantedAuthority> authorities = role.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            return Mono.just(new UsernamePasswordAuthenticationToken(userId, null, authorities));
        }else{
            return Mono.empty();
        }
    }
}
