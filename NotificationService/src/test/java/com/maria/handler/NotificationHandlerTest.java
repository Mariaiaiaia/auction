package com.maria.handler;

import com.maria.constant.NotificationServiceConstants;
import com.maria.entity.Notification;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("api-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationHandlerTest {
    private String jwtSecret = "9be5fa455defebc4c61d0d9c84cf87928741050cb1e6ff9278912648e9d5419d";
    private WebTestClient notificationWebClient;
    @Autowired
    private ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory;

    @BeforeAll
    void setUp() {
        notificationWebClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8086")
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
    void getNotifications_ReturnsNoNotifications() {
        String testToken = generateToken(99L, "user");

        notificationWebClient.get()
                .uri("/notifications/")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(NotificationServiceConstants.NO_NOTIFICATIONS);
    }

    @Test
    void getNotifications_ReturnsNotifications() {
        String testToken = generateToken(3L, "user");

        notificationWebClient.get()
                .uri("/notifications/")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Notification.class)
                .value(notifications -> {
                    assertThat(notifications).hasSize(1);
                    assertThat(notifications.get(0).getUserId()).isEqualTo(3L);
                });
    }

    @Test
    void deleteNotification_ReturnsSuccess() {
        String testToken = generateToken(1L, "user");
        String notificationId = "65d7e3a9d3b7f53eb4e4e9b1";

        notificationWebClient.delete()
                .uri("/notifications/{id}", notificationId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(NotificationServiceConstants.SUCCESSFULLY_DELETED);

        notificationWebClient.delete()
                .uri("/notifications/{id}", notificationId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(NotificationServiceConstants.EX_NOTIFIC_NOT_EXIST);
    }

    @Test
    void deleteNotification_ReturnsNotificationNotExistError() {
        String testToken = generateToken(1L, "user");
        String notificationId = "65d7e3a9d3b7f53eb4e4e9b0";

        notificationWebClient.delete()
                .uri("/notifications/{id}", notificationId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(NotificationServiceConstants.EX_NOTIFIC_NOT_EXIST);
    }

    @Test
    void deleteNotification_ReturnsNotificationNotAvailableError() {
        String testToken = generateToken(2L, "user");
        String notificationId = "65d7e3a9d3b7f53eb4e4e9b2";

        notificationWebClient.delete()
                .uri("/notifications/{id}", notificationId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error").isEqualTo(NotificationServiceConstants.EX_NOTIFIC_NOT_AVAILABLE);
    }
}


