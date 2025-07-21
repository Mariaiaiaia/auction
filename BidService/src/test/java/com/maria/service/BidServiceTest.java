package com.maria.service;

import com.maria.BidServiceApplication;
import com.maria.constant.BidServiceEventConstants;
import com.maria.core.entity.AuctionItemEvent;
import com.maria.core.entity.NewBitEvent;
import com.maria.dto.PlaceBidRequest;
import com.maria.repository.BidRepository;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("integration-test")
@Testcontainers
@SpringBootTest(classes = BidServiceApplication.class)
public class BidServiceTest {
    private ReactiveKafkaConsumerTemplate<String, NewBitEvent> newBitConsumerTemplate;
    private ReactiveKafkaProducerTemplate<String, AuctionItemEvent> auctionDeletedProducerTemplate;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private BidService bidService;
    @Autowired
    private DatabaseClient databaseClient;
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
        this.auctionDeletedProducerTemplate = createReactiveKafkaProducerTemplate();
        this.newBitConsumerTemplate = createReactiveKafkaConsumerTemplate
                (BidServiceEventConstants.NEW_BID, NewBitEvent.class, "bid-consumer-group");

        bidRepository.deleteAll().block();

        databaseClient.sql("ALTER SEQUENCE bid_id_seq RESTART WITH 1")
                .fetch().rowsUpdated().block();

        databaseClient.sql("""
                    INSERT INTO bid (user_id, auction_id, bid_amount) VALUES
                    (1, 8, 550.00),
                    (2, 4, 350.00),
                    (5, 4, 450.00),
                    (6, 4, 650.00),
                    (2, 8, 650.00),
                    (4, 8, 750.00),
                    (1, 8, 600.00);
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
                    
                    CREATE TABLE IF NOT EXISTS bid (
                          id SERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          auction_id BIGINT NOT NULL,
                          bid_amount DECIMAL(19, 2) NOT NULL
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

    @Test
    void newBid_SendsSuccessfullyToConsumer() {
        PlaceBidRequest placeBidRequest = new PlaceBidRequest(BigDecimal.valueOf(250L), 2L);
        bidService.placeBid(placeBidRequest, 1L);

        Mono.delay(Duration.ofSeconds(10)).block();

        StepVerifier.create(
                        newBitConsumerTemplate.receiveAutoAck()
                                .map(ConsumerRecord::value)
                                .take(1)
                )
                .then(() -> bidService.placeBid(placeBidRequest, 1L).block())
                .assertNext(event -> {
                    assertEquals(placeBidRequest.getAuctionId(), event.getAuctionId());
                    assertEquals(placeBidRequest.getBidAmount(), event.getBidAmount());
                })
                .verifyComplete();
    }

    @Test
    void auctionDeleted_DeleteBidsOfAuction() {
        AuctionItemEvent auctionItemEvent = new AuctionItemEvent(4L, 1L);

        auctionDeletedProducerTemplate.send(BidServiceEventConstants.DELETE_AUCTION, auctionItemEvent).block();

        StepVerifier.create(
                        Mono.delay(Duration.ofSeconds(10))
                                .thenMany(bidRepository.findByAuctionId(4L))
                )
                .expectNextCount(0)
                .verifyComplete();
    }
}
