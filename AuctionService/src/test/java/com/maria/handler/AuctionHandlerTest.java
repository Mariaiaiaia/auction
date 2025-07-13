package com.maria.handler;

import com.maria.constant.AuctionServiceConstants;
import com.maria.core.entity.AuctionDTO;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActiveProfiles("api-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuctionHandlerTest {
    @Autowired
    private WebTestClient auctionWebTestClient;
    @Autowired
    private DatabaseClient databaseClient;
    private String jwtSecret = "9be5fa455defebc4c61d0d9c84cf87928741050cb1e6ff9278912648e9d5419d";


    @BeforeAll
    void setUp() {
        auctionWebTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8084")
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
    void getInfo_ReturnsAuction() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 1L;

        auctionWebTestClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.auctionId").isEqualTo(1);
    }

    @Test
    void getInfo_ReturnsInvalidIdException() {
        String testToken = generateToken(1L, "user");
        String auctionId = "abc";

        auctionWebTestClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_INVALID_ID_FORMAT);
    }

    @Test
    void getInfo_ReturnsAuctionNotExistException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 111L;

        auctionWebTestClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_EXIST);
    }

    @Test
    void getInfo_ReturnsPrivateAuctionForSeller() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 3L;

        auctionWebTestClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.auctionId").isEqualTo(3)
                .jsonPath("$.publicAccess").isEqualTo(false);
    }

    @Test
    void getInfo_ReturnsPrivateAuctionUserNotParticipant() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 5L;

        auctionWebTestClient
                .get()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_USER_NOT_PARTICIPANT);
    }

    @Test
    void create_SuccessReturnsAuctionDTO() {
        String testToken = generateToken(1L, "user");
        CreateAuctionRequestDTO auctionRequestDTO = new CreateAuctionRequestDTO(1L, BigDecimal.valueOf(100.0),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), false);

        auctionWebTestClient
                .post()
                .uri("/auctions")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.itemId").isEqualTo(1)
                .jsonPath("$.startingPrice").isEqualTo(100)
                .jsonPath("$.sellerId").isEqualTo(1);
    }

    @Test
    void create_ReturnsDataForAuctionIsNotValidException() {
        String testToken = generateToken(2L, "user");
        CreateAuctionRequestDTO auctionRequestDTO = new CreateAuctionRequestDTO(1L, BigDecimal.valueOf(100.0),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), true);

        auctionWebTestClient
                .post()
                .uri("/auctions")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionRequestDTO)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_ITEM_NOT_BELONG_TO_USER);
    }

    @Test
    void create_ReturnsAuctionNotAvailableException() {
        String testToken = generateToken(2L, "user");
        CreateAuctionRequestDTO auctionRequestDTO = new CreateAuctionRequestDTO(2L, BigDecimal.valueOf(100.0),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), true);

        auctionWebTestClient
                .post()
                .uri("/auctions")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionRequestDTO)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_ITEM_NOT_BELONG_TO_USER);
    }

    @Test
    void create_ReturnsServiceError() {
        String testToken = generateToken(2L, "user");
        CreateAuctionRequestDTO auctionRequestDTO = new CreateAuctionRequestDTO(20L, BigDecimal.valueOf(100.0),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), true);

        auctionWebTestClient
                .post()
                .uri("/auctions")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionRequestDTO)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_SERVICE_ERROR);
    }

    @Test
    void create_ReturnsTimeIncorrectValidationException() {
        String testToken = generateToken(2L, "user");
        CreateAuctionRequestDTO auctionRequestDTO = new CreateAuctionRequestDTO(20L, BigDecimal.valueOf(100.0),
                LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(1), true);

        auctionWebTestClient
                .post()
                .uri("/auctions")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionRequestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.VALIDATION_START_DATE);
    }

    @Test
    void create_ReturnsItemRequiredValidationException() {
        String testToken = generateToken(2L, "user");
        CreateAuctionRequestDTO auctionRequestDTO = new CreateAuctionRequestDTO(null, BigDecimal.valueOf(100.0),
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), true);

        auctionWebTestClient
                .post()
                .uri("/auctions")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionRequestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.VALIDATION_ITEM_REQ);
    }

    @Test
    void closeAuction_SuccessReturnsAuctionDTO() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 1L;

        auctionWebTestClient
                .post()
                .uri("/auctions/close/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.auctionId").isEqualTo(1);
    }

    @Test
    void closeAuction_ReturnsAuctionNotExistException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 22L;

        auctionWebTestClient
                .post()
                .uri("/auctions/close/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_EXIST);
    }

    @Test
    void closeAuction_ReturnsAuctionNotAvailableException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 2L;

        auctionWebTestClient
                .post()
                .uri("/auctions/close/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_AVAILABLE);
    }

    @Test
    void delete_SuccessReturnsString() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 7L;

        auctionWebTestClient
                .delete()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(AuctionServiceConstants.AUCTION_DELETED);
    }

    @Test
    void delete_ReturnsAuctionNotExistException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 100L;

        auctionWebTestClient
                .delete()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_EXIST);
    }

    @Test
    void delete_ReturnsAuctionNotAvailableException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 5L;

        auctionWebTestClient
                .delete()
                .uri("/auctions/{id}", auctionId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_AVAILABLE);
    }

    @Test
    void update_SuccessReturnsUpdatedAuctionDTO() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 3L;
        AuctionUpdateDTO auctionUpdateDTO = new AuctionUpdateDTO(BigDecimal.valueOf(100), null, null, null);

        auctionWebTestClient
                .patch()
                .uri("/auctions/{id}", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionUpdateDTO)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.auctionId").isEqualTo(3)
                .jsonPath("$.startingPrice").isEqualTo(100);
    }

    @Test
    void update_ReturnsAuctionStatusException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 4L;
        AuctionUpdateDTO auctionUpdateDTO = new AuctionUpdateDTO(BigDecimal.valueOf(100), true, null, null);

        auctionWebTestClient
                .patch()
                .uri("/auctions/{id}", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionUpdateDTO)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_ALREADY_STARTED);
    }

    @Test
    void update_ReturnsAuctionNotExistException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 44L;
        AuctionUpdateDTO auctionUpdateDTO = new AuctionUpdateDTO(BigDecimal.valueOf(100), true, null, null);

        auctionWebTestClient
                .patch()
                .uri("/auctions/{id}", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionUpdateDTO)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_EXIST);
    }

    @Test
    void update_ReturnsAuctionNotAvailableException() {
        String testToken = generateToken(2L, "user");
        Long auctionId = 3L;
        AuctionUpdateDTO auctionUpdateDTO = new AuctionUpdateDTO(BigDecimal.valueOf(100), true, null, null);

        auctionWebTestClient
                .patch()
                .uri("/auctions/{id}", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionUpdateDTO)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_AVAILABLE);
    }

    @Test
    void update_ReturnsException() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 3L;
        AuctionUpdateDTO auctionUpdateDTO = new AuctionUpdateDTO(null, null, LocalDateTime.now().minusHours(2), null);

        auctionWebTestClient
                .patch()
                .uri("/auctions/{id}", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auctionUpdateDTO)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.VALIDATION_START_DATE);
    }

    @Test
    void getAllAuctionsForUser_SuccessReturnsAuctions() {
        String testToken = generateToken(2L, "user");

        auctionWebTestClient
                .get()
                .uri("/auctions/my_auctions/")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuctionDTO.class)
                .value(auctions -> {
                    assertThat(auctions).hasSize(1);
                    assertThat(auctions).extracting("auctionId").containsExactlyInAnyOrder( 4L);
                });
    }

    @Test
    void getSellerId_SuccessReturnsUserId() {
        String testToken = generateToken(1L, "user");
        Long auctionId = 8L;

        auctionWebTestClient
                .get()
                .uri("/auctions/get_seller/{id}", auctionId)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("2");
    }

    @Test
    void getSellerId_ReturnsAuctionNotExistException() {
        Long auctionId = 100L;

        auctionWebTestClient
                .get()
                .uri("/auctions/get_seller/{id}", auctionId)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(AuctionServiceConstants.EX_AUCTION_NOT_EXIST);
    }

    @Test
    void getActivePublicAuctions_SuccessReturnsAuctions() {
        String testToken = generateToken(2L, "user");

        auctionWebTestClient
                .get()
                .uri("/auctions")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuctionDTO.class)
                .value(auctions -> {
                    assertThat(auctions).hasSize(2);
                    assertThat(auctions).extracting("auctionId").containsExactlyInAnyOrder( 2L, 4L);
                });
    }

    @Test
    void getAllAuctionsForUserSeller_SuccessReturnsAuctions() {
        String testToken = generateToken(2L, "user");

        auctionWebTestClient
                .get()
                .uri("/auctions/sell/")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuctionDTO.class)
                .value(auctions -> {
                    assertThat(auctions).hasSize(3);
                    assertThat(auctions).extracting("auctionId").containsExactlyInAnyOrder(2L, 5L, 8L);
                });
    }
}
