package com.maria.handler;

import com.maria.UserMicroserviceApplication;
import com.maria.config.TestContainerConfig;
import com.maria.constant.UserServiceConstants;
import com.maria.dto.UserRegistrationDTO;
import com.maria.dto.UserUpdateDTO;
import com.maria.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Testcontainers
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UserMicroserviceApplication.class)
public class UserHandlerIT extends TestContainerConfig {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private DatabaseClient databaseClient;
    @Value("${jwt.secret}")
    private String jwtSecret;
    private Long harryUserId;
    private Long ronUserId;
    private final Long notExistUserId = 5L;

    @BeforeEach
    void setup() {
        userRepository.deleteAll().block();

        databaseClient.sql("ALTER SEQUENCE usr_id_seq RESTART WITH 1")
                .fetch().rowsUpdated().block();

        databaseClient.sql("""
                    INSERT INTO usr (first_name, last_name, password, email, role)
                    VALUES 
                    ('Harry', 'Potter', 'expelliarmus', 'harryp@gmail.com', 'user'),
                    ('Ron', 'Weasley', 'scabbers123', 'ronw@gmail.com', 'user')
                    ON CONFLICT (id) DO NOTHING;
                """).fetch().rowsUpdated().block();

        this.harryUserId = databaseClient.sql("SELECT id FROM usr WHERE email = 'harryp@gmail.com'")
                .map(row -> row.get("id", Long.class))
                .first()
                .block();

        this.ronUserId = databaseClient.sql("SELECT id FROM usr WHERE email = 'ronw@gmail.com'")
                .map(row -> row.get("id", Long.class))
                .first()
                .block();
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
    void getInfo_ReturnUser() {
        String validUserId = harryUserId.toString();

        this.webTestClient
                .get()
                .uri("/users/{id}", validUserId)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(1)
                .jsonPath("$.firstName").isEqualTo("Harry")
                .jsonPath("$.lastName").isEqualTo("Potter")
                .jsonPath("$.email").isEqualTo("harryp@gmail.com");
    }

    @Test
    void getInfo_ReturnInvalidFormatUserIdException() {
        String invalidUserId = "abc";

        this.webTestClient
                .get()
                .uri("/users/{id}", invalidUserId)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.INVALID_USER_ID_FORMAT);
    }

    @Test
    void getInfo_ReturnInvalidNegativeNumberUserIdException() {
        String invalidUserId = "-1";

        this.webTestClient
                .get()
                .uri("/users/{id}", invalidUserId)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.USER_ID_POSITIVE_NUMBER);
    }

    @Test
    void getInfo_ReturnUserNotExistException() {
        String userId = notExistUserId.toString();

        this.webTestClient
                .get()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.NOT_EXIST);
    }

    @Test
    void findUserByEmail_ReturnUser() {
        String validUserEmail = "harryp@gmail.com";

        this.webTestClient
                .get()
                .uri("/users/find_user/{userEmail}", validUserEmail)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(harryUserId)
                .jsonPath("$.password").isEqualTo("expelliarmus")
                .jsonPath("$.role").isEqualTo("user")
                .jsonPath("$.email").isEqualTo("harryp@gmail.com");
    }

    @Test
    void findUserByEmail_ReturnInvalidEmailException() {
        String invalidUserEmail = "harryp@gmail";

        this.webTestClient
                .get()
                .uri("/users/find_user/{userEmail}", invalidUserEmail)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.INVALID_EMAIL_FORMAT);
    }

    @Test
    void findUserByEmail_ReturnUserNotExistException() {
        String notExistUserEmail = "harmonyg@gmail.com";

        this.webTestClient
                .get()
                .uri("/users/find_user/{userEmail}", notExistUserEmail)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.NOT_EXIST);
    }

    @Test
    void delete_ReturnOkStatus() {
        String validUserId = ronUserId.toString();

        this.webTestClient
                .delete()
                .uri("/users/{id}", validUserId)
                .header("Authorization", "Bearer " + generateToken(ronUserId, "user"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void delete_ReturnInvalidFormatUserIdException() {
        String invalidUserId = "abc";

        this.webTestClient
                .delete()
                .uri("/users/{id}", invalidUserId)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.INVALID_USER_ID_FORMAT);
    }

    @Test
    void delete_UserNotExistException() {
        String userId = notExistUserId.toString();

        this.webTestClient
                .delete()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.NOT_EXIST);
    }

    @Test
    void delete_UserNotAvailableException() {
        String userId = ronUserId.toString();

        this.webTestClient
                .delete()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.NOT_AVAILABLE);
    }

    @Test
    void register_Success_ReturnUser() {
        UserRegistrationDTO newUser = new UserRegistrationDTO("Hermione", "Granger", "alohomora", "hermioneg@gmail.com");

        this.webTestClient
                .post()
                .uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Hermione")
                .jsonPath("$.lastName").isEqualTo("Granger")
                .jsonPath("$.email").isEqualTo("hermioneg@gmail.com");
    }

    @Test
    void register_ReturnUserAlreadyExistsException() {
        UserRegistrationDTO existsUser = new UserRegistrationDTO("Harry", "Potter", "expelliarmus", "harryp@gmail.com");

        this.webTestClient
                .post()
                .uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(existsUser)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.EMAIL_EXISTS);
    }

    @Test
    void register_ReturnInvalidUserRegistrationDTOException() {
        UserRegistrationDTO invalidUser = new UserRegistrationDTO("", "", "alohomora", "hermioneg@gmail.com");

        this.webTestClient
                .post()
                .uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUser)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void update_ReturnUser() {
        String validUserId = harryUserId.toString();
        UserUpdateDTO validUserUpdateDTO = new UserUpdateDTO("Ron", null, null);

        this.webTestClient
                .patch()
                .uri("/users/{id}", validUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserUpdateDTO)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(1)
                .jsonPath("$.firstName").isEqualTo("Ron")
                .jsonPath("$.lastName").isEqualTo("Potter")
                .jsonPath("$.email").isEqualTo("harryp@gmail.com");
    }

    @Test
    void update_ReturnInvalidUpdateDTOException() {
        String validUserId = harryUserId.toString();
        UserUpdateDTO validUserUpdateDTO = new UserUpdateDTO(null, null, "1111");

        this.webTestClient
                .patch()
                .uri("/users/{id}", validUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserUpdateDTO)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void update_ReturnInvalidIdException() {
        String validUserId = "abc";
        UserUpdateDTO validUserUpdateDTO = new UserUpdateDTO("Ron", null, null);

        this.webTestClient
                .patch()
                .uri("/users/{id}", validUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserUpdateDTO)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.INVALID_USER_ID_FORMAT);
    }

    @Test
    void update_ReturnUserNotAvailableException() {
        String validUserId = ronUserId.toString();
        UserUpdateDTO validUserUpdateDTO = new UserUpdateDTO("Ron", null, null);

        this.webTestClient
                .patch()
                .uri("/users/{id}", validUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserUpdateDTO)
                .header("Authorization", "Bearer " + generateToken(harryUserId, "user"))
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.NOT_AVAILABLE);
    }

    @Test
    void update_ReturnUserNotExistException() {
        String validUserId = notExistUserId.toString();
        UserUpdateDTO validUserUpdateDTO = new UserUpdateDTO("Ron", null, null);

        this.webTestClient
                .patch()
                .uri("/users/{id}", validUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserUpdateDTO)
                .header("Authorization", "Bearer " + generateToken(notExistUserId, "user"))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo(UserServiceConstants.NOT_EXIST);
    }
}


