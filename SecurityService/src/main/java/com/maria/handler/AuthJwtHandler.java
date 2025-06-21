package com.maria.handler;

import com.maria.core.entity.SharedUser;
import com.maria.entity.TokenDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class AuthJwtHandler {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expiration;

    public Mono<TokenDetails> generateToken(SharedUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", List.of(user.getRole()));

        long expirationTimeInMillis = expiration * 1000L;
        Date createdDate = new Date();
        Date expirationDate = new Date(createdDate.getTime() + expirationTimeInMillis);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .addClaims(claims)
                .setSubject(user.getUserId().toString())
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();

        TokenDetails tokenDetails = TokenDetails.builder()
                .userId(user.getUserId())
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();

        return Mono.just(tokenDetails);
    }
}


