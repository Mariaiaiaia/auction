package com.maria.handler;

import com.maria.constant.BidServiceConstants;
import com.maria.constant.BidServiceRouterConstants;
import com.maria.dto.PlaceBidRequest;
import com.maria.entity.Bid;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("api-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BidHandlerTest {
    @Autowired
    private WebTestClient bidWebTestClient;
    @Autowired
    private DatabaseClient databaseClient;
    private String jwtSecret = "9be5fa455defebc4c61d0d9c84cf87928741050cb1e6ff9278912648e9d5419d";

    @BeforeAll
    void setUp() {
        bidWebTestClient = WebTestClient.bindToServer()
                .responseTimeout(Duration.ofSeconds(120))
                .baseUrl("http://localhost:8087")
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
    void getAllBidsForUser_ReturnsBids() {
        String testToken = generateToken(1L, "user");

        bidWebTestClient
                .get()
                .uri(BidServiceRouterConstants.USER_BIDS)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Bid.class)
                .value(bids -> {
                    assertThat(bids).hasSize(3);
                    assertThat(bids).extracting("bidId").containsExactlyInAnyOrder(1L, 7L, 8L);
                });
    }

    @Test
    void getAllBidsForUser_ReturnsEmptyList() {
        String testToken = generateToken(99L, "user");

        bidWebTestClient
                .get()
                .uri(BidServiceRouterConstants.USER_BIDS)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertEquals("[]", body.trim());
                });
    }

    @Test
    void placeBid_SuccessReturnsMessage() {
        String testToken = generateToken(3L, "user");
        PlaceBidRequest placeBidRequest = new PlaceBidRequest(BigDecimal.valueOf(250L), 2L);

        bidWebTestClient
                .post()
                .uri(BidServiceRouterConstants.NEW_BID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(placeBidRequest)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(BidServiceConstants.RESPONSE_BID_PLACED);
    }

    @Test
    void placeBid_ReturnsBidExistsException() {
        String testToken = generateToken(1L, "user");
        PlaceBidRequest placeBidRequest = new PlaceBidRequest(BigDecimal.valueOf(600L), 8L);

        bidWebTestClient
                .post()
                .uri(BidServiceRouterConstants.NEW_BID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(placeBidRequest)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(BidServiceConstants.EX_BID_EXISTS);
    }

    @Test
    void placeBid_ReturnsBidValidationException() {
        String testToken = generateToken(1L, "user");
        PlaceBidRequest placeBidRequest = new PlaceBidRequest(BigDecimal.valueOf(600L), null);

        bidWebTestClient
                .post()
                .uri(BidServiceRouterConstants.NEW_BID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(placeBidRequest)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(BidServiceConstants.VALIDATION_AUCTION_ID_REQ);
    }

    @Test
    void getBiddersIdFromAuction_ReturnsListUserIds() {
        bidWebTestClient
                .get()
                .uri(BidServiceRouterConstants.GET_BIDDERS_ID, 8L)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Long.class)
                .value(userIds -> {
                    assertThat(userIds).hasSize(3);
                    assertThat(userIds).containsExactlyInAnyOrder(1L, 2L, 4L);
                });
    }

    @Test
    void getBiddersIdFromAuction_ReturnsEmptyList() {
        bidWebTestClient
                .get()
                .uri(BidServiceRouterConstants.GET_BIDDERS_ID, 99L)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertEquals("[]", body.trim());
                });
    }

    @Test
    void getAllBidsForAuction_ReturnsAuctionBids() {
        String testToken = generateToken(1L, "user");

        bidWebTestClient
                .get()
                .uri(BidServiceRouterConstants.ALL_BIDS_FOR_AUCTION, 4L)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Bid.class)
                .value(bids -> {
                    assertThat(bids).hasSize(3);
                    assertThat(bids).extracting("bidId").containsExactlyInAnyOrder(2L, 3L, 4L);
                });
    }

    @Test
    void getAllBidsForAuction_UserNotSellerReturnsException() {
        String testToken = generateToken(2L, "user");

        bidWebTestClient
                .get()
                .uri(BidServiceRouterConstants.ALL_BIDS_FOR_AUCTION, 4L)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(BidServiceConstants.EX_BIDS_AVAILABLE);
    }

    @Test
    void getAllBidsForAuction_NoBidsReturnsEmptyList() {
        String testToken = generateToken(1L, "user");

        bidWebTestClient
                .get()
                .uri(BidServiceRouterConstants.ALL_BIDS_FOR_AUCTION, 1L)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertEquals("[]", body.trim());
                });
    }
}
