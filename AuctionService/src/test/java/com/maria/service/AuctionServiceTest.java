package com.maria.service;

import com.maria.AuctionServiceApplication;
import com.maria.core.entity.*;
import com.maria.entity.Auction;
import com.maria.mapper.AuctionMapper;
import com.maria.repository.AuctionRepository;
import com.redis.testcontainers.RedisContainer;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ActiveProfiles("integration-test")
@Testcontainers
@SpringBootTest(classes = AuctionServiceApplication.class)
public class AuctionServiceTest {
    private ReactiveKafkaProducerTemplate<String, NewBitEvent> bidProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, AcceptanceEvent> acceptanceProducerTemplate;
    @Autowired
    private ReactiveSetOperations<String, String> setOperations;
    @Autowired
    private ReactiveRedisTemplate<String, AuctionDTO> auctionRedisTemplate;
    @Autowired
    private ReactiveValueOperations<String, AuctionDTO> valueOperationsAuction;
    @Autowired
    private DatabaseClient databaseClient;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private AuctionMapper auctionMapper;
    @Autowired
    private AuctionService auctionService;
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    public <T> ReactiveKafkaProducerTemplate<String, T> createReactiveKafkaProducerTemplate() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(producerProps));
    }

    public <T> ReactiveKafkaConsumerTemplate<String, T> createReactiveKafkaConsumerTemplate(String topic, Class<T> targetType, String groupId) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonDeserializer");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

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

    @Container
    private static final RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);

    @BeforeEach
    private void setup() {
        this.bidProducerTemplate = createReactiveKafkaProducerTemplate();
        this.acceptanceProducerTemplate = createReactiveKafkaProducerTemplate();

        auctionRepository.deleteAll().block();

        databaseClient.sql("ALTER SEQUENCE auction_id_seq RESTART WITH 1")
                .fetch().rowsUpdated().block();

        databaseClient.sql("""
                    INSERT INTO auction (item, seller, starting_price, current_price, bidder, start_date, end_date, finished, public_access)
                    VALUES
                    (101, 1, 100.00, 120.00, 2, '2025-04-15 10:00:00', '2025-05-15 10:00:00', false, false),
                    (102, 2, 150.00, 150.00, NULL, '2025-04-14 09:00:00', '2025-06-18 15:00:00', false, true),
                    (103, 1, 200.00, 200.00, NULL, NOW() + INTERVAL '30 minutes', NOW() + INTERVAL '55 minutes', false, false),
                    (104, 1, 300.00, 350.00, 5, '2025-04-12 15:00:00', '2025-06-18 15:00:00', false, true),
                    (105, 2, 80.00, 90.00, 6, '2025-04-15 12:00:00', '2025-06-23 18:00:00', false, false);
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
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @PostConstruct
    public void initializeDatabase() {
        String sql = """
                    CREATE SCHEMA IF NOT EXISTS auction;
                    
                    CREATE TABLE IF NOT EXISTS auction (
                        id SERIAL PRIMARY KEY,
                        item BIGINT NOT NULL,
                        seller BIGINT NOT NULL,
                        starting_price DECIMAL(19,2) NOT NULL,
                        current_price DECIMAL(19,2),
                        bidder BIGINT,
                        start_date TIMESTAMP NOT NULL,
                        end_date TIMESTAMP NOT NULL,
                        finished BOOLEAN NOT NULL,
                        public_access BOOLEAN
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
        redisContainer.start();
    }

    @AfterAll
    static void stopKafka() {
        kafkaContainer.stop();
        redisContainer.stop();
    }

    @Test
    void updateHighestBid_SuccessCurrentPriceUpdates() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(false)
                .publicAccess(true)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();


        String key = "auctions:" + auctionId;
        valueOperationsAuction.set(key, auctionMapper.toDto(auction)).block();

        BigDecimal bidValue = BigDecimal.valueOf(200L);
        NewBitEvent newBitEvent = NewBitEvent
                .builder()
                .bidId(1L)
                .auctionId(auctionId)
                .bidAmount(bidValue)
                .bidderId(1L)
                .build();

        bidProducerTemplate.send("new-bid-events", newBitEvent.getAuctionId().toString(), newBitEvent).block();

        Mono.delay(Duration.ofSeconds(10)).block();

        AuctionDTO redisUpdatedAuction = valueOperationsAuction.get(key).block();
        Assertions.assertEquals(0, redisUpdatedAuction.getCurrentPrice().compareTo(bidValue));

        StepVerifier.create(auctionRepository.findById(auctionId))
                .assertNext(repoUpdatedAuction -> {
                    Assertions.assertEquals(0, repoUpdatedAuction.getCurrentPrice().compareTo(bidValue));
                })
                .expectComplete()
                .verify();
    }

    @Test
    void updateHighestBid_smallBidPriceNotUpdated() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(false)
                .publicAccess(true)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();
        BigDecimal startingPrice = auction.getStartingPrice();

        String key = "auctions:" + auctionId;
        valueOperationsAuction.set(key, auctionMapper.toDto(auction)).block();

        BigDecimal bidValue = BigDecimal.valueOf(20L);
        NewBitEvent newBitEvent = NewBitEvent
                .builder()
                .bidId(1L)
                .auctionId(auctionId)
                .bidAmount(bidValue)
                .bidderId(1L)
                .build();

        bidProducerTemplate.send("new-bid-events", newBitEvent.getAuctionId().toString(), newBitEvent).block();

        Mono.delay(Duration.ofSeconds(15)).block();

        AuctionDTO redisUpdatedAuction = valueOperationsAuction.get(key).block();
        Assertions.assertEquals(0, redisUpdatedAuction.getCurrentPrice().compareTo(startingPrice));

        StepVerifier.create(auctionRepository.findById(auction.getAuctionId()))
                .assertNext(repoUpdatedAuction -> {
                    Assertions.assertEquals(0, repoUpdatedAuction.getCurrentPrice().compareTo(startingPrice));
                })
                .expectComplete()
                .verify();
    }

    @Test
    void updateHighestBid_bidderIsSellerPriceNotUpdated() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(false)
                .publicAccess(true)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();
        BigDecimal startingPrice = auction.getStartingPrice();

        String key = "auctions:" + auctionId;
        valueOperationsAuction.set(key, auctionMapper.toDto(auction)).block();

        BigDecimal bidValue = BigDecimal.valueOf(200L);
        NewBitEvent newBitEvent = NewBitEvent
                .builder()
                .bidId(1L)
                .auctionId(auctionId)
                .bidAmount(bidValue)
                .bidderId(2L)
                .build();

        bidProducerTemplate.send("new-bid-events", newBitEvent.getAuctionId().toString(), newBitEvent).block();

        Mono.delay(Duration.ofSeconds(15)).block();

        AuctionDTO redisUpdatedAuction = valueOperationsAuction.get(key).block();
        Assertions.assertEquals(0, redisUpdatedAuction.getCurrentPrice().compareTo(startingPrice));

        StepVerifier.create(auctionRepository.findById(auction.getAuctionId()))
                .assertNext(repoUpdatedAuction ->
                        Assertions.assertEquals(0, repoUpdatedAuction.getCurrentPrice().compareTo(startingPrice)))
                .expectComplete()
                .verify();
    }

    @Test
    void updateHighestBid_bidAfterEndTime() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(40))
                .endDate(LocalDateTime.now().minusMinutes(1))
                .sellerId(2L)
                .finished(false)
                .publicAccess(true)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();
        BigDecimal startingPrice = auction.getStartingPrice();

        String key = "auctions:" + auctionId;
        valueOperationsAuction.set(key, auctionMapper.toDto(auction)).block();

        BigDecimal bidValue = BigDecimal.valueOf(200L);
        NewBitEvent newBitEvent = NewBitEvent
                .builder()
                .bidId(1L)
                .auctionId(auctionId)
                .bidAmount(bidValue)
                .bidderId(1L)
                .build();

        bidProducerTemplate.send("new-bid-events", newBitEvent.getAuctionId().toString(), newBitEvent).block();

        Mono.delay(Duration.ofSeconds(15)).block();

        AuctionDTO redisUpdatedAuction = valueOperationsAuction.get(key).block();
        Assertions.assertEquals(0, redisUpdatedAuction.getCurrentPrice().compareTo(startingPrice));

        StepVerifier.create(auctionRepository.findById(auction.getAuctionId()))
                .assertNext(repoUpdatedAuction ->
                        Assertions.assertEquals(0, repoUpdatedAuction.getCurrentPrice().compareTo(startingPrice)))
                .expectComplete()
                .verify();
    }

    @Test
    void processAcceptanceEvent_SuccessUserAddedToAuction() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(false)
                .publicAccess(false)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();

        String key = "auction:" + auctionId + ":users";

        AcceptanceEvent acceptanceEvent = AcceptanceEvent
                .builder()
                .auctionId(auctionId)
                .userId(1L)
                .acceptance(true)
                .build();

        acceptanceProducerTemplate.send("acceptance-events", acceptanceEvent).block();
        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(setOperations.isMember(key, String.valueOf(1L)))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void processAcceptanceEvent_auctionNotExistsUserNotAdded() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(false)
                .publicAccess(false)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();

        String key = "auction:" + auctionId + ":users";

        AcceptanceEvent acceptanceEvent = AcceptanceEvent
                .builder()
                .auctionId(99L)
                .userId(3L)
                .acceptance(true)
                .build();

        acceptanceProducerTemplate.send("acceptance-events", acceptanceEvent).block();
        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(setOperations.isMember(key, String.valueOf(3L)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void processAcceptanceEvent_auctionFinishedUserNotAdded() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(true)
                .publicAccess(false)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();

        String key = "auction:" + auctionId + ":users";

        AcceptanceEvent acceptanceEvent = AcceptanceEvent
                .builder()
                .auctionId(auctionId)
                .userId(3L)
                .acceptance(true)
                .build();

        acceptanceProducerTemplate.send("acceptance-events", acceptanceEvent).block();
        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(setOperations.isMember(key, String.valueOf(3L)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void processAcceptanceEvent_acceptanceIsFalseUserNotAdded() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(false)
                .publicAccess(false)
                .build();

        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();

        String key = "auction:" + auctionId + ":users";

        AcceptanceEvent acceptanceEvent = AcceptanceEvent
                .builder()
                .auctionId(auctionId)
                .userId(3L)
                .acceptance(false)
                .build();

        acceptanceProducerTemplate.send("acceptance-events", acceptanceEvent).block();
        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(setOperations.isMember(key, String.valueOf(3L)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getAllActivePrivateAuctions_shouldReturnOnlyPrivateOne() {
        Auction auction = Auction.builder()
                .itemId(106L)
                .currentPrice(BigDecimal.valueOf(100))
                .startingPrice(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusMinutes(10))
                .endDate(LocalDateTime.now().plusMinutes(50))
                .sellerId(2L)
                .finished(false)
                .publicAccess(false)
                .build();

        Long userId = 1L;
        auction = auctionRepository.save(auction).block();
        assert auction != null;
        Long auctionId = auction.getAuctionId();

        String key = "auction:" + auctionId + ":users";
        setOperations.add(key, String.valueOf(userId)).block();

        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(auctionService.getAllActivePrivateAuctions(userId))
                .assertNext(auctionDTO -> Assertions.assertEquals(0, auctionDTO.getAuctionId().compareTo(auctionId)))
                .expectComplete()
                .verify();
    }
}


