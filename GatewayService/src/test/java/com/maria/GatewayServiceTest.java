package com.maria;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GatewayServiceTest {
    @Autowired
    private WebTestClient gatewayWebTestClient;
    private String jwtSecret = "9be5fa455defebc4c61d0d9c84cf87928741050cb1e6ff9278912648e9d5419d";

    @BeforeAll
    void setUp() {
        gatewayWebTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8081")
                .build();
    }

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
    void getInfoAuctionWithToken_ReturnsAuction() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 1L;

        gatewayWebTestClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.auctionId").isEqualTo(1);
    }

    @Test
    void getInfoAuctionWithoutToken_ReturnsUnauthorized() {
        Long auctionId = 1L;

        gatewayWebTestClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .exchange()
                .expectStatus().isUnauthorized();
        /*
                .expectBody()
                .consumeWith(response -> {
                    byte[] content = response.getResponseBody();
                    if (content != null) {
                        System.out.println("Response body: " + new String(content));
                    } else {
                        System.out.println("Response body is null");
                    }
                });

         */
    }

    @Test
    void loginWithoutToken_SuccessReturnsToken() {
        Map<String, Object> requestBody = Map.of(
                "email", "harryp@gmail.com",
                "password", "expelliarmus"
        );

        gatewayWebTestClient.post()
                .uri("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").exists()
                .jsonPath("$.userId").isEqualTo(1);
    }

    @Test
    void register_Success_ReturnUser() {
        Map<String, Object> requestBody = Map.of(
                "firstName", "Draco",
                "lastName", "Malfoy",
                "password", "12356",
                "email", "draco@gmail.com"
        );

        gatewayWebTestClient
                .post()
                .uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Draco")
                .jsonPath("$.lastName").isEqualTo("Malfoy")
                .jsonPath("$.email").isEqualTo("draco@gmail.com");
    }

    @Test
    void getItemWithoutToken_ReturnsUnauthorized() {
        Long itemId = 1L;

        gatewayWebTestClient
                .get()
                .uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void logout_SuccessReturnsUnauthorizedInRequestToAuction() {
        String testToken = generateToken(1L, "user");

        gatewayWebTestClient.post()
                .uri("/api/logout")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk();

        gatewayWebTestClient
                .get()
                .uri("/auctions/{id}", 1L)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
