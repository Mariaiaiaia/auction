package com.maria.service;

import com.maria.config.ReactiveKafkaConfig;
import com.maria.core.entity.*;
import com.maria.entity.Auction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuctionKafkaServiceImpl implements AuctionKafkaService{
    private ReactiveKafkaConsumerTemplate<String, NewBitEvent> bitConsumerTemplate;
    private  ReactiveKafkaConsumerTemplate<String, AcceptanceEvent> acceptanceConsumerTemplate;
    private final ReactiveKafkaConfig kafkaConfig;
    private ReactiveKafkaProducerTemplate<String, InvitationEvent> invitationProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, AuctionItemEvent> removeAuctionProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, Notification> notificationProducerTemplate;
    private ReactiveKafkaProducerTemplate<String, AuctionItemEvent> createAuctionProducerTemplate;

    @PostConstruct
    private void initialize() {
        this.bitConsumerTemplate = kafkaConfig.createReactiveKafkaConsumerTemplate("new-bid-events", NewBitEvent.class, "bid-consumer-group");
        this.acceptanceConsumerTemplate = kafkaConfig.createReactiveKafkaConsumerTemplate("acceptance-events", AcceptanceEvent.class, "acceptance-consumer-group");
        this.invitationProducerTemplate = kafkaConfig.createReactiveKafkaProducerTemplate();
        this.removeAuctionProducerTemplate = kafkaConfig.createReactiveKafkaProducerTemplate();
        this.notificationProducerTemplate = kafkaConfig.createReactiveKafkaProducerTemplate();
        this.createAuctionProducerTemplate = kafkaConfig.createReactiveKafkaProducerTemplate();
    }

    @Override
    public void listenToBids(Function<NewBitEvent, Mono<Void>> eventHandler){
        bitConsumerTemplate
                .receiveAutoAck()
                .concatMap(record -> {
                    NewBitEvent event = record.value();
                    log.info("Bid event: new bid: {} from user: {}", event.getBidAmount(), event.getBidderId());

                    return eventHandler.apply(event);
                })
                .doOnError(error -> log.error("Error in Kafka bid consumer: {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    @Override
    public void listenToAcceptances(Function<AcceptanceEvent, Mono<Void>> eventHandler){
        acceptanceConsumerTemplate
                .receiveAutoAck()
                .flatMap(record -> {
                    AcceptanceEvent event = record.value();

                    log.info("Acceptance event: from user: {}", event.getUserId());
                    return eventHandler.apply(event);
                })
                .doOnError(error -> log.error("Error in Kafka acceptances consumer: " + error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe();
    }

    private Mono<Void> sendNotification(String topic, Notification notification){
        return notificationProducerTemplate.send(topic, notification)
                .doOnSuccess(result -> log.info("Notification sent successfully to topic {}", topic))
                .doOnError(ex -> log.error("Failed to send notification for topic {}: {}",topic, ex.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }

    @Override
    public Mono<Void> sendNewBidNotificationEvent(Auction auction){
        NewBidNotificationEvent notificationEvent = NewBidNotificationEvent
                .builder()
                .auctionId(auction.getAuctionId())
                .newBid(auction.getCurrentPrice())
                .timestamp(LocalDateTime.now())
                .type(NotificationType.NEW_BID)
                .bidderId(auction.getBidderId())
                .build();

        return sendNotification("new-bid-notification-events", notificationEvent);
    }

    @Override
    public Mono<Void> sendAuctionFinishedNotificationEvent(Auction auction){
        AuctionFinishedNotificationEvent notificationEvent = AuctionFinishedNotificationEvent
                .builder()
                .auctionId(auction.getAuctionId())
                .itemId(auction.getItemId())
                .timestamp(LocalDateTime.now())
                .finalPrice(auction.getCurrentPrice())
                .build();

        return sendNotification("auction-finished-notification-events", notificationEvent);
    }

    @Override
    public Mono<Void> sendUserRemovedFromAuctionEvent (Auction auction, Long userId){
        UserRemovedNotificationEvent notificationEvent = UserRemovedNotificationEvent
                .builder()
                .userId(userId)
                .itemId(auction.getItemId())
                .auctionId(auction.getAuctionId())
                .timestamp(LocalDateTime.now())
                .build();

        return sendNotification("user-removed-notification-events", notificationEvent);
    }

    @Override
    public Mono<Void> sendInvitationEvent (Long auctionId, Long sellerId, Long userId){
        return invitationProducerTemplate.send("auction-invitations-events", new InvitationEvent(auctionId, sellerId, userId))
                .doOnSuccess(result -> log.info("Invitation event sent successfully for auction {} to user {}", auctionId, userId))
                .doOnError(error -> log.error("Failed to send invitation for auction {} to user {}: {}", auctionId, userId, error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }

    @Override
    public Mono<Void> sendAuctionCreatedEvent (Auction auction){
        return createAuctionProducerTemplate.send("auction-created-events", new AuctionItemEvent(auction.getAuctionId(), auction.getItemId()))
                .doOnSuccess(result -> log.info("Auction created event sent successfully for auction {}", auction.getAuctionId()))
                .doOnError(error -> log.error("Failed to send auction created event for auction {}", auction.getAuctionId()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }

    @Override
    public Mono<Void> sendAuctionRemovedEvent (Auction auction){
        return removeAuctionProducerTemplate.send("delete-auction-events", new AuctionItemEvent(auction.getAuctionId(), auction.getItemId()))
                .doOnSuccess(result -> log.info("Auction removed event sent successfully for auction {}", auction.getAuctionId()))
                .doOnError(error -> log.error("Failed to send auction removed event for auction {}", auction.getAuctionId()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .then();
    }
}
