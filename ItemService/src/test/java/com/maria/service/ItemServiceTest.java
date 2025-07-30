package com.maria.service;

import com.maria.ItemMicroserviceApplication;
import com.maria.config.TestContainerConfig;
import com.maria.constant.ItemServiceConstants;
import com.maria.constant.ItemServiceRouterConstants;
import com.maria.entity.Item;
import com.maria.exception.DatabaseOperationException;
import com.maria.repository.ItemRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@Testcontainers
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ItemMicroserviceApplication.class)
public class ItemServiceTest extends TestContainerConfig {
    @SpyBean
    private ItemRepository itemRepository;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private DatabaseClient databaseClient;
    @Value("${jwt.secret}")
    private String jwtSecret;
    @MockBean
    private S3Service s3Service;

    @BeforeEach
    void setupp() {
        itemRepository.deleteAll().block();
        databaseClient.sql("ALTER SEQUENCE item_id_seq RESTART WITH 1")
                .fetch().rowsUpdated().block();

        databaseClient.sql("""
                    INSERT INTO item (item_name, description, image, is_sold, auction_id, seller)
                    VALUES 
                    ('Laptop', 'Gaming laptop with RTX 3070', 'image1.jpg', FALSE, NULL, 1),
                    ('Phone', 'New smartphone with great camera', 'image2.jpg', FALSE, 1, 1),
                    ('Bike', 'Used mountain bike', 'image3.jpg', TRUE, 3, 1)
                    ON CONFLICT (id) DO NOTHING;
                """).fetch().rowsUpdated().block();

        Mockito.when(s3Service.uploadDataToS3(anyString(), anyString(), any()))
                .thenReturn(Mono.just("https://fake-url"));
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
    void getItem_ReturnsItem() {
        String testToken = generateToken(1L, "user");
        Long itemId = 1L;

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_ITEM, itemId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sellerId").isEqualTo(1);
    }

    @Test
    void getItem_ReturnInvalidFormatItemIdException() {
        String testToken = generateToken(1L, "user");
        String itemId = "abs";

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_ITEM, itemId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(ItemServiceConstants.EX_INVALID_ITEM_ID_FORMAT);
    }

    @Test
    void getItem_ReturnInvalidNegativeNumberItemIdException() {
        String testToken = generateToken(1L, "user");
        Long itemId = -1L;

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_ITEM, itemId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(ItemServiceConstants.EX_ITEM_ID_POSITIVE_NUMBER);
    }

    @Test
    void getItem_ReturnItemNotExistException() {
        String testToken = generateToken(1L, "user");
        Long itemId = 20L;

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_ITEM, itemId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(ItemServiceConstants.EX_ITEM_NOT_EXIST);
    }

    @Test
    void getItem_ReturnDatabaseOperationException() {
        String testToken = generateToken(1L, "user");
        Long itemId = 20L;

        doReturn(Mono.error(new DatabaseOperationException(ItemServiceConstants.EX_FAIL_GET_ITEM)))
                .when(itemRepository)
                .findById(any(Long.class));

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_ITEM, itemId)
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo(ItemServiceConstants.EX_FAIL_GET_ITEM);
    }

    @Test
    void createItem_ReturnItemDto() throws IOException {
        String token = generateToken(1L, "user");

        Path tempFile = Files.createTempFile("test-image", ".jpg");
        Files.write(tempFile, "test-image-content".getBytes());

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(tempFile.toFile()));
        bodyBuilder.part("itemName", "Test Item");
        bodyBuilder.part("itemDescription", "This is a test description");

        this.webTestClient
                .post()
                .uri(ItemServiceRouterConstants.CREATE_ITEM)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.itemName").isEqualTo("Test Item")
                .jsonPath("$.description").isEqualTo("This is a test description")
                .jsonPath("$.sellerId").isEqualTo(1);

        Files.deleteIfExists(tempFile);
    }

    @Test
    void createItem_ReturnDatabaseOperationException() throws IOException {
        String token = generateToken(1L, "user");

        Path tempFile = Files.createTempFile("test-image", ".jpg");
        Files.write(tempFile, "test-image-content".getBytes());

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new FileSystemResource(tempFile.toFile()));
        bodyBuilder.part("itemName", "Test Item");
        bodyBuilder.part("itemDescription", "This is a test description");

        doReturn(Mono.error(new DatabaseOperationException(ItemServiceConstants.EX_FAIL_GET_ITEM)))
                .when(itemRepository)
                .save(any(Item.class));

        this.webTestClient
                .post()
                .uri(ItemServiceRouterConstants.CREATE_ITEM)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo(ItemServiceConstants.EX_FAIL_CREATE_ITEM);

        Files.deleteIfExists(tempFile);
    }

    @Test
    void getSellerId_ReturnsItem() {
        Long itemId = 1L;
        Long sellerId = 1L;

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_SELLER_ID, itemId)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(sellerId);
    }

    @Test
    void getSellerId_ReturnItemNotExistException() {
        Long itemId = 10L;

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_SELLER_ID, itemId)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(ItemServiceConstants.EX_ITEM_NOT_EXIST);
    }

    @Test
    void getSellerId_ReturnDatabaseOperationException() {
        Long itemId = 1L;

        doReturn(Mono.error(new DatabaseOperationException(ItemServiceConstants.EX_FAIL_GET_ITEM)))
                .when(itemRepository)
                .findById(any(Long.class));

        this.webTestClient
                .get()
                .uri(ItemServiceRouterConstants.GET_SELLER_ID, itemId)
                .header("X-Internal-Service", "true")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo(ItemServiceConstants.EX_FAIL_GET_ITEM);
    }
}
