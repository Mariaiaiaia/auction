package com.maria.service;

import com.maria.NotificationsServiceApplication;
import com.maria.constant.NotificationServiceConstants;
import com.maria.core.entity.*;
import com.maria.repository.NotificationRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderOptions;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = NotificationsServiceApplication.class)
@Testcontainers
public class NotificationKafkaServiceTest {
    @SpyBean
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;
    private ReactiveKafkaProducerTemplate<String, Notification> notificationProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, AuctionItemEvent> removeAuctionProducerTemplate;
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    public <T>ReactiveKafkaProducerTemplate<String, T> createReactiveKafkaProducerTemplate(){
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(producerProps));
    }

    @BeforeEach
    private void initialize() {
        this.notificationProducerTemplate = createReactiveKafkaProducerTemplate();
        this.removeAuctionProducerTemplate = createReactiveKafkaProducerTemplate();

        notificationRepository.deleteAll().block();
    }

    @DynamicPropertySource
    public static void kafkaProp(DynamicPropertyRegistry registry){
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeAll
    static void startKafka() {
        kafkaContainer.start();
    }

    @AfterAll
    static void stopKafka() {
        kafkaContainer.stop();
    }

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Test
    void whenNewBidNotificationEventIsConsumed_thenNotificationShouldBeSaved(){
        NewBidNotificationEvent notificationEvent = NewBidNotificationEvent
                .builder()
                .auctionId(4L)
                .newBid(BigDecimal.valueOf(200))
                .timestamp(LocalDateTime.now())
                .type(NotificationType.NEW_BID)
                .bidderId(1L)
                .build();
        Set<Long> expectedUserIds = Set.of(2L, 5L, 6L);

        notificationProducerTemplate.send("new-bid-notification-events", notificationEvent)
                .block();

        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(notificationRepository.findByAuctionId(notificationEvent.getAuctionId()).collectList())
                .assertNext(notifications -> {
                    assertEquals(3, notifications.size());

                    for (com.maria.entity.Notification notification : notifications) {
                        assertEquals(4L, notification.getAuctionId());
                        assertTrue(expectedUserIds.contains(notification.getUserId()));
                        assertTrue(notification.getMessage().contains(NotificationServiceConstants.MESSAGE_NEW_BID + notificationEvent.getNewBid()));
                    }
                })
                .expectComplete()
                .verify();
    }

    @Test
    void whenNewBidNotificationEventNoUserIdFound_thenNotificationShouldNotBeSaved(){
        NewBidNotificationEvent notificationEvent = NewBidNotificationEvent
                .builder()
                .auctionId(1L)
                .newBid(BigDecimal.valueOf(200))
                .timestamp(LocalDateTime.now())
                .type(NotificationType.NEW_BID)
                .bidderId(1L)
                .build();

        notificationProducerTemplate.send("new-bid-notification-events", notificationEvent)
                .block();

        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(notificationRepository.findByAuctionId(notificationEvent.getAuctionId()))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    void whenAuctionFinishedEventIsConsumed_thenNotificationShouldBeSaved() {
        AuctionFinishedNotificationEvent notificationEvent = AuctionFinishedNotificationEvent
                .builder()
                .auctionId(8L)
                .itemId(1L)
                .timestamp(LocalDateTime.now())
                .finalPrice(BigDecimal.valueOf(200))
                .build();
        Set<Long> expectedUserIds = Set.of(1L, 2L, 4L);

        notificationProducerTemplate.send("auction-finished-notification-events", notificationEvent)
                .block();

        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(notificationRepository.findByAuctionId(notificationEvent.getAuctionId()).collectList())
                .assertNext(notifications -> {
                    assertEquals(3, notifications.size());

                    for (com.maria.entity.Notification notification : notifications) {
                        assertEquals(8L, notification.getAuctionId());
                        assertTrue(expectedUserIds.contains(notification.getUserId()));
                        assertTrue(notification.getMessage().contains(NotificationServiceConstants.MESSAGE_AUCTION_FINISHED + notificationEvent.getAuctionId()));
                    }
                })
                .expectComplete()
                .verify();
    }

    @Test
    void whenAuctionFinishedEventNoUserIdFound_thenNotificationShouldBeSaved() {
        AuctionFinishedNotificationEvent notificationEvent = AuctionFinishedNotificationEvent
                .builder()
                .auctionId(1L)
                .itemId(1L)
                .timestamp(LocalDateTime.now())
                .finalPrice(BigDecimal.valueOf(200))
                .build();

        notificationProducerTemplate.send("auction-finished-notification-events", notificationEvent)
                .block();

        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(notificationRepository.findByAuctionId(notificationEvent.getAuctionId()))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    void whenAuctionDeletedNotificationsEventIsConsumed_thenNotificationShouldBeDeleted() {
        AuctionItemEvent auctionItemEvent = new AuctionItemEvent(1L, 1L);

        com.maria.entity.Notification newNotification = new com.maria.entity.Notification();
        newNotification.setUserId(1L);
        newNotification.setAuctionId(1L);
        newNotification.setMessage(NotificationServiceConstants.MESSAGE_NEW_BID + 1000);
        newNotification.setTimestamp(LocalDateTime.now());
        newNotification.setItemId(1L);
        newNotification.setRead(false);

        notificationRepository.save(newNotification).block();

        Mono.delay(Duration.ofSeconds(15)).block();

        removeAuctionProducerTemplate.send("delete-auction-events", auctionItemEvent)
                .block();

        Mono.delay(Duration.ofSeconds(15)).block();

        StepVerifier.create(notificationRepository.findByAuctionId(newNotification.getAuctionId()))
                .expectNextCount(0)
                .verifyComplete();
    }
}




