package com.maria.service;

import com.maria.InvitationServiceApplication;
import com.maria.constant.InvitationServiceConstants;
import com.maria.constant.InvitationServiceRouterConstants;
import com.maria.core.entity.AcceptanceEvent;
import com.maria.core.entity.InvitationEvent;
import com.maria.entity.AcceptanceRequest;
import com.maria.entity.Invitation;
import com.maria.repository.InvitationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;
import reactor.test.StepVerifier;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureWebTestClient
@Testcontainers
@SpringBootTest(classes = InvitationServiceApplication.class)
public class InvitationServiceTest {
    private ReactiveKafkaConsumerTemplate<String, InvitationEvent> invitationConsumerTemplate;
    private ReactiveKafkaProducerTemplate<String, AcceptanceEvent> acceptanceProducerTemplate;
    private ReactiveKafkaConsumerTemplate<String, AcceptanceEvent> acceptanceConsumerTemplate;
    private ReactiveKafkaProducerTemplate<String, InvitationEvent> invitationProducerTemplate;
    @Autowired
    private InvitationRepository invitationRepository;
    @Autowired
    private DatabaseClient databaseClient;
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Autowired
    private WebTestClient webTestClient;

    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    public <T> ReactiveKafkaProducerTemplate<String, T> createReactiveKafkaProducerTemplate() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(producerProps));
    }

    public <T> ReactiveKafkaConsumerTemplate<String, T> createReactiveKafkaConsumerTemplate(String topic, Class<T> targetType, String groupId) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonDeserializer");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());

        ReceiverOptions<String, T> receiverOptions = ReceiverOptions.<String, T>create(consumerProps)
                .subscription(Collections.singleton(topic));

        return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
    }

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("1234");

    @BeforeEach
    private void setup() {
        this.acceptanceProducerTemplate = createReactiveKafkaProducerTemplate();
        this.invitationProducerTemplate = createReactiveKafkaProducerTemplate();
        this.invitationConsumerTemplate = createReactiveKafkaConsumerTemplate("auction-invitations-events", InvitationEvent.class, "invitation-consumer-group");
        this.acceptanceConsumerTemplate = createReactiveKafkaConsumerTemplate("acceptance-events", AcceptanceEvent.class, "acceptance-consumer-group");

        invitationRepository.deleteAll().block();
        databaseClient.sql("ALTER SEQUENCE invitation_id_seq RESTART WITH 1")
                .fetch().rowsUpdated().block();

        databaseClient.sql("""
                    INSERT INTO invitation (auction, seller, usr, acceptance) VALUES
                    (101, 3, 1, true),
                    (101, 3, 2, false),
                    (103, 2, 1, NULL),
                    (104, 5, 2, true),
                    (105, 6, 2, false);
                """).fetch().rowsUpdated().block();
    }

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.r2dbc.url",
                () -> "r2dbc:postgresql://" + POSTGRESQL_CONTAINER.getHost() +
                        ":" + POSTGRESQL_CONTAINER.getMappedPort(5432) + "/testdb");
        registry.add("spring.r2dbc.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.r2dbc.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @PostConstruct
    public void initializeDatabase() {
        String sql = """
                    CREATE SCHEMA IF NOT EXISTS auction;
                    
                    CREATE TABLE IF NOT EXISTS invitation (
                          id SERIAL PRIMARY KEY,
                          auction BIGINT NOT NULL,
                          seller BIGINT NOT NULL,
                          usr BIGINT NOT NULL,
                          acceptance BOOLEAN
                      );
                """;

        databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .block();
    }

    @BeforeAll
    static void startKafka() {
        kafkaContainer.start();
    }

    @AfterAll
    static void stopKafka() {
        kafkaContainer.stop();
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
    void invitationsForUser_SuccessReturnInvitations() {
        this.webTestClient
                .get()
                .uri(InvitationServiceRouterConstants.INVITATIONS)
                .header("Authorization", "Bearer " + generateToken(1L, "user"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Invitation.class)
                .consumeWith(response -> {
                    List<Invitation> invitations = response.getResponseBody();
                    assertNotNull(invitations);
                    assertEquals(2, invitations.size());

                    assertTrue(invitations.stream().anyMatch(i ->
                            i.getInvitationId() == 1L &&
                                    i.getAuctionId() == 101L &&
                                    i.getSellerId() == 3L &&
                                    i.getUserId() == 1L &&
                                    Boolean.TRUE.equals(i.getAcceptance())
                    ));

                    assertTrue(invitations.stream().anyMatch(i ->
                            i.getInvitationId() == 3L &&
                                    i.getAuctionId() == 103L &&
                                    i.getSellerId() == 2L &&
                                    i.getUserId() == 1L &&
                                    i.getAcceptance() == null
                    ));
                });
    }

    @Test
    void invitationsForUser_NoInvitationsReturnsEmptyList() {
        this.webTestClient
                .get()
                .uri(InvitationServiceRouterConstants.INVITATIONS)
                .header("Authorization", "Bearer " + generateToken(99L, "user"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("[]");
    }

    @Test
    void respondToInvitation_SuccessInvitationUpdates() {
        AcceptanceRequest acceptanceRequest = new AcceptanceRequest(103L, true);

        Invitation invitationBefore = invitationRepository
                .findByUserIdAndAuctionId(1L, 103L)
                .block();

        assertNotNull(invitationBefore);
        assertNull(invitationBefore.getAcceptance());

        this.webTestClient
                .post()
                .uri(InvitationServiceRouterConstants.INVITATIONS_ACCEPTANCE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(acceptanceRequest)
                .header("Authorization", "Bearer " + generateToken(1L, "user"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(InvitationServiceConstants.RESPONSE_BEEN_SENT);

        StepVerifier.create(invitationRepository.findByUserIdAndAuctionId(1L, 103L))
                .expectNextMatches(invitation -> Boolean.TRUE.equals(invitation.getAcceptance()))
                .verifyComplete();

        Mono.delay(Duration.ofSeconds(5)).block();

        AcceptanceEvent acceptanceEvent = acceptanceConsumerTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .blockFirst();

        assertNotNull(acceptanceEvent);
        assertEquals(103L, acceptanceEvent.getAuctionId());
        assertEquals(1L, acceptanceEvent.getUserId());
        assertTrue(acceptanceEvent.isAcceptance());
    }

    @Test
    void respondToInvitation_ReturnInvitationNotExistException() {
        AcceptanceRequest acceptanceRequest = new AcceptanceRequest(99L, true);

        this.webTestClient
                .post()
                .uri(InvitationServiceRouterConstants.INVITATIONS_ACCEPTANCE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(acceptanceRequest)
                .header("Authorization", "Bearer " + generateToken(1L, "user"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(InvitationServiceConstants.EX_INVITATION_NOT_FOUND);
    }

    @Test
    void processInvitation_SuccessInvitationSaved() {
        InvitationEvent invitationEvent = new InvitationEvent(106L, 6L, 1L);

        invitationProducerTemplate.send("auction-invitations-events", invitationEvent).block();

        Mono.delay(Duration.ofSeconds(5)).block();

        StepVerifier.create(invitationRepository.findByUserIdAndAuctionId(invitationEvent.getUserId(), invitationEvent.getAuctionId()))
                .expectNextCount(1)
                .verifyComplete();
    }
}
