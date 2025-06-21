package com.maria.handler;

import com.maria.constant.SecurityServiceConstants;
import com.maria.dto.AuthRequestDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityHandlerIT {
    private String jwtSecret = "9be5fa455defebc4c61d0d9c84cf87928741050cb1e6ff9278912648e9d5419d";

    public String generateToken(Long userId, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", List.of(userRole));

        return Jwts.builder()
                .addClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void login_ReturnsValidationException() {
        AuthRequestDTO authRequest = new AuthRequestDTO("harryp@gmail.com", "");

        WebTestClient securityWebClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8083")
                .build();

        securityWebClient.post()
                .uri("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void login_ReturnsPassNotMachException() {
        AuthRequestDTO authRequest = new AuthRequestDTO("harryp@gmail.com", "abracadabra");

        WebTestClient securityWebClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8083")
                .build();

        securityWebClient.post()
                .uri("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo(SecurityServiceConstants.PASS_NOT_MATCH);
    }

    @Test
    void login_ReturnsTokenDetails_WhenUserExists() {
        AuthRequestDTO authRequest = new AuthRequestDTO("harryp@gmail.com", "expelliarmus");

        WebTestClient securityWebClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8083")
                .build();

        securityWebClient.post()
                .uri("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").exists()
                .jsonPath("$.userId").isEqualTo(1);
    }


    @Test
    void logout_ShouldStoreTokenInRedis() {
        String testToken = generateToken(1L, "user");

        WebTestClient securityWebClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8083")
                .build();

        securityWebClient.post()
                .uri("/api/logout")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk();
    }
}
